package net.isger.brick.raw.depot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.isger.brick.raw.Seed;

public class FileSeed implements Seed {

    private File file;

    public FileSeed(File file) {
        this.file = file;
    }

    public Object getEntity() {
        return file;
    }

    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(file);
    }

}
