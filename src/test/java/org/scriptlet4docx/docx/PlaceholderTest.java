package org.scriptlet4docx.docx;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;
import org.scriptlet4docx.docx.Placeholder.PlaceholderType;

public class PlaceholderTest {

	@Test
	public void testConstructWithCurrentScriptWrap() {
		Placeholder ph = new Placeholder(UUID.randomUUID().toString(),
				"${foo.bar}", PlaceholderType.SCRIPT);
		
		assertEquals("${foo.bar}", ph.constructWithCurrentScriptWrap("foo.bar"));
		
		ph = new Placeholder(UUID.randomUUID().toString(),
				"&lt;%=foo.bar%&gt;", PlaceholderType.SCRIPT);
		
		assertEquals("<%=foo.bar%>", ph.constructWithCurrentScriptWrap("foo.bar"));
		
		ph = new Placeholder(UUID.randomUUID().toString(),
				"&lt;% foo.bar() %&gt;", PlaceholderType.SCRIPT);
		
		assertEquals("<% foo.bar() %>", ph.constructWithCurrentScriptWrap(" foo.bar() "));
	}

}
