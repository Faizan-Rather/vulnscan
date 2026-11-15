package com.backend.backend.scan;

import com.backend.backend.finding.Finding;
import com.backend.backend.finding.FindingRepository;
import com.backend.backend.finding.Severity;
import com.backend.backend.project.Project;
import com.backend.backend.scanner.ScanContext;
import com.backend.backend.scanner.SecurityCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Runs every matching SecurityCheck in the background and records the outcome. */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScanRunner {

    private final ScanRepository scans;
    private final FindingRepository findings;
    private final List<SecurityCheck> checks;   // Spring injects every SecurityCheck bean here

    @Async("scanExecutor")
    public void run(Long scanId) {
        Scan scan = scans.findById(scanId).orElseThrow();
        scan.setStatus(ScanStatus.RUNNING);
        scan.setStartedAt(Instant.now());
        scans.save(scan);

        try {
            Project project = scan.getProject();
            Path sourceRoot = project.getSourcePath() == null ? null : Path.of(project.getSourcePath());
            ScanContext context = new ScanContext(project.getType(), sourceRoot, project.getTargetUrl());

            List<Finding> all = new ArrayList<>();
            for (SecurityCheck check : checks) {
                if (check.type() != project.getType()) continue;
                try {
                    all.addAll(check.run(context));
                } catch (Exception e) {
                    // One broken check must not kill the whole scan.
                    log.warn("Check {} failed: {}", check.id(), e.toString());
                    all.add(Finding.builder(check.id(), Severity.INFO, "Check could not complete: " + check.id())
                            .description(String.valueOf(e.getMessage()))
                            .build());
                }
            }

            all.forEach(f -> f.setScan(scan));
            findings.saveAll(all);
            scan.setRiskScore(riskScore(all));
            scan.setStatus(ScanStatus.COMPLETED);
        } catch (Exception e) {
            log.error("Scan {} failed", scanId, e);
            scan.setStatus(ScanStatus.FAILED);
            scan.setErrorMessage(e.getMessage());
        } finally {
            scan.setFinishedAt(Instant.now());
            scans.save(scan);
        }
    }

    /** Adds up severity points, capped at 100. */
    static int riskScore(List<Finding> list) {
        int score = 0;
        for (Finding f : list) {
            score += switch (f.getSeverity()) {
                case CRITICAL -> 10;
                case HIGH -> 5;
                case MEDIUM -> 2;
                case LOW -> 1;
                case INFO -> 0;
            };
        }
        return Math.min(score, 100);
    }
}