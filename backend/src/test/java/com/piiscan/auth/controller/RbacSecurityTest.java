package com.piiscan.auth.controller;

import com.piiscan.auth.entity.Role;
import com.piiscan.security.JwtTokenProvider;
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
public class RbacSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "superadmin", authorities = {"USER_VIEW", "DATABASE_CREATE", "ROLE_MANAGE"})
    public void superAdmin_hasAccessToCreateDatabase() throws Exception {
        mockMvc.perform(post("/api/v1/databases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"connectionName\":\"test\",\"databaseType\":\"POSTGRESQL\",\"host\":\"localhost\",\"port\":5432,\"databaseName\":\"test\",\"username\":\"sa\",\"password\":\"pw\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "viewer", authorities = {"PII_VIEW", "DASHBOARD_VIEW"})
    public void viewer_isForbiddenToCreateDatabase() throws Exception {
        mockMvc.perform(post("/api/v1/databases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"connectionName\":\"test\",\"databaseType\":\"POSTGRESQL\",\"host\":\"localhost\",\"port\":5432,\"databaseName\":\"test\",\"username\":\"sa\",\"password\":\"pw\"}"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "viewer", authorities = {"PII_VIEW", "DASHBOARD_VIEW"})
    public void viewer_hasAccessToPiiStats() throws Exception {
        mockMvc.perform(get("/api/pii/stats"))
                .andExpect(status().isOk());
    }

    @Test
    public void unauthenticated_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/pii/stats"))
                .andExpect(status().isUnauthorized());
    }
}
