package org.xidea.el.json.test;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xidea.el.impl.ExpressionFactoryImpl;
import org.xidea.el.json.JSONTokenizer;

public class JSONTokenizerTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testE(){
		//new JSONTokenizer("0xfff2ed /19.5e-2+ 2 +19.5E-2").parse();
		String word = "0xfff2ed /19.5e-2+ 2 +19.5E-2";
		double expected = 0xfff2ed /19.5e-2+ 2 +19.5E-2;
		doTest(word,(float) expected);
		doTest("(19E2)", (float)(19E2));
		//doTest("(0xCCFF%2)+(0676/(19.5E-2)-(19.5E-2))*(0676/(19.5E-2)-(19.5E-2))",(float)((0xCCFF%2)+(0676/(19.5E-2)-(19.5E-2))*(0676/(19.5E-2)-(19.5E-2))));
		System.out.println(010);
		doTest("067",(float)(067));
		//doTest("0676/(19.5E-2)-(19.5E-2)",(float)(0676/(19.5E-2)-(19.5E-2)));
		
	}

	private void doTest(String word, Float expected) {
		Float actual = ((Number) ExpressionFactoryImpl.getInstance().create(word).evaluate("")).floatValue();
		System.out.println(actual);
		Assert.assertEquals( new Float(expected*1000).intValue(),new Float(actual*1000).intValue());
	}
}
