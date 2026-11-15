package com.backend.backend.finding;

import com.backend.backend.scan.ScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FindingController {

    public record FindingResponse(Long id, String checkId, Severity severity, String title,
                                  String description, String filePath, Integer lineNumber,
                                  String evidence, String remediation, String owaspCategory) {
        static FindingResponse of(Finding f) {
            return new FindingResponse(f.getId(), f.getCheckId(), f.getSeverity(), f.getTitle(),
                    f.getDescription(), f.getFilePath(), f.getLineNumber(), f.getEvidence(),
                    f.getRemediation(), f.getOwaspCategory());
        }
    }

    private final FindingRepository findings;
    private final ScanService scanService;

    @GetMapping("/api/scans/{scanId}/findings")
    public Page<FindingResponse> list(@PathVariable Long scanId,
                                      @RequestParam(required = false) Severity severity,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      Authentication auth) {
        scanService.getOwned(scanId, auth.getName());   // 404 if the scan isn't yours

        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("id"));

        Page<Finding> result = (severity == null)
                ? findings.findByScanId(scanId, pageRequest)
                : findings.findByScanIdAndSeverity(scanId, severity, pageRequest);
        return result.map(FindingResponse::of);
    }
}