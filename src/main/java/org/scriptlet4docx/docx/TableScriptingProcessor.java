package org.scriptlet4docx.docx;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scriptlet4docx.util.string.StringUtil;


class TableScriptingProcessor {

    private static Pattern	tablePreProcessPattern	= Pattern.compile("<w:tr\\s(?:(?!/?<w:tr\\s).)*(\\$\\[(.*?)\\]).*?</w:tr>",
													Pattern.DOTALL | Pattern.MULTILINE);

	static String process(String template) {
		Matcher m = tablePreProcessPattern.matcher(template);

		List<String> scripts = new ArrayList<String>();
		String placeholder = UUID.randomUUID().toString();

		TableScriptingRowProcessor tableScriptingRowProcessor = new TableScriptingRowProcessor();

		while (m.find()) {
			String wholeTr = m.group(0);

			String cleanTrRow = TableScriptingCleanProcessor.process(wholeTr);

			String trGroovyForm = tableScriptingRowProcessor.process(cleanTrRow);

			scripts.add(trGroovyForm);
		}

		String noScriptsText = m.replaceAll(placeholder);

		String scriptInsertedText = StringUtil.replaceOneByOne(noScriptsText, placeholder, scripts);
		;

		return scriptInsertedText;
	}
}
