package com.piiscan.auth.controller;

import com.piiscan.auth.dto.UserDto;
import com.piiscan.auth.entity.User;
import com.piiscan.auth.repository.UserRepository;
import com.piiscan.platform.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.piiscan.auth.entity.Role;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDto> userDtos = users.stream().map(user -> UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Users list retrieved", userDtos));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<ApiResponse<UserDto>> getUser(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(ApiResponse.success("User retrieved", toDto(user))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("User not found")));
    }

    @Autowired
    private com.piiscan.auth.service.SecurityAuditLogger auditLogger;

    @org.springframework.web.bind.annotation.PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<ApiResponse<UserDto>> updateUserRole(@org.springframework.web.bind.annotation.PathVariable Long id, @org.springframework.web.bind.annotation.RequestParam Role role, @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails currentUser) {
        return userRepository.findById(id).map(user -> {
            user.setRole(role);
            User saved = userRepository.save(user);
            String adminName = currentUser != null ? currentUser.getUsername() : "UNKNOWN";
            auditLogger.logRoleChange(adminName, user.getUsername(), role.name());
            return ResponseEntity.ok(ApiResponse.success("User role updated", toDto(saved)));
        }).orElse(ResponseEntity.status(404).body(ApiResponse.error("User not found")));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<ApiResponse<UserDto>> updateUserStatus(@org.springframework.web.bind.annotation.PathVariable Long id, @org.springframework.web.bind.annotation.RequestParam boolean active, @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails currentUser) {
        return userRepository.findById(id).map(user -> {
            user.setActive(active);
            User saved = userRepository.save(user);
            String adminName = currentUser != null ? currentUser.getUsername() : "UNKNOWN";
            auditLogger.logUserStatusChange(adminName, user.getUsername(), active);
            return ResponseEntity.ok(ApiResponse.success("User status updated", toDto(saved)));
        }).orElse(ResponseEntity.status(404).body(ApiResponse.error("User not found")));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@org.springframework.web.bind.annotation.PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(404).body(ApiResponse.error("User not found"));
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted", null));
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
