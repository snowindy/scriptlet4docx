package org.scriptlet4docx.docx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scriptlet4docx.util.test.TestUtils;

public class DocxTemplaterTest extends Assert {

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

        DocxTemplater templater = new DocxTemplater(none);
        template = templater.cleanupTemplate(template);
        String result = templater.processCleanedTemplate(template, params);

        assertTrue(result != null);
        assertTrue(result.contains("123#445"));
        assertTrue(StringUtils.countMatches(result, "123#445") == 4);
    }

    @Test
    public void testProcessScriptedTemplate_brokenType1() throws Exception {
        String template = TestUtils.readResource("/docx/DocxTemplaterTest-2.xml");

        DocxTemplater templater = new DocxTemplater(none);
        template = templater.cleanupTemplate(template);
        String result = templater.processCleanedTemplate(template, params);

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
        DocxTemplater templater = new DocxTemplater(none);
        template = templater.cleanupTemplate(template);
        String result = templater.processCleanedTemplate(template, params);

        assertTrue(result != null);
        assertTrue(!result.contains("$["));

        assertTrue(result.contains(">vasya<"));
        assertTrue(result.contains(">petya<"));

    }

    @Test
    public void testProcessScriptedTemplate_brokenType2() throws Exception {
        String template = TestUtils.readResource("/docx/DocxTemplaterTest-3.xml");
        DocxTemplater templater = new DocxTemplater(none);
        template = templater.cleanupTemplate(template);
        String result = templater.processCleanedTemplate(template, params);

        assertTrue(result != null);
        assertTrue(result.contains("123#445"));
        assertTrue(StringUtils.countMatches(result, "123#445") == 1);
    }

    @Test
    public void testProcessScriptedTemplate_brokenType2_noProcess() throws Exception {
        String template = TestUtils.readResource("/docx/DocxTemplaterTest-7.xml");
        DocxTemplater templater = new DocxTemplater(none);
        String result = templater.processCleanedTemplate(template, params);

        assertTrue(result != null);
        assertTrue(template.equals(result));
    }

    @Test
    public void testProcessScriptedTemplate_tableScripting_iterStatus() throws Exception {
        String template = TestUtils.readResource("/docx/DocxTemplaterTest-8.xml");
        DocxTemplater templater = new DocxTemplater(none);
        template = templater.cleanupTemplate(template);
        String result = templater.processCleanedTemplate(template, params);

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
        DocxTemplater templater = new DocxTemplater(none);
        template = templater.cleanupTemplate(template);
        String result = templater.processCleanedTemplate(template, params);

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
        DocxTemplater templater = new DocxTemplater(none);
        template = templater.cleanupTemplate(template);
        String result = templater.processCleanedTemplate(template, params1);

        assertTrue(result != null);
        assertTrue(!result.contains("else"));
        assertTrue(!result.contains("if"));

        assertTrue(result.contains("mom and dad"));
        assertTrue(result.contains("like kitties"));

        params1 = new HashMap<String, Object>();
        params1.put("value", 0);

        template = templater.cleanupTemplate(template);
        result = templater.processCleanedTemplate(template, params1);

        assertTrue(result != null);
        assertTrue(!result.contains("else"));
        assertTrue(!result.contains("if"));

        assertTrue(result.contains("mom and dad"));
        assertTrue(result.contains("like dogs"));
    }

    @Test
    public void testProcessScriptedTemplate_spacePreserve() throws Exception {
        String template = TestUtils.readResource("/docx/DocxTemplaterTest-10.xml");
        DocxTemplater templater = new DocxTemplater(none);
        template = templater.cleanupTemplate(template);
        String result = templater.processCleanedTemplate(template, params);

        assertTrue(result != null);
        assertTrue(!result.contains("print"));

        assertTrue(result.contains("like dogs"));
    }

    @Test
    public void testProcess_file() throws Exception {
        File inFile = new File("src/test/resources/docx/DocxTemplaterTest-1.docx");
        File resFile = new File("target/test-files/DocxTemplaterTest-1-file-result.docx");
        resFile.delete();

        DocxTemplater docxTemplater = new DocxTemplater(inFile);

        docxTemplater.process(resFile, params);

        assertTrue(resFile.exists());
        assertTrue(resFile.length() > 0);
    }

    @Test
    public void testProcess_withInputStreamAsOutput() throws Exception {
        File inFile = new File("src/test/resources/docx/DocxTemplaterTest-1.docx");
        File resFile = new File("target/test-files/DocxTemplaterTest-stream-2-result.docx");
        resFile.delete();

        DocxTemplater templater = new DocxTemplater(inFile);

        InputStream resStream = templater.processAndReturnInputStream(params);

        FileUtils.copyInputStreamToFile(resStream, resFile);

        assertTrue(resFile.exists());
        assertTrue(resFile.length() > 0);

        assertTrue(new File(TemplateFileManager.getInstance().getTemplatesDir(),
                TemplateFileManager.DOC_READY_STREAM_FOLDER_NAME).listFiles().length == 0);
    }

    @Test
    public void testProcess_withOutputStream() throws Exception {
        File inFile = new File("src/test/resources/docx/DocxTemplaterTest-1.docx");
        File resFile = new File("target/test-files/DocxTemplaterTest-stream-3-result.docx");
        resFile.delete();

        DocxTemplater docxTemplater = new DocxTemplater(inFile);

        docxTemplater.process(new FileOutputStream(resFile), params);

        assertTrue(resFile.exists());
        assertTrue(resFile.length() > 0);
    }

    @Test
    public void testProcess_fileMultiRun() throws Exception {
        File inFile = File.createTempFile("docx-test", "docx");
        FileUtils.copyFile(new File("src/test/resources/docx/DocxTemplaterTest-1.docx"), inFile);
        inFile.deleteOnExit();
        File resFile = new File("target/test-files/DocxTemplaterTest-1-file-result1.docx");
        resFile.delete();

        final TemplateFileManager mgr = TemplateFileManager.getInstance();

        new NonStrictExpectations(mgr) {
        };

        DocxTemplater docxTemplater1 = new DocxTemplater(inFile);

        docxTemplater1.process(resFile, params);

        DocxTemplater docxTemplater2 = new DocxTemplater(inFile);

        docxTemplater2.process(resFile, params);

        new Verifications() {
            {
                mgr.prepare((File) any, anyString);
                times = 1;
            }
        };

        assertTrue(resFile.exists());
        assertTrue(resFile.length() > 0);
    }

    @Test
    public void testProcess_stream() throws Exception {
        File inFile = new File("src/test/resources/docx/DocxTemplaterTest-1.docx");
        File resFile = new File("target/test-files/DocxTemplaterTest-1-stream-result.docx");
        resFile.delete();

        DocxTemplater docxTemplater = new DocxTemplater(new FileInputStream(inFile), "k1");

        docxTemplater.process(resFile, params);

        assertTrue(resFile.exists());
        assertTrue(resFile.length() > 0);
    }

    @Test
    public void testProcess_streamMultiRun() throws Exception {
        File inFile = new File("src/test/resources/docx/DocxTemplaterTest-1.docx");
        File resFile = new File("target/test-files/DocxTemplaterTest-1-stream-result1.docx");
        resFile.delete();

        final InputStream stream1 = new FileInputStream(inFile);
        final InputStream stream2 = new FileInputStream(inFile);

        final DocxTemplater docxTemplater1 = new DocxTemplater(stream1, "k2");
        final DocxTemplater docxTemplater2 = new DocxTemplater(stream2, "k2");

        new NonStrictExpectations(stream2) {
        };

        docxTemplater1.process(resFile, params);
        docxTemplater2.process(resFile, params);

        // testing that stream2 was not actuall read but was closed
        new Verifications() {
            {
                stream2.read((byte[]) any);
                times = 0;
            }
        };
        new Verifications() {
            {
                stream2.read((byte[]) any, anyInt, anyInt);
                times = 0;
            }
        };
        new Verifications() {
            {
                stream2.close();
            }
        };

        assertTrue(resFile.exists());
        assertTrue(resFile.length() > 0);
    }

    private File none;

    @Test
    public void testProcessScriptedTemplate_escapeAmpLtGt() throws Exception {
        String template = TestUtils.readResource("/docx/DocxTemplaterTest-11.xml");

        HashMap<String, Object> params = new HashMap<String, Object>();

        params.put("escapeTest", "This should be escaped: &, <, >.");
        DocxTemplater templater = new DocxTemplater(none);
        template = templater.cleanupTemplate(template);
        String result = templater.processCleanedTemplate(template, params);

        assertTrue(result.contains(">This should be escaped: &amp;, &lt;, &gt;.<"));
    }

    @Test
    public void testProcessScriptedTemplate_nullsReplacement() throws Exception {
        String template = TestUtils.readResource("/docx/DocxTemplaterTest-12.xml");

        HashMap<String, Object> params = new HashMap<String, Object>();

        params.put("someNullyVar", null);
        DocxTemplater templater = new DocxTemplater(none);
        template = templater.cleanupTemplate(template);
        String result = templater.processCleanedTemplate(template, params);

        assertFalse(result.contains("space=\"preserve\">null<"));
        assertTrue(result.contains("space=\"preserve\"><"));
        
        assertFalse(result.contains("space=\"arg1\">null<"));
        assertTrue(result.contains("space=\"arg1\"><"));
        
        templater.setNullReplacement("UNKNOWD");
        result = templater.processCleanedTemplate(template, params);

        assertTrue(result.contains("space=\"preserve\">UNKNOWD<"));
        assertTrue(result.contains("space=\"arg1\">UNKNOWD<"));
    }

}
