package com.piiscan.audit.security;

import com.piiscan.audit.entity.AuditLog;
import com.piiscan.audit.repository.AuditLogRepository;
import com.piiscan.pii.dto.DetectionResponse;
import com.piiscan.pii.service.PiiDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RawPiiSecurityTest {

    @Autowired
    private PiiDetectionService piiDetectionService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private static final String TEST_AADHAAR = "456789123456";
    private static final String TEST_PAN = "ABCDE1234F";
    private static final String TEST_CREDIT_CARD = "4532123412345678";

    @BeforeEach
    public void setUp() {
        auditLogRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "security_officer", roles = {"SECURITY_OFFICER"})
    public void testRawPiiIsNeverStoredInAuditDatabase() {
        String testText = String.format(
                "Customer record: Aadhaar = %s, PAN = %s, Card = %s",
                TEST_AADHAAR, TEST_PAN, TEST_CREDIT_CARD
        );

        // Perform PII detection
        DetectionResponse response = piiDetectionService.detectFromText(testText, "SECURITY_TEST_INPUT", "security_officer");
        assertThat(response).isNotNull();

        // Fetch all audit records generated
        List<AuditLog> allAuditLogs = auditLogRepository.findAll();
        assertThat(allAuditLogs).isNotEmpty();

        // Verify NO audit log record contains raw sensitive values in any string field
        for (AuditLog log : allAuditLogs) {
            assertNoRawPii(log.getDescription());
            assertNoRawPii(log.getReason());
            assertNoRawPii(log.getResourceId());
            assertNoRawPii(log.getResourceType());
            assertNoRawPii(log.getUsername());
            assertNoRawPii(log.getPiiType());
            assertNoRawPii(log.getRiskLevel());
            assertNoRawPii(log.getMaskingType());
        }
    }

    private void assertNoRawPii(String fieldContent) {
        if (fieldContent == null) return;
        assertThat(fieldContent).doesNotContain(TEST_AADHAAR);
        assertThat(fieldContent).doesNotContain(TEST_PAN);
        assertThat(fieldContent).doesNotContain(TEST_CREDIT_CARD);
    }
}
