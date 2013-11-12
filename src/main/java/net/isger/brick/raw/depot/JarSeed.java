package net.isger.brick.raw.depot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.isger.brick.raw.Seed;

public class JarSeed implements Seed {

    private File file;

    private JarEntry entry;

    public JarSeed(File file, JarEntry entry) {
        this.file = file;
        this.entry = entry;
    }

    public Object getEntity() {
        return new Object[] { file, entry };
    }

    public InputStream getInputStream() throws IOException {
        return new ZipInputStream(new FileInputStream(file));
    }

    public OutputStream getOutputStream() throws IOException {
        return new ZipOutputStream(new FileOutputStream(file));
    }

}
