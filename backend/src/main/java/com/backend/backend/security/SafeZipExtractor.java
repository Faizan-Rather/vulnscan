package com.backend.backend.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Extracts an untrusted zip safely: blocks path traversal (zip-slip) and caps total extracted size. */
public final class SafeZipExtractor {

    private static final long MAX_TOTAL_BYTES = 200L * 1024 * 1024;   // 200 MB extracted
    private static final int MAX_ENTRIES = 10_000;

    private SafeZipExtractor() {}

    public static void extract(InputStream in, Path destination) throws IOException {
        Path dest = destination.toAbsolutePath().normalize();
        Files.createDirectories(dest);

        long totalBytes = 0;
        int entryCount = 0;
        int filesExtracted = 0;
        byte[] buffer = new byte[8192];

        try (ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (++entryCount > MAX_ENTRIES) {
                    throw new IOException("Archive has too many entries (max " + MAX_ENTRIES + ")");
                }

                Path target = dest.resolve(entry.getName()).normalize();
                if (!target.startsWith(dest)) {
                    throw new IOException("Blocked a path-traversal entry: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                    continue;
                }

                Files.createDirectories(target.getParent());
                try (OutputStream out = Files.newOutputStream(target)) {
                    int read;
                    while ((read = zis.read(buffer)) > 0) {
                        totalBytes += read;
                        if (totalBytes > MAX_TOTAL_BYTES) {
                            throw new IOException("Archive is too large when extracted (max 200 MB)");
                        }
                        out.write(buffer, 0, read);
                    }
                }
                filesExtracted++;
            }
        }

        if (filesExtracted == 0) {
            throw new IOException("Archive contains no files, or is not a valid zip");
        }
    }
}