package org.scriptlet4docx.docx;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.util.StringUtils;

class DividedScriptWrapsProcessor {

	static String process(String template) {
		template = processCommon(template, dollarPattern, "${");
		template = processCommon(template, ltPattern, "&lt;%");
		template = processCommon(template, ltOutPattern, "&lt;%=");	
		template = processCommon(template, gtPattern, "%&gt;");

		return template;
	}

	private static String processCommon(String template, Pattern pattern, String replacer) {
		Matcher m = pattern.matcher(template);
		String noMatchingParts = m.replaceAll(placeholder);
		String res = StringUtils.replace(noMatchingParts, placeholder, replacer);
		return res;
	}

	private static String	placeholder		= UUID.randomUUID().toString();

	private static String ANY_TAG = "\\s*(<[^<>]*>\\s*)*";
	
	private static Pattern	dollarPattern	= Pattern.compile("\\$"+ANY_TAG+"\\{", Pattern.DOTALL | Pattern.MULTILINE);

	private static Pattern	ltPattern		= Pattern.compile("&lt;"+ANY_TAG+"%", Pattern.DOTALL | Pattern.MULTILINE);
	
	private static Pattern	gtPattern		= Pattern.compile("%"+ANY_TAG+"\\&gt;", Pattern.DOTALL | Pattern.MULTILINE);
	
	private static Pattern	ltOutPattern		= Pattern.compile("&lt;%"+ANY_TAG+"=", Pattern.DOTALL | Pattern.MULTILINE);
}
