package com.backend.backend.project;

import com.backend.backend.scanner.ScanType;
import com.backend.backend.security.SafeZipExtractor;
import com.backend.backend.security.SsrfGuard;
import com.backend.backend.users.User;
import com.backend.backend.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projects;
    private final UserRepository users;

    @Value("${vulnscan.storage-dir}")
    private String storageDir;

    public Project create(String email, String name, ScanType type, String targetUrl) {
        if (type == ScanType.WEB && (targetUrl == null || targetUrl.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "targetUrl is required for WEB projects");
        }

        if (type == ScanType.WEB) {
            try {
                SsrfGuard.check(targetUrl.trim());
            } catch (SsrfGuard.SsrfBlockedException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
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

    public Project uploadSource(Long id, String email, MultipartFile file) {
        Project project = getOwned(id, email);
        if (project.getType() != ScanType.CODE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only CODE projects accept uploads");
        }

        Path dir = Path.of(storageDir, String.valueOf(project.getId())).toAbsolutePath().normalize();
        try (InputStream in = file.getInputStream()) {
            deleteRecursively(dir);              // clear out any previous upload for this project
            SafeZipExtractor.extract(in, dir);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid archive: " + e.getMessage());
        }

        project.setSourcePath(dir.toString());
        return projects.save(project);
    }

    private static void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (Stream<Path> walk = Files.walk(dir)) {
            for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
        }
    }
}