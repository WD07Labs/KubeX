package com.ourgram.kubex.workspace;

import java.nio.file.Path;

public record KubeXWorkspaceInitResult(
    boolean success,
    Path workspaceRoot,
    KubeXInitMode mode,
    String message
) {
}
