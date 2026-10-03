package com.piiscan.audit.filter;

import com.piiscan.audit.context.AuditRequestContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements Filter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            String requestId = httpRequest.getHeader(CORRELATION_ID_HEADER);
            if (requestId == null || requestId.isBlank()) {
                requestId = "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            }

            String ipAddress = extractClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            if (userAgent != null && userAgent.length() > 300) {
                userAgent = userAgent.substring(0, 300);
            }

            AuditRequestContext ctx = AuditRequestContext.builder()
                    .requestId(requestId)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .endpoint(httpRequest.getRequestURI())
                    .httpMethod(httpRequest.getMethod())
                    .build();

            AuditRequestContext.set(ctx);
            MDC.put("requestId", requestId);
            httpResponse.setHeader(CORRELATION_ID_HEADER, requestId);

            try {
                chain.doFilter(request, response);
            } finally {
                AuditRequestContext.clear();
                MDC.remove("requestId");
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
