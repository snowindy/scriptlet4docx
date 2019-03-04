package org.scriptlet4docx.docx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.scriptlet4docx.docx.Placeholder.PlaceholderType;
import org.scriptlet4docx.docx.Placeholder.ScriptWraps;
import org.scriptlet4docx.util.string.StringUtil;
import org.scriptlet4docx.util.xml.XMLUtils;

/**
 * 
 * Extend DocxTemplate to give you the ability to process .docx with many more scripts.
 * Eliminating the problem of "Method code too large!" Do not confuse with
 * {@link groovy.text.StreamingTemplateEngine}
 * 
 * <p>Use: </p> Each N number of scriptlets in your .docx insert a mark "&lt;!=BREAK!&gt;"
 * This will tell the engine that it can execute the scripts up to that point.
 * Then, it will continue processing until the next &lt;!=BREAK!&gt;
 * 
 * @author ortizman@gmail.com
 * @author manuel.ortiz@fluxit.com.ar
 *
 */
public class DocxTemplaterBulk extends DocxTemplater {

	final static String UTIL_FUNC_HOLDER = "__docxTemplaterInstance";
	final static String NULL_REPLACER_REF = UTIL_FUNC_HOLDER + ".replaceIfNull";
	private static String NEW_LINE_PLACEHOLDER = "26f679ad-e7fd-4d42-9e05-946f393c277d";

	@SuppressWarnings("unused")
	private String replaceIfNull(Object o) {
		return o == null ? nullReplacement : String.valueOf(o);
	}

    /**
     * 
     * @param nullReplacement
     *            When scriptlet output is null this value take its place.<br />
     *            Useful when you want nothing to be printed, or custom value
     *            like "UNKNOWN".
     */
    public void setNullReplacement(String nullReplacement) {
        this.nullReplacement = nullReplacement;
    }
    
	private String nullReplacement = "";

	private static Pattern scriptPattern = Pattern.compile(
			"((&lt;%=?(.*?)%&gt;)|\\$\\{(.*?)\\}|(&lt;\\!=?(.*?)\\!&gt;))", Pattern.DOTALL | Pattern.MULTILINE);

	public DocxTemplaterBulk(File pathToDocx) {
		super(pathToDocx);
	}

	public DocxTemplaterBulk(InputStream inputStream, String templateKey) {
		super(inputStream, templateKey);
	}

	@Override
	protected String processCleanedTemplate(String template, Map<String, Object> params)
			throws CompilationFailedException, ClassNotFoundException, IOException {

		params = processParams(params);

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
			} else if (ph.scriptWrap == ScriptWraps.BREAK) {
				ph.setScriptTextNoWrap("");
			}

			scripts.add(ph);
		}

		String replacedScriptsTemplate = m.replaceAll(replacement);

		List<String> pieces = Arrays
				.asList(StringUtils.splitByWholeSeparatorPreserveAllTokens(replacedScriptsTemplate, replacement));

		if (pieces.size() != scripts.size() + 1) {
			throw new IllegalStateException(
					String.format(
							"Programming bug was detected. Text pieces size does not match scripts size (%s, %s)."
									+ " Please report this as a bug to the library author.",
							pieces.size(), scripts.size()));
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
		StringBuilder partialResult = new StringBuilder();

		for (Placeholder placeholder : tplSkeleton) {
			if (PlaceholderType.SCRIPT == placeholder.type) {

				String cleanScriptNoWrap = XMLUtils.getNoTagsTrimText(placeholder.getScriptTextNoWrap());
				cleanScriptNoWrap = StringUtils.replaceEach(cleanScriptNoWrap,
						new String[] { "&amp;", "&gt;", "&lt;", "&quot;", "«", "»", "“", "”", "‘", "’" },
						new String[] { "&", ">", "<", "\"", "\"", "\"", "\"", "\"", "\"", "\"" });

				cleanScriptNoWrap = cleanScriptNoWrap.trim();
				// Replacing missing replacements, at least on top level
				if (cleanScriptNoWrap.matches("\\w+")) {
					if (!params.containsKey(cleanScriptNoWrap)) {
						params.put(cleanScriptNoWrap, null);
					}
				}

				if (placeholder.scriptWrap == ScriptWraps.DOLLAR_PRINT
						|| placeholder.scriptWrap == ScriptWraps.SCRIPLET_PRINT
						|| placeholder.scriptWrap == ScriptWraps.BREAK) {
					cleanScriptNoWrap = NULL_REPLACER_REF + "(" + cleanScriptNoWrap + ")";
				}
				String script = placeholder.constructWithCurrentScriptWrap(cleanScriptNoWrap);
				builder.append(script);

				if (placeholder.scriptWrap == ScriptWraps.BREAK) {
					partialResult.append(processPartialTemplate(builder.toString(), params));
					builder.setLength(0); // reset builder
				}

			} else {
				builder.append(placeholder.ph);
			}
		}

		String scriptAppliedStr;

		if (builder != null && builder.length() > 0) {
			partialResult.append(processPartialTemplate(builder.toString(), params));
		}
		scriptAppliedStr = partialResult.toString();

		scriptAppliedStr = StringUtil.escapeSimpleSet(scriptAppliedStr);

		scriptAppliedStr = StringUtils.replace(scriptAppliedStr, NEW_LINE_PLACEHOLDER, "<w:br/>");

		String result = scriptAppliedStr;
		for (Placeholder placeholder : tplSkeleton) {
			if (PlaceholderType.TEXT == placeholder.type) {
				result = StringUtils.replace(result, placeholder.ph, placeholder.text);
			}
		}

		return result;
	}

	private String processPartialTemplate(String template, Map<String, Object> params) {
		final String methodName = "processPartialTemplate";

		params.put(UTIL_FUNC_HOLDER, this);

		if (logger.isLoggable(Level.FINEST)) {
			logger.logp(Level.FINEST, CLASS_NAME, methodName, String.format("\ntemplate = \n%s\n", template));
		}

		try {
			return String.valueOf(getTemplateEngine().createTemplate(template).make(params));
		} catch (Throwable e) {
			logger.logp(Level.SEVERE, CLASS_NAME, methodName, String.format("Cannot process template: [%s].", template),
					e);
			throw new RuntimeException(e);
		}
	}

}
