package com.backend.backend.finding;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FindingRepository extends JpaRepository<Finding, Long> {

    Page<Finding> findByScanId(Long scanId, Pageable pageable);

    Page<Finding> findByScanIdAndSeverity(Long scanId, Severity severity, Pageable pageable);

    @Query("select f.severity, count(f) from Finding f where f.scan.id = :scanId group by f.severity")
    List<Object[]> countBySeverity(@Param("scanId") Long scanId);
}