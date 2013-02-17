package org.scriptlet4docx.util.string;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.scriptlet4docx.util.string.StringUtil;
import org.scriptlet4docx.util.test.TestUtils;
import org.junit.Test;

public class StringUtilTest {

	@Test
	public void testReplaceOneByOne() {
		String in = "wswsw RRR d dh gh  gh g RRR d";
		String out = StringUtil.replaceOneByOne(in, "RRR", Arrays.asList("va", "vt"));

		assertEquals("wswsw va d dh gh  gh g vt d", out);
		
		in = "wswsw RRR d dh gh  gh g RRR d RRR";
		out = StringUtil.replaceOneByOne(in, "RRR", Arrays.asList("va", "vt"));
		
		assertEquals("wswsw va d dh gh  gh g vt d ", out);

	}
	
	@Test
    public void testNoBreakingInitialTemplate() {
	    String in = TestUtils.readResource("/util/string/StringUtilTest-1.txt");
	    
	    String out = StringUtil.replaceOneByOne(in, "4d5f4c1a-b11a-45f0-834c-e716b278e349", Arrays.asList(new String[]{}));
	    
	    assertEquals(in, out);
	}
	
	@Test
    public void testEscapeSimpleSet(){
	    String in = "markup text <, >> & &amp;";

        assertEquals("markup text &lt;, &gt;&gt; &amp; &amp;amp;", StringUtil.escapeSimpleSet(in));
	}

}
