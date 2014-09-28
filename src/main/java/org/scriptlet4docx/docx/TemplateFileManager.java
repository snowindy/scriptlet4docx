package org.scriptlet4docx.docx;

import groovy.util.AntBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.scriptlet4docx.docx.TemplateContent.ContentItem;

public class TemplateFileManager {

    private TemplateFileManager() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        templatesDir = new File(tmpDir + "/scriptlet4docx", FastDateFormat.getInstance("yyyy-MM-dd-HH-mm-ss-SSS")
                .format(new Date()));
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

    public TemplateContent getTemplateContent(String templateKey) throws IOException {
        File dir = new File(getTemplateUnzipFolder(templateKey), "word");

        List<ContentItem> items = new ArrayList<ContentItem>();

        for (File f : dir.listFiles()) {
            if (f.isFile()
                    && (f.getName().equals("document.xml") || f.getName().startsWith("header") || f.getName()
                            .startsWith("footer"))) {
                items.add(new ContentItem(f.getName(), null));
            }
        }

        for (ContentItem item : items) {
            File contentFile = getTmplPreprocessedFile(templateKey, item.getIdentifier());

            if (!contentFile.exists()) {
                contentFile = new File(dir, item.getIdentifier());
            }

            item.setContent(FileUtils.readFileToString(contentFile, "UTF-8"));
        }

        return new TemplateContent(items);
    }

    static final String DOC_UNZIP_FOLDER_NAME = "/doc-unzip";
    static final String DOC_READY_STREAM_FOLDER_NAME = "/doc-ready-streamed";
    static final String DOC_FROM_STREAM = "/tmpl-from-stream.docx";

    public File getTemplateUnzipFolder(String templateKey) {
        return new File(templatesDir, templateKey + "/" + DOC_UNZIP_FOLDER_NAME);
    }

    public File createTmpProcessFolder() {
        return new File(templatesDir, UUID.randomUUID().toString());
    }

    private File getTmplPreprocessedFile(String templateKey, String identifier) {
        return new File(templatesDir, templateKey + "/preproc-" + identifier);
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
        File preprocessed = getTmplPreprocessedFile(templateKey, "document.xml");
        return preprocessed.exists();
    }

    public void savePreProcessed(String templateKey, TemplateContent content) throws IOException {
        for (ContentItem item : content.getItems()) {
            File preprocessed = getTmplPreprocessedFile(templateKey, item.getIdentifier());
            FileUtils.writeStringToFile(preprocessed, item.getContent(), "UTF-8");
        }
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
