package com.ourgram.kubex.workspace;

import java.nio.file.Path;

public record KubeXWorkspaceBuildResult(
    boolean success,
    Path workspaceRoot,
    String message
) {
}
