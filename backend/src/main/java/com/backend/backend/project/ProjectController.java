package com.backend.backend.project;

import com.backend.backend.scanner.ScanType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    public record ProjectRequest(@NotBlank @Size(max = 100) String name,
                                 @NotNull ScanType type,
                                 @Size(max = 500) String targetUrl) {}

    public record ProjectResponse(Long id, String name, ScanType type,
                                  String targetUrl, Instant createdAt) {
        static ProjectResponse of(Project p) {
            return new ProjectResponse(p.getId(), p.getName(), p.getType(),
                    p.getTargetUrl(), p.getCreatedAt());
        }
    }

    private final ProjectService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request, Authentication auth) {
        return ProjectResponse.of(
                service.create(auth.getName(), request.name(), request.type(), request.targetUrl()));
    }

    @GetMapping
    public List<ProjectResponse> list(Authentication auth) {
        return service.list(auth.getName()).stream().map(ProjectResponse::of).toList();
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable Long id, Authentication auth) {
        return ProjectResponse.of(service.getOwned(id, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        service.delete(id, auth.getName());
    }
}
