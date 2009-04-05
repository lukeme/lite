package org.xidea.lite.parser.test;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xidea.el.json.JSONEncoder;
import org.xidea.lite.parser.ClientJSBuilder;
import org.xidea.lite.parser.XMLParser;

public class ClientJSBuilderTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testBuildJS() {
		List<Object> liteCode = new XMLParser().parse(this.getClass()
				.getResource("asciitable-client.xhtml"));
//		String result = new ClientJSBuilder().buildJS("testTemplate", liteCode);
		System.out.println("==JS Code==");
		System.out.println(liteCode);
	}

}
