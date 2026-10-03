package com.piiscan.scheduler.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduler_configuration")
@Data
public class SchedulerConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;
    
    private LocalTime scanTime;
    
    @Enumerated(EnumType.STRING)
    private ScanType scanType;
    
    private boolean retryEnabled;
    private int retryCount;
    private int retryDelay;
    private boolean emailNotificationEnabled;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
