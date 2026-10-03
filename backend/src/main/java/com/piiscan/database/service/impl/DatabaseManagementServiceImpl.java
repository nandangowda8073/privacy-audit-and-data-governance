package com.piiscan.database.service.impl;

import com.piiscan.database.dto.*;
import com.piiscan.database.entity.ConnectionStatus;
import com.piiscan.database.entity.DatabaseConnection;
import com.piiscan.database.entity.DatabaseType;
import com.piiscan.database.exception.DatabaseConnectionException;
import com.piiscan.database.exception.DuplicateConnectionException;
import com.piiscan.database.repository.DatabaseConnectionRepository;
import com.piiscan.database.security.EncryptionService;
import com.piiscan.database.service.DatabaseManagementService;
import com.piiscan.platform.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DatabaseManagementServiceImpl implements DatabaseManagementService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseManagementServiceImpl.class);

    @Autowired
    private DatabaseConnectionRepository repository;

    @Autowired
    private EncryptionService encryptionService;

    private String getCurrentUser() {
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception ignored) {}
        return "SYSTEM";
    }

    @Autowired
    private com.piiscan.audit.service.AuditLogService auditLogService;

    private void auditLog(String action, String user, String databaseName, Long dbId, boolean success, String reason) {
        log.info("[AUDIT] Action: [{}] | User: [{}] | Database Name: [{}] | Timestamp: [{}]",
                action, user, databaseName, LocalDateTime.now());
        if (auditLogService != null) {
            com.piiscan.audit.enums.AuditAction auditAction = switch (action) {
                case "Database Added" -> com.piiscan.audit.enums.AuditAction.DATABASE_CREATE;
                case "Database Updated" -> com.piiscan.audit.enums.AuditAction.DATABASE_UPDATE;
                case "Database Deleted" -> com.piiscan.audit.enums.AuditAction.DATABASE_DELETE;
                case "Connection Tested" -> com.piiscan.audit.enums.AuditAction.DATABASE_CONNECTION_TEST;
                default -> com.piiscan.audit.enums.AuditAction.DATABASE_UPDATE;
            };
            auditLogService.logDatabaseEvent(
                auditAction,
                success ? com.piiscan.audit.enums.AuditResult.SUCCESS : com.piiscan.audit.enums.AuditResult.FAILURE,
                dbId,
                databaseName,
                action + " for database: " + databaseName,
                reason
            );
        }
    }

    @Override
    public DatabaseConnectionDto createDatabase(CreateDatabaseRequest request, String createdBy) {
        String username = (createdBy != null && !createdBy.isBlank()) ? createdBy : getCurrentUser();
        log.info("User [{}] creating database connection: {}", username, request.getConnectionName());

        // Validate uniqueness
        if (repository.existsByConnectionName(request.getConnectionName())) {
            throw new DuplicateConnectionException("Connection name '" + request.getConnectionName() + "' already exists.");
        }
        if (repository.existsByHostAndDatabaseNameAndDatabaseType(request.getHost(), request.getDatabaseName(), request.getDatabaseType())) {
            throw new DuplicateConnectionException("A connection for host '" + request.getHost() + "' and database '" + request.getDatabaseName() + "' already exists.");
        }

        DatabaseConnection conn = DatabaseConnection.builder()
                .connectionName(request.getConnectionName())
                .databaseType(request.getDatabaseType())
                .host(request.getHost())
                .port(request.getPort())
                .databaseName(request.getDatabaseName())
                .username(request.getUsername())
                .encryptedPassword(encryptionService.encrypt(request.getPassword()))
                .description(request.getDescription())
                .enabled(true)
                .connectionStatus(ConnectionStatus.UNKNOWN)
                .createdBy(username)
                .build();

        DatabaseConnection saved = repository.save(conn);
        auditLog("Database Added", username, saved.getConnectionName(), saved.getId(), true, "New database connection created");
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DatabaseConnectionDto> getAllDatabases(String search, DatabaseType dbType, ConnectionStatus status, Boolean enabled) {
        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;
        return repository.searchDatabases(searchParam, dbType, status, enabled)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DatabaseConnectionDto getDatabaseById(Long id) {
        return mapToDto(findById(id));
    }

    @Override
    public DatabaseConnectionDto updateDatabase(Long id, UpdateDatabaseRequest request) {
        DatabaseConnection conn = findById(id);
        String username = getCurrentUser();
        log.info("User [{}] updating database connection: id={}, name={}", username, id, request.getConnectionName());

        if (repository.existsByConnectionNameAndIdNot(request.getConnectionName(), id)) {
            throw new DuplicateConnectionException("Connection name '" + request.getConnectionName() + "' already exists.");
        }
        if (repository.existsByHostAndDatabaseNameAndDatabaseTypeAndIdNot(request.getHost(), request.getDatabaseName(), request.getDatabaseType(), id)) {
            throw new DuplicateConnectionException("A connection for host '" + request.getHost() + "' and database '" + request.getDatabaseName() + "' already exists.");
        }

        conn.setConnectionName(request.getConnectionName());
        conn.setDatabaseType(request.getDatabaseType());
        conn.setHost(request.getHost());
        conn.setPort(request.getPort());
        conn.setDatabaseName(request.getDatabaseName());
        conn.setUsername(request.getUsername());
        conn.setDescription(request.getDescription());
        conn.setConnectionStatus(ConnectionStatus.UNKNOWN);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            conn.setEncryptedPassword(encryptionService.encrypt(request.getPassword()));
        }

        DatabaseConnection updated = repository.save(conn);
        auditLog("Database Updated", username, updated.getConnectionName(), updated.getId(), true, "Database connection settings updated");
        return mapToDto(updated);
    }

    @Override
    public void deleteDatabase(Long id) {
        DatabaseConnection conn = findById(id);
        String username = getCurrentUser();
        String dbName = conn.getConnectionName();
        log.info("User [{}] deleting database connection: id={}, name={}", username, id, dbName);
        repository.delete(conn);
        auditLog("Database Deleted", username, dbName, id, true, "Database connection deleted by admin");
    }

    @Override
    public TestConnectionResponse testConnection(Long id) {
        DatabaseConnection conn = findById(id);
        String username = getCurrentUser();
        log.info("User [{}] testing connection for database: id={}, name={}", username, id, conn.getConnectionName());

        long startTime = System.currentTimeMillis();
        LocalDateTime timestamp = LocalDateTime.now();

        try {
            String password = encryptionService.decrypt(conn.getEncryptedPassword());
            String jdbcUrl = buildJdbcUrl(conn);

            // Load the correct JDBC driver
            if (conn.getDatabaseType() == DatabaseType.MYSQL) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else if (conn.getDatabaseType() == DatabaseType.POSTGRESQL) {
                Class.forName("org.postgresql.Driver");
            }

            try (Connection jdbcConn = DriverManager.getConnection(jdbcUrl, conn.getUsername(), password)) {
                long responseTime = System.currentTimeMillis() - startTime;
                DatabaseMetaData meta = jdbcConn.getMetaData();
                String version = meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();

                conn.setConnectionStatus(ConnectionStatus.CONNECTED);
                conn.setDatabaseVersion(version);
                conn.setLastConnectionTest(timestamp);
                conn.setLastResponseTimeMs(responseTime);
                repository.save(conn);

                auditLog("Connection Tested", username, conn.getConnectionName(), id, true, "Connection test succeeded: " + version);
                log.info("Connection test SUCCEEDED for db id={}, version={}, time={}ms", id, version, responseTime);
                return TestConnectionResponse.builder()
                        .success(true)
                        .message("Connection Successful")
                        .databaseVersion(version)
                        .responseTimeMs(responseTime)
                        .timestamp(timestamp)
                        .build();
            }
        } catch (ClassNotFoundException e) {
            log.warn("JDBC driver not found for db id={}: {}", id, e.getMessage());
            conn.setConnectionStatus(ConnectionStatus.DISCONNECTED);
            conn.setLastConnectionTest(timestamp);
            repository.save(conn);
            auditLog("Connection Tested", username, conn.getConnectionName(), id, false, "JDBC driver not available: " + conn.getDatabaseType());
            return buildFailedResponse("JDBC driver not available: " + conn.getDatabaseType(), System.currentTimeMillis() - startTime, timestamp);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.warn("Connection test FAILED for db id={}: {}", id, e.getMessage());
            conn.setConnectionStatus(ConnectionStatus.DISCONNECTED);
            conn.setLastConnectionTest(timestamp);
            conn.setLastResponseTimeMs(responseTime);
            repository.save(conn);
            auditLog("Connection Tested", username, conn.getConnectionName(), id, false, simplifyErrorMessage(e.getMessage()));
            return buildFailedResponse("Connection Failed: " + simplifyErrorMessage(e.getMessage()), responseTime, timestamp);
        }
    }

    @Override
    public DatabaseConnectionDto enableDatabase(Long id) {
        DatabaseConnection conn = findById(id);
        String username = getCurrentUser();
        conn.setEnabled(true);
        DatabaseConnection saved = repository.save(conn);
        auditLog("Database Updated", username, saved.getConnectionName(), saved.getId(), true, "Database enabled");
        return mapToDto(saved);
    }

    @Override
    public DatabaseConnectionDto disableDatabase(Long id) {
        DatabaseConnection conn = findById(id);
        String username = getCurrentUser();
        conn.setEnabled(false);
        conn.setConnectionStatus(ConnectionStatus.DISCONNECTED);
        DatabaseConnection saved = repository.save(conn);
        auditLog("Database Updated", username, saved.getConnectionName(), saved.getId(), true, "Database disabled");
        return mapToDto(saved);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private DatabaseConnection findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Database connection not found with id: " + id));
    }

    private String buildJdbcUrl(DatabaseConnection conn) {
        return switch (conn.getDatabaseType()) {
            case MYSQL -> String.format("jdbc:mysql://%s:%d/%s?connectTimeout=5000&socketTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true",
                    conn.getHost(), conn.getPort(), conn.getDatabaseName());
            case POSTGRESQL -> String.format("jdbc:postgresql://%s:%d/%s?connectTimeout=5&socketTimeout=5",
                    conn.getHost(), conn.getPort(), conn.getDatabaseName());
        };
    }

    private TestConnectionResponse buildFailedResponse(String message, long responseTimeMs, LocalDateTime timestamp) {
        return TestConnectionResponse.builder()
                .success(false)
                .message(message)
                .responseTimeMs(responseTimeMs)
                .timestamp(timestamp)
                .build();
    }

    private String simplifyErrorMessage(String msg) {
        if (msg == null) return "Unknown error";
        if (msg.contains("Connection refused")) return "Connection refused — is the host reachable and the port open?";
        if (msg.contains("Unknown host") || msg.contains("nodename")) return "Unknown host — check the hostname.";
        if (msg.contains("Access denied")) return "Access denied — check username and password.";
        if (msg.contains("timeout") || msg.contains("timed out")) return "Connection timed out — host is unreachable.";
        return msg.length() > 120 ? msg.substring(0, 120) + "…" : msg;
    }

    private DatabaseConnectionDto mapToDto(DatabaseConnection c) {
        return DatabaseConnectionDto.builder()
                .id(c.getId())
                .connectionName(c.getConnectionName())
                .databaseType(c.getDatabaseType())
                .host(c.getHost())
                .port(c.getPort())
                .databaseName(c.getDatabaseName())
                .username(c.getUsername())
                .description(c.getDescription())
                .enabled(c.isEnabled())
                .connectionStatus(c.getConnectionStatus())
                .databaseVersion(c.getDatabaseVersion())
                .lastConnectionTest(c.getLastConnectionTest())
                .lastResponseTimeMs(c.getLastResponseTimeMs())
                .createdBy(c.getCreatedBy())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
