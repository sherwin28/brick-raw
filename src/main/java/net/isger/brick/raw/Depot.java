package net.isger.brick.raw;

public interface Depot {

    public static final String LAB_CLASSPATH = "java.class.path";

    public static final String LAB_FILE = "file";

    public static final String LAB_JAR = "jar";

    public boolean isSupport(String label);

    public void mount(String path);

    public Seed seek(String info);

    public Artifact wrap(String info);

    public Artifact wrap(Seed seed);

    public void unmount(String path);

}
