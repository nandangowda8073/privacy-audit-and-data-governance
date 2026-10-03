package com.piiscan.auth.config;

import com.piiscan.auth.entity.Permission;
import com.piiscan.auth.entity.Role;
import com.piiscan.auth.entity.RolePermission;
import com.piiscan.auth.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RbacDataSeeder implements CommandLineRunner {

    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public void run(String... args) {
        if (rolePermissionRepository.count() == 0) {
            log.info("Seeding initial RBAC permissions...");
            seedSuperAdmin();
            seedSecurityOfficer();
            seedAuditor();
            seedViewer();
            log.info("RBAC permissions seeded successfully.");
        }
    }

    private void seedSuperAdmin() {
        Arrays.stream(Permission.values()).forEach(permission -> {
            rolePermissionRepository.save(RolePermission.builder()
                    .role(Role.ROLE_SUPER_ADMIN)
                    .permission(permission)
                    .build());
        });
    }

    private void seedSecurityOfficer() {
        List<Permission> permissions = Arrays.asList(
                Permission.DATABASE_VIEW, Permission.DATABASE_TEST_CONNECTION,
                Permission.SCAN_VIEW, Permission.SCAN_START, Permission.SCAN_CANCEL, Permission.SCAN_SCHEDULE,
                Permission.PII_VIEW, Permission.PII_EXPORT,
                Permission.RISK_VIEW,
                Permission.MASKING_POLICY_VIEW, Permission.MASKING_POLICY_CREATE, Permission.MASKING_POLICY_UPDATE, Permission.MASKING_POLICY_DELETE,
                Permission.AUDIT_LOG_VIEW, Permission.AUDIT_LOG_EXPORT,
                Permission.REPORT_VIEW, Permission.REPORT_GENERATE, Permission.REPORT_DOWNLOAD,
                Permission.DASHBOARD_VIEW, Permission.NOTIFICATION_VIEW,
                Permission.DECISION_VIEW, Permission.DECISION_EXPLAIN
        );
        permissions.forEach(permission -> {
            rolePermissionRepository.save(RolePermission.builder()
                    .role(Role.ROLE_SECURITY_OFFICER)
                    .permission(permission)
                    .build());
        });
    }

    private void seedAuditor() {
        List<Permission> permissions = Arrays.asList(
                Permission.SCAN_VIEW,
                Permission.PII_VIEW,
                Permission.RISK_VIEW,
                Permission.AUDIT_LOG_VIEW, Permission.AUDIT_LOG_EXPORT,
                Permission.REPORT_VIEW, Permission.REPORT_GENERATE, Permission.REPORT_DOWNLOAD,
                Permission.DASHBOARD_VIEW, Permission.NOTIFICATION_VIEW,
                Permission.DECISION_VIEW
        );
        permissions.forEach(permission -> {
            rolePermissionRepository.save(RolePermission.builder()
                    .role(Role.ROLE_AUDITOR)
                    .permission(permission)
                    .build());
        });
    }

    private void seedViewer() {
        List<Permission> permissions = Arrays.asList(
                Permission.PII_VIEW,
                Permission.RISK_VIEW,
                Permission.DASHBOARD_VIEW,
                Permission.NOTIFICATION_VIEW
        );
        permissions.forEach(permission -> {
            rolePermissionRepository.save(RolePermission.builder()
                    .role(Role.ROLE_VIEWER)
                    .permission(permission)
                    .build());
        });
    }
}
