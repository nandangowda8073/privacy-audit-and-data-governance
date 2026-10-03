package com.piiscan.database.controller;

import com.piiscan.database.dto.*;
import com.piiscan.database.entity.ConnectionStatus;
import com.piiscan.database.entity.DatabaseType;
import com.piiscan.database.service.DatabaseManagementService;
import com.piiscan.platform.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/databases", "/api/databases"})
public class DatabaseController {

    @Autowired
    private DatabaseManagementService service;

    @PostMapping
    @PreAuthorize("hasAuthority('DATABASE_CREATE')")
    public ResponseEntity<ApiResponse<DatabaseConnectionDto>> createDatabase(
            @Valid @RequestBody CreateDatabaseRequest request,
            @AuthenticationPrincipal UserDetails user) {
        DatabaseConnectionDto dto = service.createDatabase(request, user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Database connection created successfully", dto));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DATABASE_VIEW')")
    public ResponseEntity<ApiResponse<List<DatabaseConnectionDto>>> getAllDatabases(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) DatabaseType dbType,
            @RequestParam(required = false) ConnectionStatus status,
            @RequestParam(required = false) Boolean enabled) {
        List<DatabaseConnectionDto> list = service.getAllDatabases(search, dbType, status, enabled);
        return ResponseEntity.ok(ApiResponse.success("Database connections retrieved", list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DATABASE_VIEW')")
    public ResponseEntity<ApiResponse<DatabaseConnectionDto>> getDatabaseById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Database connection details retrieved", service.getDatabaseById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DATABASE_UPDATE')")
    public ResponseEntity<ApiResponse<DatabaseConnectionDto>> updateDatabase(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDatabaseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Database connection updated successfully", service.updateDatabase(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DATABASE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteDatabase(@PathVariable Long id) {
        service.deleteDatabase(id);
        return ResponseEntity.ok(ApiResponse.success("Database connection deleted successfully", null));
    }

    @PostMapping("/{id}/test")
    @PreAuthorize("hasAuthority('DATABASE_TEST_CONNECTION')")
    public ResponseEntity<ApiResponse<TestConnectionResponse>> testConnection(@PathVariable Long id) {
        TestConnectionResponse result = service.testConnection(id);
        return ResponseEntity.ok(ApiResponse.success("Connection test executed", result));
    }

    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('DATABASE_UPDATE')")
    public ResponseEntity<ApiResponse<DatabaseConnectionDto>> enableDatabase(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Database enabled successfully", service.enableDatabase(id)));
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('DATABASE_UPDATE')")
    public ResponseEntity<ApiResponse<DatabaseConnectionDto>> disableDatabase(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Database disabled successfully", service.disableDatabase(id)));
    }
}
