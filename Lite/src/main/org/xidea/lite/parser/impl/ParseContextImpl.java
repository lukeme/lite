package org.xidea.lite.parser.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.xidea.lite.parser.DecoratorContext;
import org.xidea.lite.parser.ResourceContext;
import org.xidea.lite.parser.TextParser;
import org.xidea.lite.parser.ParseContext;
import org.xidea.lite.parser.NodeParser;
import org.xidea.lite.parser.ResultTranslator;

/**
 * 不要较差调用，交叉调用，用this代替，确保继承安全
 * 取消final 之后，容易引发一个短路bug，等到发现之后再修复吧。
 * @author jindw
 */
public class ParseContextImpl extends ParseContextProxy implements ParseContext {
	private static final long serialVersionUID = 1L;

	protected final Map<String, String> featrueMap= new HashMap<String, String>();
	
	protected ParseContextImpl() {
	}
	public ParseContextImpl(ResourceContext base){
		initialize(base, null, null, null, null);
	}
	public ParseContextImpl(ResourceContext base,DecoratorContext decoratorContext, Map<String, String> featrues,
			NodeParser<? extends Object>[] parsers, TextParser[] ips) {
		initialize(base, decoratorContext,featrues, parsers, ips);
	}

	public ParseContextImpl(ParseContext parent, 
			ResultTranslator translator) {
		super(parent);
		//需要重设 ParseChain 的context
		this.parserHolder = new ParseHolderImpl(this,parent);
		this.resultContext = new ResultContextImpl(this);
		this.featrueMap.putAll(parent.getFeatrueMap());
		this.setResultTranslator(translator);
	}


	protected void initialize(ResourceContext base,DecoratorContext decoratorContext, Map<String, String> featrues,
			NodeParser<? extends Object>[] parsers, TextParser[] ips) {
		resourceContext = base;
		this.decoratorContext = decoratorContext;
		xmlContext = new XMLContextImpl(this);
		resultContext = new ResultContextImpl(this);
		parserHolder = new ParseHolderImpl(this, parsers, ips);
		initializeFeatrues(featrues);
	}

	protected void initializeFeatrues(Map<String, String> newFeatrues) {
		if (newFeatrues != null) {
			String v = newFeatrues.get("compress");
			if (v != null) {
				xmlContext.setCompress("true".equalsIgnoreCase(v));
			}
			v = newFeatrues.get("reserveSpace");
			if (v != null) {
				xmlContext.setReserveSpace("true".equalsIgnoreCase(v));
			}
			v = newFeatrues.get("format");
			if (v != null) {
				xmlContext.setFormat("true".equalsIgnoreCase(v));
			}
			featrueMap.clear();
			featrueMap.putAll(newFeatrues);
		}
	}

	public String getFeatrue(String key) {
		return featrueMap.get(key);
	}

	public Map<String, String> getFeatrueMap() {
		return featrueMap;
	}

	public void parse(Object source) {
		getTopChain().process(source);
	}

	public List<Object> parseText(String text, int defaultType) {
		int type = this.getTextType();
		int mark = this.mark();
		List<Object> result;
		try {
			this.setTextType(defaultType);
			parse(text);
		} finally {
			this.setTextType(type);
			result = this.reset(mark);
		}
		return result;
	}
}