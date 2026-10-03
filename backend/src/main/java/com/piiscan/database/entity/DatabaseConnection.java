package com.piiscan.database.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "database_connections", uniqueConstraints = {
        @UniqueConstraint(name = "uq_connection_name", columnNames = {"connection_name"}),
        @UniqueConstraint(name = "uq_host_db", columnNames = {"host", "database_name", "database_type"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "connection_name", nullable = false, unique = true, length = 100)
    private String connectionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "database_type", nullable = false, length = 30)
    private DatabaseType databaseType;

    @Column(nullable = false, length = 255)
    private String host;

    @Column(nullable = false)
    private Integer port;

    @Column(name = "database_name", nullable = false, length = 255)
    private String databaseName;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "encrypted_password", nullable = false, length = 500)
    private String encryptedPassword;

    @Column(length = 500)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_status", length = 20)
    @Builder.Default
    private ConnectionStatus connectionStatus = ConnectionStatus.UNKNOWN;

    @Column(name = "database_version", length = 100)
    private String databaseVersion;

    @Column(name = "last_connection_test")
    private LocalDateTime lastConnectionTest;

    @Column(name = "last_response_time_ms")
    private Long lastResponseTimeMs;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
