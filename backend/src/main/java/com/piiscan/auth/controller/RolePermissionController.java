package com.piiscan.auth.controller;

import com.piiscan.auth.entity.Permission;
import com.piiscan.auth.entity.Role;
import com.piiscan.auth.entity.RolePermission;
import com.piiscan.auth.repository.RolePermissionRepository;
import com.piiscan.platform.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/roles")
public class RolePermissionController {

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<ApiResponse<Map<Role, List<Permission>>>> getRolePermissions() {
        List<RolePermission> all = rolePermissionRepository.findAll();
        Map<Role, List<Permission>> matrix = Arrays.stream(Role.values())
                .collect(Collectors.toMap(
                        role -> role,
                        role -> all.stream()
                                .filter(rp -> rp.getRole() == role)
                                .map(RolePermission::getPermission)
                                .collect(Collectors.toList())
                ));
        return ResponseEntity.ok(ApiResponse.success("Role permissions retrieved", matrix));
    }

    @GetMapping("/permissions/all")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<ApiResponse<List<Permission>>> getAllAvailablePermissions() {
        return ResponseEntity.ok(ApiResponse.success("All permissions retrieved", Arrays.asList(Permission.values())));
    }

    @Autowired
    private com.piiscan.auth.service.SecurityAuditLogger auditLogger;

    @PutMapping("/{role}/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @Transactional
    public ResponseEntity<ApiResponse<List<Permission>>> updateRolePermissions(
            @PathVariable Role role,
            @RequestBody List<Permission> newPermissions,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails user) {
        
        rolePermissionRepository.deleteByRole(role);
        
        List<RolePermission> toSave = newPermissions.stream()
                .map(p -> RolePermission.builder().role(role).permission(p).build())
                .collect(Collectors.toList());
        
        rolePermissionRepository.saveAll(toSave);
        
        String username = user != null ? user.getUsername() : "UNKNOWN";
        auditLogger.logPermissionChange(username, role.name());
        
        return ResponseEntity.ok(ApiResponse.success("Role permissions updated", newPermissions));
    }
}
