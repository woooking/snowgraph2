package edu.pku.sei.tsr.snowgraph.api.event;

import java.nio.file.Path;

public interface ShutDownEvent {
    Path getConfigDirPath();
}
