package com.piiscan.dashboard.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DashboardSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Verify 401 Unauthorized for Unauthenticated Request to Dashboard Overview")
    void testUnauthenticatedAccessDenied() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "no_perm_user", authorities = {"SOME_OTHER_PERMISSION"})
    @DisplayName("Verify 403 Forbidden for User Missing DASHBOARD_VIEW Permission")
    void testUserWithoutPermissionForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/overview"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "dash_auditor", authorities = {"DASHBOARD_VIEW"})
    @DisplayName("Verify 200 OK for Authorized User with DASHBOARD_VIEW Permission")
    void testAuthorizedUserSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/overview"))
                .andExpect(status().isOk());
    }
}
