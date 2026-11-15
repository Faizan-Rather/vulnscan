package com.backend.backend.scanner;

import java.nio.file.Path;

/** Everything a check needs to know about what it is scanning. */
public record ScanContext(ScanType type, Path sourceRoot, String targetUrl) {}