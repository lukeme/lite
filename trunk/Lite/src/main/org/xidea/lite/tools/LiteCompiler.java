package org.xidea.lite.tools;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.el.impl.CommandParser;
import org.xidea.el.json.JSONEncoder;
import org.xidea.jsi.JSIRuntime;
import org.xidea.lite.impl.HotTemplateEngine;
import org.xidea.lite.impl.ParseUtil;
import org.xidea.lite.parse.ParseContext;

public class LiteCompiler {
	private static final Log log = LogFactory.getLog(LiteCompiler.class);
	private File root;
	private File config;
	private String path;
	private Map<String, byte[]> resultMap;
	private File output;
	private String[] includes;
	private String[] excludes;
	private HotTemplateEngine engine;
	private String translator;
	private ResourceManagerImpl resourceManager;

	public LiteCompiler(String[] args) {
		log.info("Args:" + JSONEncoder.encode(args));
		CommandParser cp = new CommandParser(args);
		cp.setup(this);
	}

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[] {
					// "-root","D:\\workspace\\FileServer/src/main/org/jside/filemanager/","-litecode","D:\\workspace\\FileServer/build/dest/lite","-nodeParsers","org.xidea.lite.parser.impl.HTMLNodeParser"
					"-root", "D:\\workspace\\Lite2/web/", "-output",
					"D:\\workspace\\Lite2/build/dest/web", "-path",
					"/doc/guide/index.xhtml", "-includes",
					"/doc/guide/*.xhtml", "-excludes",
					"/doc/guide/layout.xhtml", "-translators", "php" };
		}
		new LiteCompiler(args).execute();
	}

	public void execute() {
		try {
			initialize();
			if (path == null) {
				this.processDir(root, "/");
				for (String path : this.resourceManager.getLinkedResources()) {
					this.processFile(path);
				}
			} else {
				this.processFile(path);
			}
			if (!output.exists()) {
				if (output.getName().endsWith(".zip")) {
					output.getParentFile().mkdir();
					output.createNewFile();
				} else {
					output.mkdir();
				}
			}
			if (output.getName().endsWith(".zip")) {
				this.writeZipResult();
			} else {
				this.writeDirResult();
			}

			log.info("执行成功");
		} catch (Exception e) {
			log.error("编译失败", e);
		}
	}

	private void writeZipResult() throws IOException {
		ZipOutputStream zipos = new ZipOutputStream(new FileOutputStream(output));
		for (String path : resultMap.keySet()) {
			zipos.setMethod(ZipOutputStream.DEFLATED);
			zipos.putNextEntry(new ZipEntry(path.substring(1)));
			zipos.write(resultMap.get(path));
		}
		zipos.flush();
		zipos.finish();
		zipos.close();
	}

	private void writeDirResult() throws IOException {
		for (String path : resultMap.keySet()) {
			byte[] data = resultMap.get(path);
			File cachedFile = new File(output, path);
			cachedFile.getParentFile().mkdirs();
			OutputStream out = new FileOutputStream(cachedFile);
			try {
				out.write(data);
				out.flush();
				log.info("文件写入成功:" + cachedFile);
			} finally {
				out.close();
			}
		}

	}

	protected void initialize() throws IOException {
		if (root == null) {
			root = new File(".");
		}
		if (config == null) {
			config = new File(root, "/WEB-INF/lite.xml");
		}
		this.resourceManager = new ResourceManagerImpl(root.toURI(), config
				.exists() ? config.toURI() : null);
		engine = new HotTemplateEngine(resourceManager);

		if (output != null) {
			if (!output.exists()) {
				log.info("mkdirs:" + output);
				output.mkdirs();
			}
		}
	}

	public void processDir(final File dir, final String path) {
		log.info("处理目录：" + dir.getAbsolutePath());
		final PathMatcher includes = PathMatcher.createMatcher(this.includes);
		final PathMatcher excludes = PathMatcher.createMatcher(this.excludes);

		dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if (!file.equals(output)) {
					String path2 = path + file.getName();
					if (file.isDirectory()) {
						if (file.getName().startsWith(".")) {
							log.warn("跳过目录：" + file);
						} else {
							if (excludes == null || !excludes.must(path)) {
								processDir(file, path2 + '/');
							}
						}
					} else if ((includes == null || includes.match(path2))
							&& (excludes == null || !excludes.match(path2))) {
						try {
							processFile(path2);
						} catch (IOException e) {
							log.error("文件处理异常:" + path2, e);
						}
					}
				}
				return false;
			}
		});
	}

	public boolean processFile(final String path) throws IOException {
		log.info("处理文件：" + path);
		if (this.resultMap.containsKey(path)) {
			return false;
		}
		if (resourceManager.isTemplate(path)) {
			try {
				String layout = resourceManager.getFeatureMap(path).get(
						ParseContext.FEATURE_LAYOUT);
				if (path.equals(layout)) {
					return false;
				}
				String path2 = translatePath(path);
				String result = engine.getLiteCode(path);

				String encoding = resourceManager.getEncoding(path);
				this.resultMap.put(path2, result.getBytes("utf-8"));
				if (this.translator != null) {
					if ("php".equals(translator)) {
						this.buildPHP(path, result, encoding);
					}
				}

				return true;
			} catch (Exception e) {
				// JOptionPane.showConfirmDialog(null, e);
				log.error("处理模板异常：" + path, e);
				return false;
			}
		} else {
			Object data = resourceManager.getFilteredContent(path);
			if (data instanceof byte[]) {
				this.resultMap.put(path, (byte[]) data);
			} else {
				String text = resourceManager.getFilteredText(path);
				String encoding = resourceManager.getEncoding(path);
				this.resultMap.put(path, text.getBytes(encoding));
			}
			return true;
		}
	}

	private String translatePath(final String path) {
		return "/WEB-INF/litecode/" + path.replace('/', '^');
	}

	private void buildPHP(String path, String litecode, String encoding)
			throws IOException {
		JSIRuntime runtime = ParseUtil.getJSIRuntime();
		Object translator = runtime
				.eval("new ($import('org.xidea.lite.impl.php:PHPTranslator',{}))('"
						+ path + "'," + litecode + ")");
		String result = (String) runtime.invoke(translator, "translate");
		this.resultMap.put(translatePath(path) + ".php", result
				.getBytes(encoding));
	}

	public void setRoot(File webRoot) {
		this.root = webRoot;
	}

	public void setConfig(File config) {
		this.config = config;
	}

	public void setOutput(File litecode) {
		this.output = litecode;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setIncludes(String[] includes) {
		this.includes = includes;
	}

	public void setExcludes(String[] excludes) {
		this.excludes = excludes;
	}

	public void setTranslator(String translator) {
		this.translator = translator;
	}

}
