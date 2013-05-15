package org.scriptlet4docx.docx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class DeleteOnCloseFileInputStream extends FileInputStream {

    public DeleteOnCloseFileInputStream(File file) throws FileNotFoundException {
        super(file);
        this.f = file;
    }

    private File f;

    @Override
    public void close() throws IOException {
        super.close();
        FileUtils.deleteQuietly(f);
    }
}
