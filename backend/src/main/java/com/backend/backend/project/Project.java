package com.backend.backend.project;

import com.backend.backend.scanner.ScanType;
import com.backend.backend.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    private String name;

    @Enumerated(EnumType.STRING)
    private ScanType type;

    @Column(name = "target_url")
    private String targetUrl;

    @Column(name = "source_path")
    private String sourcePath;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}