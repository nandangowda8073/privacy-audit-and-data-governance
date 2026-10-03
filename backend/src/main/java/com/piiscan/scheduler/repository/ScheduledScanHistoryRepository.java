package com.piiscan.scheduler.repository;

import com.piiscan.scheduler.entity.ScheduledScanHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduledScanHistoryRepository extends JpaRepository<ScheduledScanHistory, Long> {
    List<ScheduledScanHistory> findTop10ByOrderByStartedAtDesc();
    ScheduledScanHistory findFirstByOrderByStartedAtDesc();

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(s.recordsScanned), 0) FROM ScheduledScanHistory s WHERE s.startedAt BETWEEN :from AND :to")
    Long sumRecordsScannedBetween(@org.springframework.data.repository.query.Param("from") java.time.LocalDateTime from, @org.springframework.data.repository.query.Param("to") java.time.LocalDateTime to);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(s.piiDetected), 0) FROM ScheduledScanHistory s WHERE s.startedAt BETWEEN :from AND :to")
    Long sumPiiDetectedBetween(@org.springframework.data.repository.query.Param("from") java.time.LocalDateTime from, @org.springframework.data.repository.query.Param("to") java.time.LocalDateTime to);

    List<ScheduledScanHistory> findByStartedAtBetweenOrderByStartedAtDesc(java.time.LocalDateTime from, java.time.LocalDateTime to);
}
