package edu.pku.sei.tsr.snowgraph.api;

public interface PostInitRegistry {
    void registerDataPath(String path, boolean watch);
}
