package org.scriptlet4docx.docx;

import static org.junit.Assert.*;

import java.util.UUID;

import org.scriptlet4docx.docx.Placeholder;
import org.junit.Test;

public class PlaceholderTest {

	@Test
	public void testConstructWithCurrentScriptWrap() {
		Placeholder ph = new Placeholder(UUID.randomUUID().toString(),
				"${foo.bar}", Placeholder.SCRIPT);
		
		assertEquals("${foo.bar}", ph.constructWithCurrentScriptWrap("foo.bar"));
		
		ph = new Placeholder(UUID.randomUUID().toString(),
				"&lt;%=foo.bar%&gt;", Placeholder.SCRIPT);
		
		assertEquals("<%=foo.bar%>", ph.constructWithCurrentScriptWrap("foo.bar"));
		
		ph = new Placeholder(UUID.randomUUID().toString(),
				"&lt;% foo.bar() %&gt;", Placeholder.SCRIPT);
		
		assertEquals("<% foo.bar() %>", ph.constructWithCurrentScriptWrap(" foo.bar() "));
	}

}
