package org.scriptlet4docx.docx;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scriptlet4docx.util.string.StringUtil;
import org.scriptlet4docx.util.xml.XMLUtils;

class TableScriptingCleanProcessor {

    private static Pattern rowCleanPattern = Pattern.compile("\\$\\[(.*?)\\]", Pattern.DOTALL | Pattern.MULTILINE);

    static String process(String wholeRow) {
        Matcher m1 = rowCleanPattern.matcher(wholeRow);

        String ph = UUID.randomUUID().toString();

        String cleanTr = null;
        List<String> replacements = new ArrayList<String>();

        while (m1.find()) {
            String dirtyScript = m1.group(1);
            String cleanScript = XMLUtils.getNoTagsTrimText(dirtyScript);

            replacements.add(String.format("$[%s]", cleanScript));
        }

        cleanTr = m1.replaceAll(ph);

        cleanTr = StringUtil.replaceOneByOne(cleanTr, ph, replacements);

        return cleanTr;
    }
}
