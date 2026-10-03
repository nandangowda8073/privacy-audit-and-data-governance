package com.piiscan.masking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piiscan.auth.entity.Role;
import com.piiscan.masking.dto.MaskingPolicyRequest;
import com.piiscan.masking.enums.MaskingType;
import com.piiscan.pii.enums.PiiEntityType;
import com.piiscan.pii.enums.RiskLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MaskingPolicyControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- GET /api/masking/policies ---

    @Test
    @WithMockUser(username = "super_admin", authorities = {"MASKING_POLICY_VIEW"})
    void getPolicies_withSuperAdmin_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/masking/policies"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "sec_officer", authorities = {"MASKING_POLICY_VIEW"})
    void getPolicies_withSecurityOfficer_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/masking/policies"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "auditor", authorities = {"MASKING_POLICY_VIEW"})
    void getPolicies_withAuditor_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/masking/policies"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "viewer", authorities = {"DASHBOARD_VIEW"})
    void getPolicies_withViewer_shouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/masking/policies"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPolicies_unauthenticated_shouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/api/masking/policies"))
                .andExpect(status().isUnauthorized());
    }

    // --- POST /api/masking/policies ---

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void createPolicy_withSuperAdmin_shouldSucceed() throws Exception {
        MaskingPolicyRequest request = new MaskingPolicyRequest();
        request.setRole(Role.ROLE_VIEWER);
        request.setPiiType(PiiEntityType.EMAIL);
        request.setRiskLevel(RiskLevel.MEDIUM);
        request.setMaskingType(MaskingType.PARTIAL);
        request.setDescription("Test policy");
        request.setEnabled(true);

        mockMvc.perform(post("/api/masking/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "AUDITOR")
    void createPolicy_withAuditor_shouldBeForbidden() throws Exception {
        MaskingPolicyRequest request = new MaskingPolicyRequest();
        request.setRole(Role.ROLE_VIEWER);
        request.setPiiType(PiiEntityType.EMAIL);
        request.setRiskLevel(RiskLevel.MEDIUM);
        request.setMaskingType(MaskingType.PARTIAL);
        request.setDescription("Test policy");
        request.setEnabled(true);

        mockMvc.perform(post("/api/masking/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void createPolicy_withViewer_shouldBeForbidden() throws Exception {
        MaskingPolicyRequest request = new MaskingPolicyRequest();
        mockMvc.perform(post("/api/masking/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
