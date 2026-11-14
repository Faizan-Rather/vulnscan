package com.backend.backend.project;

import com.backend.backend.scanner.ScanType;
import com.backend.backend.users.User;
import com.backend.backend.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projects;
    private final UserRepository users;

    public Project create(String email, String name, ScanType type, String targetUrl) {
        if (type == ScanType.WEB && (targetUrl == null || targetUrl.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "targetUrl is required for WEB projects");
        }
        User owner = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Project project = new Project();
        project.setOwner(owner);
        project.setName(name.trim());
        project.setType(type);
        project.setTargetUrl(type == ScanType.WEB ? targetUrl.trim() : null);
        return projects.save(project);
    }

    public List<Project> list(String email) {
        return projects.findByOwnerEmailOrderByCreatedAtDesc(email);
    }

    /** Returns the project only if it belongs to this user, otherwise 404. */
    public Project getOwned(Long id, String email) {
        return projects.findById(id)
                .filter(p -> p.getOwner().getEmail().equals(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    public void delete(Long id, String email) {
        projects.delete(getOwned(id, email));
    }
}