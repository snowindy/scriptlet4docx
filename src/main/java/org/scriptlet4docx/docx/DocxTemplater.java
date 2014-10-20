package org.scriptlet4docx.docx;

import groovy.text.GStringTemplateEngine;
import groovy.util.AntBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.scriptlet4docx.docx.Placeholder.PlaceholderType;
import org.scriptlet4docx.docx.Placeholder.ScriptWraps;
import org.scriptlet4docx.docx.TemplateContent.ContentItem;
import org.scriptlet4docx.util.string.StringUtil;
import org.scriptlet4docx.util.xml.XMLUtils;

public class DocxTemplater {

    private File pathToDocx;
    private InputStream templateStream;
    private String streamTemplateKey;

    /**
     * Reads template content from file on file system.<br/>
     * Note that with this constructor implicit template caching occurs.<br/>
     * This mean if you change source template after first process() invocation,
     * result document will not reflect your changes. Use different file names
     * if you need no-cache behavior.
     * 
     * @param pathToDocx
     *            path to docx template. Would be read only once with first
     *            process invocation.
     */
    public DocxTemplater(File pathToDocx) {
        this.pathToDocx = pathToDocx;
    }

    /**
     * Reads template content from input stream.<br/>
     * TemplateKey is used for perfomance and caching. DocxTemplater caches
     * input stream content and associates it with given TemplateKey. When
     * multiple process() invocations occur with same templateKey, only the 1st
     * one will actually read stream content.
     * 
     * @param inputStream
     *            template binary stream to read from
     * @param templateKey
     *            unique identifier associated with given template. Should not
     *            contain special characters like '/' and be too long. This
     *            parameter is used for file system file path.
     */
    public DocxTemplater(InputStream inputStream, String templateKey) {
        this.templateStream = inputStream;
        this.streamTemplateKey = templateKey;
    }

    private static Pattern scriptPattern = Pattern.compile("((&lt;%=?(.*?)%&gt;)|\\$\\{(.*?)\\})", Pattern.DOTALL
            | Pattern.MULTILINE);

    TemplateContent cleanupTemplate(TemplateContent content) {
        List<ContentItem> items = new ArrayList<TemplateContent.ContentItem>();

        for (int i = 0; i < content.getItems().size(); i++) {
            items.add(new ContentItem(content.getItems().get(i).getIdentifier(), cleanupTemplate(content.getItems()
                    .get(i).getContent())));
        }
        return new TemplateContent(items);
    }

    String cleanupTemplate(String template) {
        template = DividedScriptWrapsProcessor.process(template);
        template = TableScriptingProcessor.process(template);
        return template;
    }

    TemplateContent processCleanedTemplate(TemplateContent content, Map<String, Object> params)
            throws CompilationFailedException, ClassNotFoundException, IOException {
        List<ContentItem> items = new ArrayList<TemplateContent.ContentItem>();

        for (int i = 0; i < content.getItems().size(); i++) {
            items.add(new ContentItem(content.getItems().get(i).getIdentifier(), processCleanedTemplate(content
                    .getItems().get(i).getContent(), params)));
        }
        return new TemplateContent(items);
    }

    String processCleanedTemplate(String template, Map<String, Object> params) throws CompilationFailedException,
            ClassNotFoundException, IOException {
        final String methodName = "processScriptedTemplate";

        String replacement = UUID.randomUUID().toString();

        List<Placeholder> scripts = new ArrayList<Placeholder>();

        Matcher m = scriptPattern.matcher(template);

        while (m.find()) {
            String scriptText = m.group(0);
            Placeholder ph = new Placeholder(UUID.randomUUID().toString(), scriptText, PlaceholderType.SCRIPT);

            if (ph.scriptWrap == ScriptWraps.DOLLAR_PRINT) {
                ph.setScriptTextNoWrap(m.group(4));
            } else if (ph.scriptWrap == ScriptWraps.SCRIPLET || ph.scriptWrap == ScriptWraps.SCRIPLET_PRINT) {
                ph.setScriptTextNoWrap(m.group(3));
            }

            scripts.add(ph);
        }

        String replacedScriptsTemplate = m.replaceAll(replacement);

        List<String> pieces = Arrays.asList(StringUtils.splitByWholeSeparatorPreserveAllTokens(replacedScriptsTemplate,
                replacement));

        if (pieces.size() != scripts.size() + 1) {
            throw new IllegalStateException(String.format(
                    "Programming bug was detected. Text pieces size does not match scripts size (%s, %s)."
                            + " Please report this as a bug to the library author.", pieces.size(), scripts.size()));
        }

        List<Placeholder> tplSkeleton = new ArrayList<Placeholder>();

        int i = 0;
        for (String piece : pieces) {
            tplSkeleton.add(new Placeholder(UUID.randomUUID().toString(), piece, PlaceholderType.TEXT));

            if (i < scripts.size()) {
                tplSkeleton.add(scripts.get(i));
            }
            i++;
        }

        StringBuilder builder = new StringBuilder();

        for (Placeholder placeholder : tplSkeleton) {
            if (PlaceholderType.SCRIPT == placeholder.type) {
                String cleanScriptNoWrap = XMLUtils.getNoTagsTrimText(placeholder.getScriptTextNoWrap());
                cleanScriptNoWrap = StringUtils.replaceEach(cleanScriptNoWrap, new String[] {"&amp;", "&gt;", "&lt;", "&quot;",
                        "«", "»", "“", "”", "‘", "’" }, new String[] {"&", ">", "<", "\"", "\"", "\"", "\"", "\"", "\"",
                        "\"" });
                if (placeholder.scriptWrap == ScriptWraps.DOLLAR_PRINT
                        || placeholder.scriptWrap == ScriptWraps.SCRIPLET_PRINT) {
                    cleanScriptNoWrap = NULL_REPLACER_REF + "(" + cleanScriptNoWrap + ")";
                }
                String script = placeholder.constructWithCurrentScriptWrap(cleanScriptNoWrap);
                builder.append(script);
            } else {
                builder.append(placeholder.ph);
            }
        }

        template = builder.toString();

        params.put(UTIL_FUNC_HOLDER, this);

        if (logger.isLoggable(Level.FINEST)) {
            logger.logp(Level.FINEST, CLASS_NAME, methodName, String.format("\ntemplate = \n%s\n", template));
        }

        GStringTemplateEngine engine1 = new GStringTemplateEngine();
        String scriptAppliedStr;
        try {
            scriptAppliedStr = String.valueOf(engine1.createTemplate(template).make(params));
        } catch (Throwable e) {
            logger.logp(Level.SEVERE, CLASS_NAME, methodName,
                    String.format("Cannot process template: [%s].", template), e);
            throw new RuntimeException(e);
        }

        scriptAppliedStr = StringUtil.escapeSimpleSet(scriptAppliedStr);

        String result = scriptAppliedStr;
        for (Placeholder placeholder : tplSkeleton) {
            if (PlaceholderType.TEXT == placeholder.type) {
                result = StringUtils.replace(result, placeholder.ph, placeholder.text);
            }
        }

        return result;
    }

    static String CLASS_NAME = DocxTemplater.class.getCanonicalName();
    static Logger logger = Logger.getLogger(CLASS_NAME);

    String setupTemplate() throws IOException {
        String templateKey = null;
        if (pathToDocx != null) {
            // this is file-base usage
            // TODO what if hash collision? A longer hash algorithm may be
            // needed.
            templateKey = pathToDocx.hashCode() + "-" + FilenameUtils.getBaseName(pathToDocx.getName());
            if (!TemplateFileManager.getInstance().isPrepared(templateKey)) {
                TemplateFileManager.getInstance().prepare(pathToDocx, templateKey);
            }
        } else {
            // this is stream-based usage
            try {
                templateKey = streamTemplateKey;
                if (!TemplateFileManager.getInstance().isTemplateFileFromStreamExists(templateKey)) {
                    TemplateFileManager.getInstance().saveTemplateFileFromStream(templateKey, templateStream);
                    TemplateFileManager.getInstance().prepare(
                            TemplateFileManager.getInstance().getTemplateFileFromStream(templateKey), templateKey);
                }
            } finally {
                IOUtils.closeQuietly(templateStream);
            }

        }
        return templateKey;
    }

    /**
     * Process template with the given params and return output stream as
     * result.
     */
    public InputStream processAndReturnInputStream(Map<String, Object> params) {
        File tmpResFile = TemplateFileManager.getInstance().getUniqueOutStreamFile();
        process(tmpResFile, params);
        try {
            return new DeleteOnCloseFileInputStream(tmpResFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Process template with the given params and writes result as output
     * stream.<br/>
     * Note that stream will be closed automatically.
     */
    public void process(OutputStream outputStream, Map<String, Object> params) {
        try {
            InputStream inputStream = null;
            try {
                inputStream = processAndReturnInputStream(params);
                IOUtils.copy(inputStream, outputStream);
            } finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Process template with the given params and save result as a docx file.
     */
    public void process(File destDocx, Map<String, Object> params) {
        try {
            String templateKey = setupTemplate();

            TemplateContent tCont = TemplateFileManager.getInstance().getTemplateContent(templateKey);

            if (!TemplateFileManager.getInstance().isPreProcessedTemplateExists(templateKey)) {
                tCont = cleanupTemplate(tCont);
                TemplateFileManager.getInstance().savePreProcessed(templateKey, tCont);
            }

            tCont = processCleanedTemplate(tCont, params);
            processResult(destDocx, templateKey, tCont);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void processResult(File destDocx, String templateKey, TemplateContent content) throws IOException {
        File tmpProcessFolder = TemplateFileManager.getInstance().createTmpProcessFolder();

        destDocx.delete();
        FileUtils.deleteDirectory(tmpProcessFolder);

        FileUtils
                .copyDirectory(TemplateFileManager.getInstance().getTemplateUnzipFolder(templateKey), tmpProcessFolder);

        for (ContentItem item : content.getItems()) {
            FileUtils.writeStringToFile(new File(tmpProcessFolder, "word/" + item.getIdentifier()), item.getContent(),
                    "UTF-8");
        }

        AntBuilder antBuilder = new AntBuilder();
        HashMap<String, Object> params1 = new HashMap<String, Object>();
        params1.put("destfile", destDocx);
        params1.put("basedir", tmpProcessFolder);
        params1.put("includes", "**/*.*");
        params1.put("excludes", "");
        params1.put("encoding", "UTF-8");
        antBuilder.invokeMethod("zip", params1);

        FileUtils.deleteDirectory(tmpProcessFolder);
    }

    /**
     * Cleans up templater temporary folder.<br/>
     * Normally should be called when application is about to end its execution.
     */
    public static void cleanup() {
        try {
            TemplateFileManager.getInstance().cleanup();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    final static String UTIL_FUNC_HOLDER = "__docxTemplaterInstance";
    final static String NULL_REPLACER_REF = UTIL_FUNC_HOLDER + ".replaceIfNull";

    @SuppressWarnings("unused")
    private String replaceIfNull(Object o) {
        return o == null ? nullReplacement : String.valueOf(o);
    }

    private String nullReplacement = "";

    /**
     * 
     * @param nullReplacement
     *            When scriptlet output is null this value take it's place.<br />
     *            Useful when you want nothing to be printed, or custom value
     *            like "UNKNOWN".
     */
    public void setNullReplacement(String nullReplacement) {
        this.nullReplacement = nullReplacement;
    }

}
