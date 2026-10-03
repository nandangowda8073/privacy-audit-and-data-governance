package com.piiscan.decision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piiscan.auth.entity.Permission;
import com.piiscan.auth.entity.Role;
import com.piiscan.auth.entity.User;
import com.piiscan.auth.repository.UserRepository;
import com.piiscan.security.JwtTokenProvider;
import com.piiscan.decision.dto.DecisionExplanationRequest;
import com.piiscan.decision.enums.DecisionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ExplainableDecisionSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String viewerToken;

    @BeforeEach
    void setUp() {
        // Setup Super Admin with DECISION_EXPLAIN and DECISION_VIEW
        User admin = userRepository.findByUsername("admin_decision_test").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("admin_decision_test")
                        .email("admin_dec@test.com")
                        .password(passwordEncoder.encode("Password123!"))
                        .fullName("Admin Decision Test")
                        .role(Role.ROLE_SUPER_ADMIN)
                        .active(true)
                        .build())
        );
        UsernamePasswordAuthenticationToken adminAuth = new UsernamePasswordAuthenticationToken(
                admin.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"), new SimpleGrantedAuthority("DECISION_EXPLAIN"), new SimpleGrantedAuthority("DECISION_VIEW"))
        );
        adminToken = tokenProvider.generateToken(adminAuth);

        // Setup Viewer WITHOUT DECISION_EXPLAIN
        User viewer = userRepository.findByUsername("viewer_decision_test").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("viewer_decision_test")
                        .email("viewer_dec@test.com")
                        .password(passwordEncoder.encode("Password123!"))
                        .fullName("Viewer Decision Test")
                        .role(Role.ROLE_VIEWER)
                        .active(true)
                        .build())
        );
        UsernamePasswordAuthenticationToken viewerAuth = new UsernamePasswordAuthenticationToken(
                viewer.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_VIEWER"))
        );
        viewerToken = tokenProvider.generateToken(viewerAuth);
    }

    @Test
    @DisplayName("Should return 401 Unauthenticated when no JWT token provided")
    void testUnauthenticated401() throws Exception {
        mockMvc.perform(get("/api/v1/decisions/history"))
                .andExpect(status().isUnauthorized());

        DecisionExplanationRequest req = DecisionExplanationRequest.builder()
                .decisionType(DecisionType.ACCESS)
                .requiredPermission(Permission.PII_VIEW)
                .build();

        mockMvc.perform(post("/api/v1/decisions/explain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 403 Forbidden for Viewer role lacking DECISION_EXPLAIN permission")
    void testUnauthorized403() throws Exception {
        DecisionExplanationRequest req = DecisionExplanationRequest.builder()
                .decisionType(DecisionType.ACCESS)
                .requiredPermission(Permission.PII_VIEW)
                .build();

        mockMvc.perform(post("/api/v1/decisions/explain")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 200 OK for Super Admin authorized user")
    void testAuthorized200() throws Exception {
        DecisionExplanationRequest req = DecisionExplanationRequest.builder()
                .decisionType(DecisionType.ACCESS)
                .requiredPermission(Permission.PII_VIEW)
                .build();

        mockMvc.perform(post("/api/v1/decisions/explain")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Impersonation Prevention: Viewer trying to specify targetUsername should fail")
    void testImpersonationPrevention() throws Exception {
        DecisionExplanationRequest req = DecisionExplanationRequest.builder()
                .decisionType(DecisionType.ACCESS)
                .requiredPermission(Permission.PII_VIEW)
                .targetUsername("admin_decision_test")
                .build();

        mockMvc.perform(post("/api/v1/decisions/explain")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
