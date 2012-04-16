package org.scriptlet4docx.util.xml;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.scriptlet4docx.util.xml.XMLUtils;
import org.junit.Test;

import org.scriptlet4docx.util.test.TestUtils;

public class XMLUtilsTest {

	@Test
	public void testGetNoTagsTrimText() throws IOException {
		String xml = TestUtils.readResource("/util/xml/XMLUtilsTest-1.xml");

		String result = XMLUtils.getNoTagsTrimText(xml);

		assertTrue(result != null);
		assertTrue(result.equals("${ contract .number }"));
	}

	@Test
	public void testGetNoTagsTrimText_preserveSpaces() throws IOException {
		String xml = TestUtils.readResource("/util/xml/XMLUtilsTest-2.xml");

		String result = XMLUtils.getNoTagsTrimText(xml);

		assertTrue(result != null);
		assertTrue(result.equals("out.print('like dogs');"));
	}
}
