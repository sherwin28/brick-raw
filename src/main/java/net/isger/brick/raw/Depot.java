package net.isger.brick.raw;

import java.net.URL;

public interface Depot {

    public static final String LAB_CLASSPATH = "java.class.path";

    public static final String LAB_FILE = "file";

    public static final String LAB_JAR = "jar";

    public boolean isSupport(String label);

    public void mount(URL url);

    public void mount(String res);

    public Seed seek(String info);

    public Artifact wrap(String res);

    public Artifact wrap(Seed seed);

    public void unmount(String res);

    public void unmount(URL url);

}
