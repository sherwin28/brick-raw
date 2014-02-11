package net.isger.brick.raw.depot;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import net.isger.brick.raw.Artifact;
import net.isger.brick.raw.Seed;
import net.isger.brick.util.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarDepot extends AbstractDepot {

    private static final Logger LOG;

    private Vector<File> paths;

    static {
        LOG = LoggerFactory.getLogger(JarDepot.class);
    }

    public JarDepot() {
        paths = new Vector<File>();
        addLabel(LAB_CLASSPATH);
        addLabel(LAB_JAR);
        addLabel(LAB_FILE);
    }

    public void mount(URL url) {
        mount(url.getFile());
    }

    public void mount(String path) {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            path = file.getAbsolutePath();
            if (!paths.contains(file) && Files.isJar(path)) {
                LOG.info("Mount the resource location {}", path);
                paths.add(file);
            }
        }
    }

    public Seed seek(String info) {
        Seed seed = null;
        JarInputStream jis = null;
        JarEntry je = null;
        for (File path : paths) {
            try {
                jis = Files.openJarIS(path.getAbsolutePath());
                je = Files.search(jis, info);
                if (je != null) {
                    seed = new JarSeed(path, je);
                    break;
                }
            } catch (IOException e) {
            } finally {
                Files.close(jis);
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
