package org.scriptlet4docx.docx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.scriptlet4docx.util.string.StringUtil;

class TableScriptingRowProcessor {
    private static Pattern groovyFormPattern = Pattern
            .compile("\\$\\[((.*?)(@\\w+).*?)\\]", Pattern.DOTALL | Pattern.MULTILINE);

    private boolean iterStatusDefined = false;

    String process(String trRowStr) {
        Matcher m = groovyFormPattern.matcher(trRowStr);

        List<String> scripts = new ArrayList<String>();
        String placeholder = UUID.randomUUID().toString();

        Set<String> vars = new HashSet<String>();
        Set<String> prefixes = new HashSet<String>();
        String varPref = null;
        String varName = null;
        while (m.find()) {
            int initVarPrefLen = 0;
            String funcPref = "";
            String varToReplace = null;
            varPref = StringUtils.defaultString(m.group(2));
            initVarPrefLen = varPref.length();
            if (StringUtils.isNotBlank(varPref)) {
                if (StringUtils.countMatches(varPref, "(") > StringUtils.countMatches(varPref, ")")) {
                    int idx = varPref.lastIndexOf("(");
                    funcPref = varPref.substring(0, idx + 1);
                    varPref = varPref.substring(idx + 1);
                }
            }

            varToReplace = StringUtils.defaultString(m.group(3));
            if (varName == null) {
                varName = varToReplace.replace("@", "");
            }

            vars.add(varToReplace);
            prefixes.add(varPref);

            String sc = m.group(1);
            sc = sc.substring(initVarPrefLen);
            sc = StringUtils.replaceOnce(sc, varToReplace, funcPref + varName);
            sc = String.format("${%s}", sc);
            scripts.add(sc);
        }

        if (vars.size() == 0) {
            throw new TemplateVarException("There must at least one use of @-variable in $[...] block.");
        }
        if (vars.size() > 1) {
            throw new TemplateVarException("Only one @-variable is allowed in $[...] block.");
        }
        if (prefixes.size() > 1) {
            throw new TemplateVarException("Only one @-variable prefix is allowed in $[...] block.");
        }

        String noScriptsTr = m.replaceAll(placeholder);

        String body = StringUtil.replaceOneByOne(noScriptsTr, placeholder, scripts);

        String wrapTop = null;
        String iterStatus = iterStatusDefined ? "iterStatus=0;" : "def iterStatus=0;";
        iterStatusDefined = true;

        if (StringUtils.isBlank(varPref)) {
            wrapTop = String.format("&lt;%% %s for ( %s in %sList ) { iterStatus++; %%&gt;", iterStatus, varName,
                    varName);
        } else {
            wrapTop = String.format("&lt;%% %s for ( %s in %s%s ) { iterStatus++; %%&gt;", iterStatus, varName,
                    varPref, varName);
        }

        String res = wrapTop + body + "&lt;% }; %&gt;";

        return res;

    }
}
