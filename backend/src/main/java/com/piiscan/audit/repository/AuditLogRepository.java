package com.piiscan.audit.repository;

import com.piiscan.audit.entity.AuditLog;
import com.piiscan.audit.enums.AuditAction;
import com.piiscan.audit.enums.AuditResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    List<AuditLog> findTop50ByOrderByTimestampDesc();

    Page<AuditLog> findByUsername(String username, Pageable pageable);

    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    Page<AuditLog> findByResult(AuditResult result, Pageable pageable);

    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action IN :accessActions AND a.timestamp >= :from AND a.timestamp <= :to")
    long countAccessAttempts(
            @org.springframework.data.repository.query.Param("accessActions") java.util.List<AuditAction> accessActions,
            @org.springframework.data.repository.query.Param("from") java.time.LocalDateTime from,
            @org.springframework.data.repository.query.Param("to") java.time.LocalDateTime to
    );

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action IN :deniedActions AND a.timestamp >= :from AND a.timestamp <= :to")
    long countAccessDeniedEvents(
            @org.springframework.data.repository.query.Param("deniedActions") java.util.List<AuditAction> deniedActions,
            @org.springframework.data.repository.query.Param("from") java.time.LocalDateTime from,
            @org.springframework.data.repository.query.Param("to") java.time.LocalDateTime to
    );
}
