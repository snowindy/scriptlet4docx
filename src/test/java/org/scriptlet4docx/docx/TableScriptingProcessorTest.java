package org.scriptlet4docx.docx;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.scriptlet4docx.docx.TableScriptingProcessor;
import org.scriptlet4docx.docx.TemplateVarException;
import org.junit.Test;

import org.scriptlet4docx.util.test.TestUtils;

public class TableScriptingProcessorTest {

	@Test
	public void testProccess1() throws IOException {
		String template =  TestUtils.readResource(
				"/docx/TableScriptingProcessorTest-1.xml");

		String result = TableScriptingProcessor.process(template);

		assertTrue(result != null);
		assertTrue(!result.contains("$["));
		assertTrue(result
				.contains("&lt;% def iterStatus=0; for ( wawawa in tour.wawawa ) { iterStatus++; %&gt;"));
		assertTrue(result.contains("${wawawa.myway}"));

	}
	
	@Test(expected = TemplateVarException.class)
	public void testProccess_multiVarEx1() throws IOException {
		String template =  TestUtils.readResource(
				"/docx/TableScriptingProcessorTest-2.xml");

		TableScriptingProcessor.process(template);
	}
	
	@Test(expected = TemplateVarException.class)
	public void testProccess_multiVarEx2() throws IOException {
		String template =  TestUtils.readResource(
				"/docx/TableScriptingProcessorTest-3.xml");

		TableScriptingProcessor.process(template);
	}
	
	@Test
	public void testProccess_rootVar() throws Exception {
		String cleanTr =  TestUtils.readResource(
				"/docx/TableScriptingProcessorTest-4.xml");

		String result = TableScriptingProcessor.process(cleanTr);

		assertTrue(result != null);
		assertTrue(!result.contains("$["));
		assertTrue(result
				.contains("&lt;% def iterStatus=0; for ( wawawa in wawawaList ) { iterStatus++; %&gt;"));
		assertTrue(result.contains("${wawawa.id}"));
	}
	
	@Test
	public void testProccess_iterStatus_MultiTables() throws Exception {
		String cleanTr =  TestUtils.readResource(
				"/docx/TableScriptingProcessorTest-5.xml");

		String result = TableScriptingProcessor.process(cleanTr);

		assertTrue(result != null);
		assertTrue(!result.contains("$["));
		assertTrue(result
				.contains("&lt;% def iterStatus=0; for ( wawawa in wawawaList ) { iterStatus++; %&gt;"));
		assertTrue(result.contains("${wawawa.id}"));
		
		assertTrue(result
				.contains("&lt;% iterStatus=0; for ( bababa in bababaList ) { iterStatus++; %&gt;"));
		assertTrue(result.contains("${bababa.id}"));
	}

}
