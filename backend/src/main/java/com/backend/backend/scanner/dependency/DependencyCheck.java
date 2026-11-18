package com.backend.backend.scanner.dependency;

import com.fasterxml.jackson.databind.JsonNode;
import com.backend.backend.finding.Finding;
import com.backend.backend.finding.Severity;
import com.backend.backend.scanner.ScanContext;
import com.backend.backend.scanner.ScanType;
import com.backend.backend.scanner.SecurityCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/** SCA (Software Composition Analysis): finds known CVEs in declared dependencies via OSV.dev. */
@Component
@RequiredArgsConstructor
@Slf4j
public class DependencyCheck implements SecurityCheck {

    private static final Set<String> SKIP_DIRS = Set.of("node_modules", ".git", "target", "build", "dist");
    private static final int MAX_DEPENDENCIES = 300;   // safety cap so one huge project can't run forever
    private static final String OWASP_CATEGORY = "A06:2021 Vulnerable and Outdated Components";

    private final OsvClient osv;

    @Override
    public String id() {
        return "SCA-001";
    }

    @Override
    public ScanType type() {
        return ScanType.CODE;
    }

    @Override
    public List<Finding> run(ScanContext ctx) throws Exception {
        Path root = ctx.sourceRoot();
        List<Dependency> dependencies = new ArrayList<>();

        try (Stream<Path> files = Files.walk(root, 8)) {
            for (Path file : files.filter(Files::isRegularFile).filter(f -> !inSkippedDir(root, f)).toList()) {
                String name = file.getFileName().toString();
                String relativePath = root.relativize(file).toString();
                try {
                    if (name.equals("pom.xml")) {
                        dependencies.addAll(PomParser.parse(file, relativePath));
                    } else if (name.equals("package.json")) {
                        dependencies.addAll(NpmParser.parse(file, relativePath));
                    }
                } catch (Exception e) {
                    log.warn("Could not parse {}: {}", relativePath, e.toString());
                }
            }
        }

        List<Finding> findings = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int queried = 0;

        for (Dependency dep : dependencies) {
            String key = dep.ecosystem() + ":" + dep.name() + "@" + dep.version();
            if (!seen.add(key)) continue;          // same library found in two files, don't query twice
            if (++queried > MAX_DEPENDENCIES) break;

            try {
                for (JsonNode vuln : osv.query(dep)) {
                    findings.add(toFinding(dep, vuln));
                }
            } catch (Exception e) {
                log.warn("OSV lookup failed for {}@{}: {}", dep.name(), dep.version(), e.toString());
            }
        }
        return findings;
    }

    private Finding toFinding(Dependency dep, JsonNode vuln) {
        String vulnId = vuln.path("id").asText("UNKNOWN");
        String fixedVersion = fixedVersion(vuln);

        return Finding.builder(id(), severityOf(vuln),
                        "Vulnerable dependency: " + dep.name() + "@" + dep.version() + " (" + vulnId + ")")
                .description(vuln.path("summary").asText("No summary provided by OSV."))
                .file(dep.file(), null)
                .evidence(dep.name() + "@" + dep.version())
                .remediation(fixedVersion != null
                        ? "Upgrade " + dep.name() + " to " + fixedVersion + " or later."
                        : "See https://osv.dev/vulnerability/" + vulnId + " for fix guidance.")
                .owasp(OWASP_CATEGORY)
                .build();
    }

    static Severity severityOf(JsonNode vuln) {
        String s = vuln.path("database_specific").path("severity").asText("MODERATE").toUpperCase();
        return switch (s) {
            case "CRITICAL" -> Severity.CRITICAL;
            case "HIGH" -> Severity.HIGH;
            case "LOW" -> Severity.LOW;
            default -> Severity.MEDIUM;
        };
    }

    static String fixedVersion(JsonNode vuln) {
        for (JsonNode affected : vuln.path("affected")) {
            for (JsonNode range : affected.path("ranges")) {
                for (JsonNode event : range.path("events")) {
                    if (event.has("fixed")) return event.get("fixed").asText();
                }
            }
        }
        return null;
    }

    private static boolean inSkippedDir(Path root, Path file) {
        for (Path part : root.relativize(file)) {
            if (SKIP_DIRS.contains(part.toString())) return true;
        }
        return false;
    }
}