package com.backend.backend.scanner.dependency;

/** One library a project declares, with the file it came from. */
public record Dependency(String ecosystem, String name, String version, String file) {}