package com.ourgram.kubex;

public record ProjectSnapshot(
    String name,
    String metadataProvider,
    String primaryPlatform,
    String typingGenerator,
    String snippetGenerator,
    String compiler,
    String compilerBackend
) {
}
