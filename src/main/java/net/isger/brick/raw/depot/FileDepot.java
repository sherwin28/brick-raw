package net.isger.brick.raw.depot;

import java.io.File;
import java.net.URL;
import java.util.Vector;

import net.isger.brick.raw.Artifact;
import net.isger.brick.raw.Seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileDepot extends AbstractDepot {

    private static final Logger LOG;

    private Vector<File> paths;

    static {
        LOG = LoggerFactory.getLogger(FileDepot.class);
    }

    public FileDepot() {
        paths = new Vector<File>();
        addLabel(LAB_CLASSPATH);
        addLabel(LAB_FILE);
    }

    public void mount(URL url) {
        mount(url.getFile());
    }

    public void mount(String path) {
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            if (!paths.contains(file)) {
                LOG.info("Mount the resource location {}",
                        file.getAbsolutePath());
                paths.add(file);
            }
        }
    }

    public Seed seek(String info) {
        Seed seed = null;
        File file = null;
        for (File path : paths) {
            file = new File(path, info);
            if (file.exists() && file.isFile()) {
                seed = new FileSeed(file);
                break;
            }
        }
        return seed;
    }

    public Artifact wrap(String info) {
        return null;
    }

    public Artifact wrap(Seed seed) {
        return null;
    }

    public void unmount(String path) {
    }

    public void unmount(URL url) {
    }

}
