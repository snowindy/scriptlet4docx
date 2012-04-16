package org.scriptlet4docx.util.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLUtils {

	private static Pattern tagPattern = Pattern.compile("<[^<>]*>", Pattern.DOTALL
			| Pattern.MULTILINE);

	public static String getNoTagsTrimText(String xml) {
		Matcher m = tagPattern.matcher(xml);
		if (!m.find()) {
			return xml;
		}
		String replaced = m.replaceAll("");

		replaced = replaced.replaceAll("\\r\\n", "");
		replaced = replaced.replaceAll("\\n", "");
		replaced = replaced.replaceAll("\\t", "");
		
		replaced = replaced.replaceAll("\\s+", " ");
		replaced = replaced.trim();

		return replaced;
	}
}
