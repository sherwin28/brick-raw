package net.isger.brick.raw;

public interface Depot {

    public void mount(String path);

    public Seed seek(String name);

    public Artifact wrap(String name);

    public Artifact wrap(Seed seed);

    public void unmount(String path);

}
