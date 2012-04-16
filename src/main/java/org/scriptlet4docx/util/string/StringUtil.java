package org.scriptlet4docx.util.string;

import java.util.List;

import com.google.common.base.Splitter;

public class StringUtil {
	public static String replaceOneByOne(String inText, String replaceStr,
			List<String> replacements) {
		Iterable<String> pieces = Splitter.on(replaceStr).split(inText);

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
}
