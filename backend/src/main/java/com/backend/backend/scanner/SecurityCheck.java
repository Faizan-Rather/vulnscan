package com.backend.backend.scanner;

import com.backend.backend.finding.Finding;

import java.util.List;

/** The plugin contract. Every scanner is a Spring bean that implements this. */
public interface SecurityCheck {

    /** Short unique name, e.g. "SECRETS-001". */
    String id();

    /** Which kind of project this check applies to. */
    ScanType type();

    /** Examines the target and returns whatever problems it found (empty list if none). */
    List<Finding> run(ScanContext context) throws Exception;
}