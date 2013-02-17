package org.scriptlet4docx.docx;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scriptlet4docx.util.test.TestUtils;

public class DocxTemplaterTest {

    static HashMap<String, Object> params;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        params = new HashMap<String, Object>();
        HashMap<String, String> contract = new HashMap<String, String>();
        contract.put("number", "123#445");
        params.put("value", 1);
        params.put("contract", contract);
        params.put("escapeTest", "This should be escaped: &, <, >.");

        List<String> personList = new ArrayList<String>();
        personList.add("vasya");
        personList.add("petya");

        params.put("personList", personList);
        params.put("menList", personList);

        List<HashMap<String, Object>> employeeList = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> p1 = new HashMap<String, Object>();
        p1.put("name", "Tom");
        p1.put("address", "Moscow");
        HashMap<String, Object> p2 = new HashMap<String, Object>();
        p2.put("name", "John");
        p2.put("address", "New York");
        employeeList.add(p1);
        employeeList.add(p2);

        params.put("employeeList", employeeList);
    }

    @Test
    public void testProcessScriptedTemplate() throws Exception {
        String template = TestUtils.readResource("/docx/DocxTemplaterTest-1.xml");

        String result = DocxTemplater.processScriptedTemplate(template, params);

        assertTrue(result != null);
        assertTrue(result.contains("123#445"));
        assertTrue(StringUtils.countMatches(result, "123#445") == 4);
    }

    @Test
    public void testProcessScriptedTemplate_brokenType1() throws Exception {
        String template = TestUtils.readResource("/docx/DocxTemplaterTest-2.xml");

        String result = DocxTemplater.processScriptedTemplate(template, params);

        assertTrue(result != null);
        assertTrue(result.contains("123#445"));
        assertTrue(StringUtils.countMatches(result, "123#445") == 1);
    }

    @Test
    public void testPreProcessTableScripting_multiTr() throws Exception {
        String template = TestUtils.readResource("/docx/DocxTemplaterTest-5.xml");

        String result = TableScriptingProcessor.process(template);

        assertTrue(result != null);
        assertTrue(!result.contains("$["));

        assertTrue(result.contains("&lt;% def iterStatus=0; for ( wawawa in tour.wawawa ) { iterStatus++; %&gt;"));
        assertTrue(result.contains("${wawawa.myway}"));

        assertTrue(result.contains("&lt;% iterStatus=0; for ( mamama in tour1.mamama ) { iterStatus++; %&gt;"));
        assertTrue(result.contains("${mamama.myway}"));

    }

    @Test
    public void testProcessScriptedTemplate_tableScripting() throws Exception {
        String template = TestUtils.readResource("/docx/DocxTemplaterTest-6.xml");

        String result = DocxTemplater.processScriptedTemplate(template, params);

        assertTrue(result != null);
        assertTrue(!result.contains("$["));

        assertTrue(result.contains(">vasya<"));
        assertTrue(result.contains(">petya<"));

    }

    @Test
    public void testProcessScriptedTemplate_brokenType2() throws Exception {
        String template = TestUtils.readResource("/docx/DocxTemplaterTest-3.xml");

        String result = DocxTemplater.processScriptedTemplate(template, params);

        assertTrue(result != null);
        assertTrue(result.contains("123#445"));
        assertTrue(StringUtils.countMatches(result, "123#445") == 1);
    }

    @Test
    public void testProcessScriptedTemplate_brokenType2_noProcess() throws Exception {
        String template = TestUtils.readResource("/docx/DocxTemplaterTest-7.xml");

        String result = DocxTemplater.processScriptedTemplate(template, params);

        assertTrue(result != null);
        assertTrue(template.equals(result));
    }

    @Test
    public void testProcessScriptedTemplate_tableScripting_iterStatus() throws Exception {
        String template = TestUtils.readResource("/docx/DocxTemplaterTest-8.xml");

        String result = DocxTemplater.processScriptedTemplate(template, params);

        assertTrue(result != null);
        assertTrue(!result.contains("$["));

        assertTrue(result.contains(">vasya<"));
        assertTrue(result.contains(">petya<"));

        assertTrue(!result.contains(">${iterStatus}<"));

        assertTrue(result.contains(">1<"));
        assertTrue(result.contains(">2<"));

    }

    @Test
    public void testProcessScriptedTemplate_tableScripting_iterStatus_multiTable() throws Exception {
        String template = TestUtils.readResource("/docx/DocxTemplaterTest-4.xml");

        String result = DocxTemplater.processScriptedTemplate(template, params);

        assertTrue(result != null);
        assertTrue(!result.contains("$["));

        assertTrue(result.contains(">vasya<"));
        assertTrue(result.contains(">petya<"));

        assertTrue(!result.contains(">${iterStatus}<"));

        assertTrue(result.contains(">1<"));
        assertTrue(result.contains(">2<"));

        assertTrue(StringUtils.countMatches(result, ">1<") == 2);
        assertTrue(StringUtils.countMatches(result, ">2<") == 2);

    }

    @Test
    public void testProcessScriptedTemplate_logicScriptlets() throws Exception {
        String template = TestUtils.readResource("/docx/DocxTemplaterTest-9.xml");

        HashMap<String, Object> params1 = new HashMap<String, Object>();
        params1.put("value", 1);

        String result = DocxTemplater.processScriptedTemplate(template, params1);

        assertTrue(result != null);
        assertTrue(!result.contains("else"));
        assertTrue(!result.contains("if"));

        assertTrue(result.contains("mom and dad"));
        assertTrue(result.contains("like kitties"));

        params1 = new HashMap<String, Object>();
        params1.put("value", 0);

        result = DocxTemplater.processScriptedTemplate(template, params1);

        assertTrue(result != null);
        assertTrue(!result.contains("else"));
        assertTrue(!result.contains("if"));

        assertTrue(result.contains("mom and dad"));
        assertTrue(result.contains("like dogs"));
    }

    @Test
    public void testProcessScriptedTemplate_spacePreserve() throws Exception {
        String template = TestUtils.readResource("/docx/DocxTemplaterTest-10.xml");

        String result = DocxTemplater.processScriptedTemplate(template, params);

        assertTrue(result != null);
        assertTrue(!result.contains("print"));

        assertTrue(result.contains("like dogs"));
    }

    @Test
    public void testProcess() throws Exception {
        File inFile = File.createTempFile("DocxTemplaterTest-1", ".docx");
        inFile.deleteOnExit();
        File resFile = new File("target/test-files/DocxTemplaterTest-1-result.docx");
        resFile.delete();

        TestUtils.copyResourceToFile("/docx/DocxTemplaterTest-1.docx", inFile);

        DocxTemplater docxTemplater = new DocxTemplater(inFile);

        docxTemplater.process(resFile, params);

        assertTrue(resFile.exists());
        assertTrue(resFile.length() > 0);
    }

    @Test
    public void testProcessScriptedTemplate_escapeAmpLtGt() throws Exception {
        String template = TestUtils.readResource("/docx/DocxTemplaterTest-11.xml");

        HashMap<String, Object> params = new HashMap<String, Object>();
        
        params.put("escapeTest", "This should be escaped: &, <, >.");
        
        String result = DocxTemplater.processScriptedTemplate(template, params);

        assertTrue(result.contains(">This should be escaped: &amp;, &lt;, &gt;.<"));
    }

}
