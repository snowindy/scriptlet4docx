package org.scriptlet4docx.docx;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Assert;
import org.junit.Test;

public class TemplateFileManagerTest extends Assert {
    @Test
    public void testTemplatesDirOps() throws Exception {
        TemplateFileManager mgr = new TemplateFileManager();
        String templateKey = "k1";
        File tempDir = mgr.getTemplatesDir();
        File docxFile = new File("src/test/resources/docx/DocxTemplaterTest-1.docx");
        assertFalse(mgr.isPrepared(templateKey));
        mgr.prepare(docxFile, templateKey);
        assertTrue(mgr.isPrepared(templateKey));
        assertFalse(mgr.getTemplateContent(templateKey).isEmpty());
        assertEquals(mgr.getTemplateUnzipFolder(templateKey), new File(tempDir, templateKey + "/"
                + TemplateFileManager.DOC_UNIZIP_FOLDER_NAME));
        assertFalse(mgr.isPreProcessedTemplateExists(templateKey));
        mgr.savePreProcessed(templateKey, "1");
        assertTrue(mgr.isPreProcessedTemplateExists(templateKey));
        assertEquals("1", mgr.getTemplateContent(templateKey));

        assertFalse(mgr.isTemplateFileFromStreamExists(templateKey));
        mgr.saveTemplateFileFromStream(templateKey, new FileInputStream(docxFile));
        assertTrue(mgr.isTemplateFileFromStreamExists(templateKey));

        mgr.cleanup();
        assertTrue(tempDir.exists());
        assertEquals(0, tempDir.listFiles().length);

    }
}
