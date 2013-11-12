package net.isger.brick.raw;

public interface Artifact {

    public Seed getSeed();

    public Object use(String id, Object... args);

}
