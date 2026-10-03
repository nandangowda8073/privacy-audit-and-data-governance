package com.piiscan.database.service;

import com.piiscan.database.dto.*;
import com.piiscan.database.entity.ConnectionStatus;
import com.piiscan.database.entity.DatabaseType;

import java.util.List;

public interface DatabaseManagementService {
    DatabaseConnectionDto createDatabase(CreateDatabaseRequest request, String createdBy);
    List<DatabaseConnectionDto> getAllDatabases(String search, DatabaseType dbType, ConnectionStatus status, Boolean enabled);
    DatabaseConnectionDto getDatabaseById(Long id);
    DatabaseConnectionDto updateDatabase(Long id, UpdateDatabaseRequest request);
    void deleteDatabase(Long id);
    TestConnectionResponse testConnection(Long id);
    DatabaseConnectionDto enableDatabase(Long id);
    DatabaseConnectionDto disableDatabase(Long id);
}
