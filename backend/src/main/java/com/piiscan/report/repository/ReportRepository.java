package com.piiscan.report.repository;

import com.piiscan.report.entity.Report;
import com.piiscan.report.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {
    Page<Report> findByGeneratedBy(String generatedBy, Pageable pageable);
    List<Report> findByReportType(ReportType reportType);
}
