package com.piiscan.database.dto;

import com.piiscan.database.entity.ConnectionStatus;
import com.piiscan.database.entity.DatabaseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseConnectionDto {
    private Long id;
    private String connectionName;
    private DatabaseType databaseType;
    private String host;
    private Integer port;
    private String databaseName;
    private String username;
    private String description;
    private boolean enabled;
    private ConnectionStatus connectionStatus;
    private String databaseVersion;
    private LocalDateTime lastConnectionTest;
    private Long lastResponseTimeMs;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
