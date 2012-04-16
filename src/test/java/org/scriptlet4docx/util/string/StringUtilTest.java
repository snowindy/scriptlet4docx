package org.scriptlet4docx.util.string;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.scriptlet4docx.util.string.StringUtil;
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

}
