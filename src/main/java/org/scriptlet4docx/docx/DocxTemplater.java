package org.scriptlet4docx.docx;

import groovy.text.GStringTemplateEngine;
import groovy.util.AntBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.scriptlet4docx.docx.Placeholder.ScriptWraps;
import org.scriptlet4docx.util.string.StringUtil;
import org.scriptlet4docx.util.xml.XMLUtils;

public class DocxTemplater {

    private File pathToExtractedDocxFolder;
    private File pathToDocx;
    private File contentXmlFile;
    private static final String PATH_TO_CONTENT = "word/document.xml";

    public DocxTemplater(String pathToDocx) {
        this(new File(pathToDocx));
    }

    public DocxTemplater(File pathToDocx) {
        this.pathToDocx = pathToDocx;
    }

    public void process(String destDocx, Map<String, Object> params) {
        process(new File(destDocx), params);
    }

    private boolean useCache = true;
    private boolean extractToCurrentDocxDir;

    private static String cacheUnzipDirsSuffix = UUID.randomUUID().toString();

    private void setup() throws IOException {
        if (pathToDocx.isFile()) {
            if (useCache && extractToCurrentDocxDir) {
                pathToExtractedDocxFolder = new File(pathToDocx.getParentFile(), FilenameUtils.getExtension(pathToDocx
                        .getName()));
            } else {
                String suffix = useCache ? cacheUnzipDirsSuffix : UUID.randomUUID().toString();

                String tmpDir = System.getProperty("java.io.tmpdir");
                File dir = new File(tmpDir, FilenameUtils.getBaseName(pathToDocx.getName()) + suffix);
                pathToExtractedDocxFolder = dir;
            }

            if (!pathToExtractedDocxFolder.exists()) {
                AntBuilder antBuilder = new AntBuilder();
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("src", pathToDocx);
                params.put("dest", pathToExtractedDocxFolder);
                params.put("overwrite", "true");
                antBuilder.invokeMethod("unzip", params);
            }
        } else {
            pathToExtractedDocxFolder = pathToDocx;
        }

        contentXmlFile = new File(pathToExtractedDocxFolder, PATH_TO_CONTENT);
    }

    static Pattern scriptPattern = Pattern.compile("((&lt;%=?(.*?)%&gt;)|\\$\\{(.*?)\\})", Pattern.DOTALL
            | Pattern.MULTILINE);

    static String processScriptedTemplate(String template, Map<String, ? extends Object> params)
            throws CompilationFailedException, ClassNotFoundException, IOException {
        final String methodName = "processScriptedTemplate";

        template = DividedScriptWrapsProcessor.process(template);
        template = TableScriptingProcessor.process(template);

        String replacement = UUID.randomUUID().toString();

        List<Placeholder> scripts = new ArrayList<Placeholder>();

        Matcher m = scriptPattern.matcher(template);

        while (m.find()) {
            String scriptText = m.group(0);
            Placeholder ph = new Placeholder(UUID.randomUUID().toString(), scriptText, Placeholder.SCRIPT);

            if (ph.scriptWrap == ScriptWraps.DOLLAR_PRINT) {
                ph.setScriptTextNoWrap(m.group(4));
            } else if (ph.scriptWrap == ScriptWraps.SCRIPLET || ph.scriptWrap == ScriptWraps.SCRIPLET_PRINT) {
                ph.setScriptTextNoWrap(m.group(3));
            }

            scripts.add(ph);
        }

        String replacedScriptsTemplate = m.replaceAll(replacement);

        String[] pieces = StringUtils.splitByWholeSeparator(replacedScriptsTemplate, replacement);

        List<Placeholder> tplSkeleton = new ArrayList<Placeholder>();

        int i = 0;
        for (String piece : pieces) {
            tplSkeleton.add(new Placeholder(UUID.randomUUID().toString(), piece, Placeholder.TEXT));

            if (i < scripts.size()) {
                tplSkeleton.add(scripts.get(i));
            }
            i++;
        }

        StringBuilder builder = new StringBuilder();

        for (Placeholder placeholder : tplSkeleton) {
            if (Placeholder.SCRIPT == placeholder.type) {
                String cleanScriptNoWrap = XMLUtils.getNoTagsTrimText(placeholder.getScriptTextNoWrap());
                String script = placeholder.constructWithCurrentScriptWrap(cleanScriptNoWrap);
                builder.append(script);
            } else {
                builder.append(placeholder.ph);
            }
        }

        template = builder.toString();

        if (logger.isLoggable(Level.FINEST)) {
            logger.logp(Level.FINEST, CLASS_NAME, methodName, String.format("\ntemplate = \n%s\n", template));
        }

        // TODO add already processed template caching
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
            if (Placeholder.TEXT == placeholder.type) {
                result = StringUtils.replace(result, placeholder.ph, placeholder.text);
            }
        }

        return result;
    }

    static String CLASS_NAME = DocxTemplater.class.getCanonicalName();
    static Logger logger = Logger.getLogger(CLASS_NAME);

    public File process(File destDocx, Map<String, Object> params) {
        try {
            setup();

            if (!contentXmlFile.exists()) {
                throw new FileNotFoundException(contentXmlFile.getAbsolutePath());
            }

            String template = FileUtils.readFileToString(contentXmlFile, "UTF-8");

            String result = processScriptedTemplate(template, params);

            String destDocxString = destDocx.getPath();

            String noExtPathString = destDocxString.substring(0, destDocxString.lastIndexOf("."));
            File noExtPath = new File(noExtPathString);

            destDocx.delete();
            FileUtils.deleteDirectory(noExtPath);

            FileUtils.copyDirectory(pathToExtractedDocxFolder, noExtPath);

            FileUtils.writeStringToFile(new File(noExtPath, PATH_TO_CONTENT), result, "UTF-8");

            AntBuilder antBuilder = new AntBuilder();
            HashMap<String, Object> params1 = new HashMap<String, Object>();
            params1.put("destfile", destDocx);
            params1.put("basedir", noExtPath);
            params1.put("includes", "**/*.*");
            params1.put("excludes", "");
            params1.put("encoding", "UTF-8");
            antBuilder.invokeMethod("zip", params1);

            return noExtPath;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void cleanup(File file) {
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isUseCache() {
        return useCache;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    public boolean isExtractToCurrentDocxDir() {
        return extractToCurrentDocxDir;
    }

    public void setExtractToCurrentDocxDir(boolean extractToCurrentDocxDir) {
        this.extractToCurrentDocxDir = extractToCurrentDocxDir;
    }

}
