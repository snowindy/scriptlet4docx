package org.scriptlet4docx.util.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class TestUtils {
	public static String readResource(String path) {
		InputStream in = TestUtils.class.getResourceAsStream(path);
		try {
			return IOUtils.toString(in, "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(in);
		}

	}

	public static void copyResourceToFile(String path, File outFile) {
		try {
			FileOutputStream out = new FileOutputStream(outFile);
			InputStream in = TestUtils.class.getResourceAsStream(path);
			try {
				IOUtils.copy(in, out);
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
