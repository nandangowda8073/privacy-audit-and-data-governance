package com.piiscan.report.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piiscan.report.dto.ReportGenerateRequest;
import com.piiscan.report.enums.ReportFormat;
import com.piiscan.report.enums.ReportType;
import org.junit.jupiter.api.DisplayName;
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
public class ReportSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/reports without authentication should return 401")
    void unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/reports without REPORT_VIEW authority should return 403")
    @WithMockUser(username = "unauthorized_user", authorities = {"DASHBOARD_VIEW"})
    void missingReportView_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/reports/generate without REPORT_GENERATE authority should return 403")
    @WithMockUser(username = "viewer_only", authorities = {"REPORT_VIEW"})
    void missingReportGenerate_shouldReturn403() throws Exception {
        ReportGenerateRequest req = ReportGenerateRequest.builder()
                .reportType(ReportType.SCAN_SUMMARY)
                .format(ReportFormat.PDF)
                .build();

        mockMvc.perform(post("/api/reports/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/reports/1/download without REPORT_DOWNLOAD authority should return 403")
    @WithMockUser(username = "view_only", authorities = {"REPORT_VIEW"})
    void missingReportDownload_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/reports/1/download"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/reports with REPORT_VIEW authority should return 200")
    @WithMockUser(username = "auditor_user", authorities = {"REPORT_VIEW"})
    void authorizedReportView_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk());
    }
}
