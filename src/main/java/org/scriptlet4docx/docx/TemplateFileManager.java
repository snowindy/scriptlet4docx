package org.scriptlet4docx.docx;

import groovy.util.AntBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

public class TemplateFileManager {

    private TemplateFileManager() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        templatesDir = new File(tmpDir, "docx-tmpl-" + new Date().getTime() + "-" + UUID.randomUUID().toString());
        templatesDir.mkdirs();
    }

    private volatile static TemplateFileManager instance = new TemplateFileManager();

    public static TemplateFileManager getInstance() {
        return instance;
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

    static final String DOC_UNZIP_FOLDER_NAME = "/doc-unzip";
    static final String DOC_READY_STREAM_FOLDER_NAME = "/doc-ready-streamed";
    static final String DOC_CONTENT_PREPROCESSED = "/doc-tmpl-preprocessed.xml";
    static final String DOC_FROM_STREAM = "/tmpl-from-stream.docx";

    public File getTemplateUnzipFolder(String templateKey) {
        return new File(templatesDir, templateKey + "/" + DOC_UNZIP_FOLDER_NAME);
    }

    public File createTmpProcessFolder() {
        return new File(templatesDir, UUID.randomUUID().toString());
    }

    private File getTmplPreprocessedFile(String templateKey) {
        return new File(templatesDir, templateKey + "/" + DOC_CONTENT_PREPROCESSED);
    }

    public boolean isPrepared(String templateKey) {
        File dir = getTemplateUnzipFolder(templateKey);
        return dir.exists();
    }

    public void prepare(File pathToDocx, String templateKey) throws IOException {
        File dir = getTemplateUnzipFolder(templateKey);
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

    public File getTemplateFileFromStream(String templateKey) {
        return new File(templatesDir, templateKey + "/" + DOC_FROM_STREAM);
    }

    public boolean isTemplateFileFromStreamExists(String templateKey) {
        return getTemplateFileFromStream(templateKey).exists();
    }

    public void saveTemplateFileFromStream(String templateKey, InputStream iStream) throws IOException {
        File f = getTemplateFileFromStream(templateKey);
        FileUtils.deleteQuietly(f);
        FileUtils.copyInputStreamToFile(iStream, f);
    }

    public File getUniqueOutStreamFile() {
        return new File(templatesDir, DOC_READY_STREAM_FOLDER_NAME + "/" + UUID.randomUUID().toString());
    }
}
