package com.piiscan.database.dto;

import com.piiscan.database.entity.DatabaseType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateDatabaseRequest {

    @NotBlank(message = "Connection name is required")
    @Size(min = 2, max = 100, message = "Connection name must be between 2 and 100 characters")
    private String connectionName;

    @NotNull(message = "Database type is required")
    private DatabaseType databaseType;

    @NotBlank(message = "Host is required")
    @Size(max = 255, message = "Host must not exceed 255 characters")
    private String host;

    @NotNull(message = "Port is required")
    @Min(value = 1, message = "Port must be at least 1")
    @Max(value = 65535, message = "Port must not exceed 65535")
    private Integer port;

    @NotBlank(message = "Database name is required")
    @Size(max = 255, message = "Database name must not exceed 255 characters")
    private String databaseName;

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username must not exceed 100 characters")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
