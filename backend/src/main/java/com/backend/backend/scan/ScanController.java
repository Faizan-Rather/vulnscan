package com.backend.backend.scan;

import com.backend.backend.finding.FindingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ScanController {

    public record ScanResponse(Long id, Long projectId, ScanStatus status, Integer riskScore,
                               String errorMessage, Instant createdAt, Instant startedAt,
                               Instant finishedAt, Map<String, Long> summary) {}

    private final ScanService scanService;
    private final FindingRepository findings;

    @PostMapping("/api/projects/{projectId}/scans")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ScanResponse start(@PathVariable Long projectId, Authentication auth) {
        return toResponse(scanService.start(projectId, auth.getName()));
    }

    @GetMapping("/api/projects/{projectId}/scans")
    public List<ScanResponse> list(@PathVariable Long projectId, Authentication auth) {
        return scanService.listForProject(projectId, auth.getName()).stream()
                .map(this::toResponse).toList();
    }

    @GetMapping("/api/scans/{id}")
    public ScanResponse get(@PathVariable Long id, Authentication auth) {
        return toResponse(scanService.getOwned(id, auth.getName()));
    }

    private ScanResponse toResponse(Scan s) {
        Map<String, Long> summary = new LinkedHashMap<>();
        for (String severity : List.of("CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO")) {
            summary.put(severity, 0L);
        }
        for (Object[] row : findings.countBySeverity(s.getId())) {
            summary.put(row[0].toString(), (Long) row[1]);
        }
        return new ScanResponse(s.getId(), s.getProject().getId(), s.getStatus(), s.getRiskScore(),
                s.getErrorMessage(), s.getCreatedAt(), s.getStartedAt(), s.getFinishedAt(), summary);
    }
}