package org.scriptlet4docx.docx;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.scriptlet4docx.docx.TableScriptingRowProcessor;
import org.junit.Test;

import org.scriptlet4docx.util.test.TestUtils;

public class TableScriptingRowProcessorTest {

	@Test
	public void testProcess() throws IOException {
		String template = TestUtils.readResource("/docx/TableScriptingRowProcessorTest-1.xml");

		TableScriptingRowProcessor tableScriptingRowProcessor = new TableScriptingRowProcessor();

		String result = tableScriptingRowProcessor.process(template);

		assertTrue(result != null);
		assertTrue(!result.contains("$["));
		assertTrue(result.contains("&lt;% def iterStatus=0; for ( wawawa in tour.wawawa ) { iterStatus++; %&gt;"));
		assertTrue(result.contains("${wawawa.myway}"));

	}

	@Test
	public void testProcess_rootVar() throws Exception {
		String cleanTr = TestUtils.readResource("/docx/TableScriptingRowProcessorTest-2.xml");

		TableScriptingRowProcessor tableScriptingRowProcessor = new TableScriptingRowProcessor();

		String result = tableScriptingRowProcessor.process(cleanTr);

		assertTrue(result != null);
		assertTrue(!result.contains("$["));
		assertTrue(result.contains("&lt;% def iterStatus=0; for ( person in personList ) { iterStatus++; %&gt;"));
		assertTrue(result.contains("${person}"));
	}

	@Test
	public void testProcess_applyFunc1() throws Exception {
		String cleanTr = TestUtils.readResource("/docx/TableScriptingRowProcessorTest-3.xml");

		TableScriptingRowProcessor tableScriptingRowProcessor = new TableScriptingRowProcessor();
		String result = tableScriptingRowProcessor.process(cleanTr);

		assertTrue(result != null);
		assertTrue(!result.contains("$["));
		assertTrue(result.contains("&lt;% def iterStatus=0; for ( wawawa in tour.wawawa ) { iterStatus++; %&gt;"));
		assertTrue(result.contains("${repSrv.applyFunc(wawawa.myway)}"));
	}

	@Test
	public void testProcess_varPref1() throws Exception {
		String cleanTr = TestUtils.readResource("/docx/TableScriptingRowProcessorTest-4.xml");

		TableScriptingRowProcessor tableScriptingRowProcessor = new TableScriptingRowProcessor();
		String result = tableScriptingRowProcessor.process(cleanTr);

		assertTrue(result != null);
		assertTrue(!result.contains("$["));
		assertTrue(result
				.contains("&lt;% def iterStatus=0; for ( wawawa in tourist.tour.wawawa ) { iterStatus++; %&gt;"));
		assertTrue(result.contains("${wawawa.myway}"));
	}

	@Test
	public void testProcess_varPref2() throws Exception {
		String cleanTr = TestUtils.readResource("/docx/TableScriptingRowProcessorTest-5.xml");

		TableScriptingRowProcessor tableScriptingRowProcessor = new TableScriptingRowProcessor();
		String result = tableScriptingRowProcessor.process(cleanTr);

		assertTrue(result != null);
		assertTrue(!result.contains("$["));
		assertTrue(result
				.contains("&lt;% def iterStatus=0; for ( wawawa in tourist().tour.wawawa ) { iterStatus++; %&gt;"));
		assertTrue(result.contains("${wawawa.myway}"));
	}

	@Test
	public void testProcess_applyFunc2() throws Exception {
		String cleanTr = TestUtils.readResource("/docx/TableScriptingRowProcessorTest-6.xml");

		TableScriptingRowProcessor tableScriptingRowProcessor = new TableScriptingRowProcessor();
		String result = tableScriptingRowProcessor.process(cleanTr);

		assertTrue(result != null);
		assertTrue(!result.contains("$["));
		assertTrue(result.contains("&lt;% def iterStatus=0; for ( wawawa in tour.wawawa ) { iterStatus++; %&gt;"));
		assertTrue(result.contains("${tour.applyFunc(wawawa.myway)}"));
	}

	@Test
	public void testProcess_iterStatus() throws Exception {
		String cleanTr = TestUtils.readResource("/docx/TableScriptingRowProcessorTest-7.xml");

		TableScriptingRowProcessor tableScriptingRowProcessor = new TableScriptingRowProcessor();
		String result = tableScriptingRowProcessor.process(cleanTr);

		assertTrue(result != null);
		assertTrue(!result.contains("$["));
		assertTrue(result.contains("&lt;% def iterStatus=0; for ( wawawa in wawawaList ) { iterStatus++; %&gt;"));
		assertTrue(result.contains("${wawawa.myway}"));
	}

}
