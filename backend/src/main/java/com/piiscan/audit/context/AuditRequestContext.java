package com.piiscan.audit.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditRequestContext {

    private static final ThreadLocal<AuditRequestContext> CONTEXT = new ThreadLocal<>();

    private String requestId;
    private String ipAddress;
    private String userAgent;
    private String endpoint;
    private String httpMethod;

    public static AuditRequestContext get() {
        AuditRequestContext ctx = CONTEXT.get();
        if (ctx == null) {
            ctx = new AuditRequestContext();
            CONTEXT.set(ctx);
        }
        return ctx;
    }

    public static void set(AuditRequestContext context) {
        CONTEXT.set(context);
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
