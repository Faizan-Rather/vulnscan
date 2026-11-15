package com.backend.backend.finding;

import com.backend.backend.scan.Scan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "findings")
@Getter
@Setter
@NoArgsConstructor
public class Finding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scan_id")
    private Scan scan;

    @Column(name = "check_id", nullable = false)
    private String checkId;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    private String title;
    private String description;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "line_number")
    private Integer lineNumber;

    private String evidence;
    private String remediation;

    @Column(name = "owasp_category")
    private String owaspCategory;

    public static Builder builder(String checkId, Severity severity, String title) {
        return new Builder(checkId, severity, title);
    }

    /** Small helper so scanners can build findings in one readable chain. */
    public static class Builder {
        private final Finding finding = new Finding();

        Builder(String checkId, Severity severity, String title) {
            finding.checkId = checkId;
            finding.severity = severity;
            finding.title = title;
        }

        public Builder description(String value) { finding.description = value; return this; }
        public Builder file(String path, Integer line) { finding.filePath = path; finding.lineNumber = line; return this; }
        public Builder evidence(String value) { finding.evidence = value; return this; }
        public Builder remediation(String value) { finding.remediation = value; return this; }
        public Builder owasp(String value) { finding.owaspCategory = value; return this; }

        public Finding build() { return finding; }
    }
}