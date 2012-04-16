package org.scriptlet4docx.docx;

class Placeholder {
	String ph, text;
	int type;
	ScriptWraps scriptWrap;
	String scriptTextNoWrap;

	public void setScriptTextNoWrap(String scriptTextNoWrap) {
		this.scriptTextNoWrap = scriptTextNoWrap;
	}

	static int SCRIPT = 1;
	static int TEXT = 0;

	public String getScriptTextNoWrap() {
		return scriptTextNoWrap;
	}

	public String constructWithCurrentScriptWrap(String scriptTextNoWrap) {
		String format = null;
		if (scriptWrap == ScriptWraps.DOLLAR_PRINT) {
			format = "${%s}";
		} else if (scriptWrap == ScriptWraps.SCRIPLET) {
			format = "<%%%s%%>";
		} else if (scriptWrap == ScriptWraps.SCRIPLET_PRINT) {
			format = "<%%=%s%%>";
		} else {
			throw new RuntimeException(String.format(
					"ScriptWrap is undefined: %s", scriptWrap));
		}

		return String.format(format, scriptTextNoWrap);
	}

	public Placeholder(String ph, String text, int type) {
		this.ph = ph;
		this.text = text;
		this.type = type;

		if (this.type == SCRIPT) {
			detectScriptWrap();
		}
	}

	private void detectScriptWrap() {
		String noSpaces = text.replaceAll("\\s", "");
		if (noSpaces.startsWith("$")) {
			scriptWrap = ScriptWraps.DOLLAR_PRINT;
		} else if (noSpaces.startsWith("&lt;%=")) {
			scriptWrap = ScriptWraps.SCRIPLET_PRINT;
		} else if (noSpaces.startsWith("&lt;%")) {
			scriptWrap = ScriptWraps.SCRIPLET;
		} else {
			throw new IllegalArgumentException(String.format(
					"Script wrap cannot be detected: [%s]", text));
		}
	}

	static enum ScriptWraps {
		DOLLAR_PRINT, // ${}
		SCRIPLET, // <%%>
		SCRIPLET_PRINT // <%=%>
	}
}