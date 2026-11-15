package com.backend.backend.scanner;

import com.backend.backend.finding.Finding;
import com.backend.backend.finding.Severity;
import org.springframework.stereotype.Component;

import java.util.List;

/** Temporary check that returns fake findings, so we can test the whole scan flow. Deleted on Day 7. */
@Component
public class DummyCheck implements SecurityCheck {

    @Override
    public String id() {
        return "DUMMY-001";
    }

    @Override
    public ScanType type() {
        return ScanType.CODE;
    }

    @Override
    public List<Finding> run(ScanContext context) throws Exception {
        Thread.sleep(3000);   // pretend to work, so you can watch the RUNNING status

        return List.of(
                Finding.builder(id(), Severity.HIGH, "Example high-severity finding")
                        .description("This is a fake finding that proves the scan pipeline works.")
                        .file("src/Example.java", 42)
                        .evidence("String password = \"hunter2\";")
                        .remediation("Move secrets into environment variables.")
                        .owasp("A07:2021 Identification and Authentication Failures")
                        .build(),
                Finding.builder(id(), Severity.LOW, "Example low-severity finding")
                        .description("Another fake finding, to test counting by severity.")
                        .build()
        );
    }
}