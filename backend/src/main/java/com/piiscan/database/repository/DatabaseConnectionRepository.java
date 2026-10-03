package com.piiscan.database.repository;

import com.piiscan.database.entity.ConnectionStatus;
import com.piiscan.database.entity.DatabaseConnection;
import com.piiscan.database.entity.DatabaseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatabaseConnectionRepository extends JpaRepository<DatabaseConnection, Long> {

    boolean existsByConnectionName(String connectionName);

    boolean existsByConnectionNameAndIdNot(String connectionName, Long id);

    boolean existsByHostAndDatabaseNameAndDatabaseTypeAndIdNot(String host, String databaseName, DatabaseType databaseType, Long id);

    boolean existsByHostAndDatabaseNameAndDatabaseType(String host, String databaseName, DatabaseType databaseType);

    Optional<DatabaseConnection> findByConnectionName(String connectionName);

    @Query("SELECT d FROM DatabaseConnection d WHERE " +
           "(:search IS NULL OR LOWER(d.connectionName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(d.host) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:dbType IS NULL OR d.databaseType = :dbType) " +
           "AND (:status IS NULL OR d.connectionStatus = :status) " +
           "AND (:enabled IS NULL OR d.enabled = :enabled) " +
           "ORDER BY d.createdAt DESC")
    List<DatabaseConnection> searchDatabases(
            @Param("search") String search,
            @Param("dbType") DatabaseType dbType,
            @Param("status") ConnectionStatus status,
            @Param("enabled") Boolean enabled
    );
}
