package org.scriptlet4docx.docx;

import groovy.util.AntBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

public class TemplateFileManager {

    public TemplateFileManager() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        templatesDir = new File(tmpDir, "docx-tmpl-" + new Date().getTime() + "-" + UUID.randomUUID().toString());
        templatesDir.mkdirs();
    }

    private File templatesDir;

    /**
     * For test use
     */
    File getTemplatesDir() {
        return templatesDir;
    }

    public String getTemplateContent(String templateKey) throws IOException {
        File contentFile = getTmplPreprocessedFile(templateKey);

        if (!contentFile.exists()) {
            File dir = getTemplateUnzipFolder(templateKey);
            contentFile = new File(dir, DocxTemplater.PATH_TO_CONTENT);
        }
        return FileUtils.readFileToString(contentFile, "UTF-8");
    }

    static final String DOC_UNIZIP_FOLDER_NAME = "/doc-unzip";
    static final String DOC_CONTENT_PREPROCESSED = "/doc-tmpl-preprocessed.xml";

    public File getTemplateUnzipFolder(String templateKey) {
        return new File(templatesDir, templateKey + "/" + DOC_UNIZIP_FOLDER_NAME);
    }

    public File createTmpProcessFolder() {
        return new File(templatesDir, UUID.randomUUID().toString());
    }

    private File getTmplPreprocessedFile(String templateKey) {
        return new File(templatesDir, templateKey + "/" + DOC_CONTENT_PREPROCESSED);
    }

    public void prepare(File pathToDocx, String templateKey) throws IOException {
        File dir = getTemplateUnzipFolder(templateKey);
        if (!dir.exists()) {
            if (pathToDocx.exists() && pathToDocx.isFile()) {
                AntBuilder antBuilder = new AntBuilder();
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("src", pathToDocx);
                params.put("dest", dir);
                params.put("overwrite", "true");
                antBuilder.invokeMethod("unzip", params);
            } else {
                throw new FileNotFoundException(String.format("Cannot find docx template: '%s'",
                        pathToDocx.getAbsolutePath()));
            }
        }
    }

    public boolean isPreProcessedTemplateExists(String templateKey) {
        File preprocessed = getTmplPreprocessedFile(templateKey);
        return preprocessed.exists();
    }

    public void savePreProcessed(String templateKey, String content) throws IOException {
        File preprocessed = getTmplPreprocessedFile(templateKey);
        FileUtils.writeStringToFile(preprocessed, content, "UTF-8");
    }

    public void cleanup() throws IOException {
        FileUtils.deleteDirectory(templatesDir);
        templatesDir.mkdirs();
    }
}
