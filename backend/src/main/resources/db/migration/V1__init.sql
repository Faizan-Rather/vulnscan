CREATE TABLE users (
                       id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email         VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
                       created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB;

CREATE TABLE projects (
                          id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                          owner_id    BIGINT       NOT NULL,
                          name        VARCHAR(100) NOT NULL,
                          type        VARCHAR(20)  NOT NULL,
                          target_url  VARCHAR(500),
                          source_path VARCHAR(500),
                          created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                          CONSTRAINT fk_projects_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE scans (
                       id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                       project_id    BIGINT      NOT NULL,
                       status        VARCHAR(20) NOT NULL,
                       risk_score    INT,
                       error_message TEXT,
                       created_at    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                       started_at    DATETIME(6),
                       finished_at   DATETIME(6),
                       CONSTRAINT fk_scans_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE findings (
                          id             BIGINT AUTO_INCREMENT PRIMARY KEY,
                          scan_id        BIGINT       NOT NULL,
                          check_id       VARCHAR(50)  NOT NULL,
                          severity       VARCHAR(20)  NOT NULL,
                          title          VARCHAR(500) NOT NULL,
                          description    TEXT,
                          file_path      VARCHAR(1000),
                          line_number    INT,
                          evidence       TEXT,
                          remediation    TEXT,
                          owasp_category VARCHAR(100),
                          CONSTRAINT fk_findings_scan FOREIGN KEY (scan_id) REFERENCES scans(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_findings_severity ON findings(severity);