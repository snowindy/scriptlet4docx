package org.scriptlet4docx.docx;

import static org.junit.Assert.*;

import java.io.IOException;

import org.scriptlet4docx.docx.DividedScriptWrapsProcessor;
import org.junit.Test;

import org.scriptlet4docx.util.test.TestUtils;

public class DividedScriptWrapsProcessorTest {

	@Test
	public void testProcess_dollar() throws IOException {
		String template = TestUtils.readResource("/docx/DividedScriptWrapsProcessor-1.xml");

		String result = DividedScriptWrapsProcessor.process(template);

		assertTrue(result != null);
		assertTrue(result.contains("${"));
		assertTrue(!result.contains("${contract"));
	}

	@Test
	public void testProcess_ltGt() throws IOException {
		String template = TestUtils.readResource("/docx/DividedScriptWrapsProcessor-2.xml");

		String result = DividedScriptWrapsProcessor.process(template);

		assertTrue(result != null);
		assertTrue(result.contains("&lt;%"));
		assertTrue(result.contains("%&gt;"));
	}

	@Test
	public void testProcess_ltGt_out() throws IOException {
		String template = TestUtils.readResource("/docx/DividedScriptWrapsProcessor-3.xml");

		String result = DividedScriptWrapsProcessor.process(template);

		assertTrue(result != null);
		assertTrue(result.contains("&lt;%="));
		assertTrue(result.contains("%&gt;"));
	}
}
