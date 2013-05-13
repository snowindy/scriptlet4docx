package org.scriptlet4docx.docx;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class TemplateFileManagerTest extends Assert {
    @Test
    public void testTemplatesDirOps() throws Exception {
        TemplateFileManager mgr = new TemplateFileManager();
        String templateKey = "k1";
        File tempDir = mgr.getTemplatesDir();
        mgr.prepare(new File("src/test/resources/docx/DocxTemplaterTest-1.docx"), templateKey);
        assertFalse(mgr.getTemplateContent(templateKey).isEmpty());
        assertEquals(mgr.getTemplateUnzipFolder(templateKey), new File(tempDir, templateKey + "/"
                + TemplateFileManager.DOC_UNIZIP_FOLDER_NAME));
        assertFalse(mgr.isPreProcessedTemplateExists(templateKey));
        mgr.savePreProcessed(templateKey, "1");
        assertTrue(mgr.isPreProcessedTemplateExists(templateKey));
        assertEquals("1", mgr.getTemplateContent(templateKey));
    }
}
