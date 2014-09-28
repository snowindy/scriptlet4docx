package org.scriptlet4docx.docx;

import groovy.util.Eval;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.scriptlet4docx.docx.TemplateContent.ContentItem;

public class TemplateFileManagerTest extends Assert {
    @Test
    public void testTemplatesDirOps() throws Exception {
        TemplateFileManager mgr = TemplateFileManager.getInstance();
        mgr.cleanup();

        String templateKey = "k1";
        File tempDir = mgr.getTemplatesDir();
        File docxFile = new File("src/test/resources/docx/DocxTemplaterTest-1.docx");
        assertFalse(mgr.isPrepared(templateKey));
        mgr.prepare(docxFile, templateKey);
        assertTrue(mgr.isPrepared(templateKey));
        assertFalse(Eval
                .x(mgr.getTemplateContent(templateKey).getItems(), "x.find{it.identifier == 'document.xml'}.content")
                .toString().isEmpty());
        assertEquals(mgr.getTemplateUnzipFolder(templateKey), new File(tempDir, templateKey + "/"
                + TemplateFileManager.DOC_UNZIP_FOLDER_NAME));
        assertFalse(mgr.isPreProcessedTemplateExists(templateKey));
        mgr.savePreProcessed(templateKey, new TemplateContent(Arrays.asList(new ContentItem("document.xml", "1"))));
        assertTrue(mgr.isPreProcessedTemplateExists(templateKey));
        assertEquals("1", Eval.x(mgr.getTemplateContent(templateKey).getItems(),
                "x.find{it.identifier == 'document.xml'}.content"));

        assertFalse(mgr.isTemplateFileFromStreamExists(templateKey));
        mgr.saveTemplateFileFromStream(templateKey, new FileInputStream(docxFile));
        assertTrue(mgr.isTemplateFileFromStreamExists(templateKey));

        mgr.cleanup();
        assertTrue(tempDir.exists());
        assertEquals(0, tempDir.listFiles().length);

    }
}
