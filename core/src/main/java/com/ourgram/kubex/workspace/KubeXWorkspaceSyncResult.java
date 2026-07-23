package com.ourgram.kubex.workspace;

import java.nio.file.Path;
import java.util.List;

public record KubeXWorkspaceSyncResult(
    boolean success,
    Path workspaceRoot,
    List<Path> publishedFiles,
    int sourceMapCount,
    String message
) {
}
