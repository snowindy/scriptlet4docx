package org.scriptlet4docx.util.string;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {
	public static String replaceOneByOne(String inText, String replaceStr,
			List<String> replacements) {
	    String[] pieces = StringUtils.splitByWholeSeparator(inText, replaceStr);

		StringBuilder body = new StringBuilder(400);
		int idx = 0;
		for (String piece : pieces) {
			body.append(piece);
			if (idx < replacements.size()) {
				body.append(replacements.get(idx));
			}
			idx++;
		}

		return body.toString();
	}
	
	/**
	 * Escapes only &,<,>
	 */
	public static String escapeSimpleSet(String inText) {
	    inText = inText.replaceAll("&", "&amp;");
	    inText = inText.replaceAll("<", "&lt;");
	    inText = inText.replaceAll(">", "&gt;");
	    return inText;
	}
}
