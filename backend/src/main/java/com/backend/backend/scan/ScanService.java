package com.backend.backend.scan;

import com.backend.backend.project.Project;
import com.backend.backend.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScanService {

    private final ScanRepository scans;
    private final ScanRunner runner;
    private final ProjectService projects;

    /**
     * Deliberately NOT @Transactional: the scan row must be committed to the database
     * before the background job tries to read it.
     */
    public Scan start(Long projectId, String email) {
        Project project = projects.getOwned(projectId, email);   // 404 if not yours

        Scan scan = new Scan();
        scan.setProject(project);
        scan = scans.save(scan);

        runner.run(scan.getId());   // returns immediately, the work happens in the background
        return scan;
    }

    /** Returns the scan only if its project belongs to this user, otherwise 404. */
    public Scan getOwned(Long scanId, String email) {
        return scans.findById(scanId)
                .filter(s -> s.getProject().getOwner().getEmail().equals(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Scan not found"));
    }

    public List<Scan> listForProject(Long projectId, String email) {
        projects.getOwned(projectId, email);   // ownership check
        return scans.findByProjectIdOrderByCreatedAtDesc(projectId);
    }
}