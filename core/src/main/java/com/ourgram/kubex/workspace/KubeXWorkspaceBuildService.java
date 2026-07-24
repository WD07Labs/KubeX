package com.ourgram.kubex.workspace;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public final class KubeXWorkspaceBuildService {
    private static final Duration COMMAND_TIMEOUT = Duration.ofMinutes(5);
    private static final int OUTPUT_LIMIT = 1200;

    public KubeXWorkspaceBuildResult build(Path gameRoot) {
        Path normalizedGameRoot = gameRoot.toAbsolutePath().normalize();
        Path workspaceRoot = normalizedGameRoot.resolve("kubex");
        Path packageJson = workspaceRoot.resolve("package.json");
        Path nodeModules = workspaceRoot.resolve("node_modules");

        try {
            if(!Files.isDirectory(workspaceRoot)) {
                return new KubeXWorkspaceBuildResult(false, workspaceRoot, "KubeX workspace was not found");
            }

            if(!Files.exists(packageJson)) {
                return new KubeXWorkspaceBuildResult(false, workspaceRoot, "package.json was not found in kubex");
            }

            String npm = isWindows() ? "npm.cmd" : "npm";

            boolean installedDependencies = false;
            if(!Files.isDirectory(nodeModules)) {
                run(workspaceRoot, npm, "install");
                installedDependencies = true;
            }
            run(workspaceRoot, npm, "run", "build");
            return new KubeXWorkspaceBuildResult(
                true,
                workspaceRoot,
                installedDependencies ? "Installed dependencies and completed build" : "Build completed"
            );
        } catch (IOException exception) {
            if(isMissingCommand(exception)) {
                return new KubeXWorkspaceBuildResult(false, workspaceRoot, "Node.js를 설치해주세요 (npm/node was not found)");
            }
            return new KubeXWorkspaceBuildResult(false, workspaceRoot, failureMessage(exception));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new KubeXWorkspaceBuildResult(false, workspaceRoot, "Build interrupted");
        }
    }

    private void run(Path workspaceRoot, String... command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command)
        .directory(workspaceRoot.toFile())
        .redirectErrorStream(true).start();

        boolean finished = process.waitFor(COMMAND_TIMEOUT.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        if(!finished) {
            process.destroyForcibly();
            throw new IOException(command[0] + " timed out");
        }

        if(process.exitValue() != 0) {
            throw new IOException(composeCommandFailure(command, output));
        }
    }

    private String composeCommandFailure(String[] command, String output) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.join(" ", command)).append(" failed");
        if(!output.isBlank()) {
            builder.append(": ").append(trimOutput(output));
        }
        return builder.toString();
    }

    private String trimOutput(String output) {
        String compact = output.replace("\r", " ").replace('\n', ' ').trim();
        if(compact.length() <= OUTPUT_LIMIT) return compact;
        return compact.substring(0, OUTPUT_LIMIT) + "...";
    }

    private boolean isMissingCommand(IOException exception) {
        String message = exception.getMessage();
        if(message == null) return false;

        String lower = message.toLowerCase(java.util.Locale.ROOT);
        return lower.contains("cannot run program")
        || lower.contains("createprocess error=2")
        || lower.contains("error=2")
        || lower.contains("no such file or directory");
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win");
    }

    private String failureMessage(Exception exception) {
        Throwable current = exception;
        while(current != null) {
            String message = current.getMessage();
            if(message != null && !message.isBlank()) return message;
            current = current.getCause();
        }
        return exception.getClass().getSimpleName();
    }
}
