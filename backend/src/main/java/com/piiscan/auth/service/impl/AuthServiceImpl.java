package com.piiscan.auth.service.impl;

import com.piiscan.auth.dto.AuthResponse;
import com.piiscan.auth.dto.LoginRequest;
import com.piiscan.auth.dto.RegisterRequest;
import com.piiscan.auth.dto.UserDto;
import com.piiscan.auth.entity.User;
import com.piiscan.auth.repository.UserRepository;
import com.piiscan.auth.service.AuthService;
import com.piiscan.platform.common.exception.BadRequestException;
import com.piiscan.platform.common.exception.ResourceNotFoundException;
import com.piiscan.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private com.piiscan.audit.service.AuditLogService auditLogService;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail(), loginRequest.getUsernameOrEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (auditLogService != null) {
            auditLogService.logAuthEvent(
                com.piiscan.audit.enums.AuditAction.AUTH_LOGIN,
                com.piiscan.audit.enums.AuditResult.SUCCESS,
                user.getUsername(),
                user.getRole() != null ? user.getRole().name() : null,
                "User logged in successfully",
                "Valid credentials"
            );
        }

        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .user(mapToDto(user))
                .build();
    }

    @Override
    public UserDto register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email Address already in use!");
        }

        User user = User.builder()
                .fullName(registerRequest.getFullName())
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole())
                .active(true)
                .build();

        User result = userRepository.save(user);

        if (auditLogService != null) {
            auditLogService.logUserManagementEvent(
                com.piiscan.audit.enums.AuditAction.USER_CREATE,
                com.piiscan.audit.enums.AuditResult.SUCCESS,
                result.getUsername(),
                "NONE",
                result.getRole() != null ? result.getRole().name() : "USER",
                "New user registered"
            );
        }

        return mapToDto(result);
    }

    @Override
    public UserDto getCurrentUser(String usernameOrEmail) {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToDto(user);
    }

    @Autowired
    private com.piiscan.auth.repository.RolePermissionRepository rolePermissionRepository;

    private UserDto mapToDto(User user) {
        java.util.List<String> permissions = rolePermissionRepository.findByRole(user.getRole())
                .stream()
                .map(rp -> rp.getPermission().name())
                .collect(java.util.stream.Collectors.toList());

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .permissions(permissions)
                .build();
    }
}
