package com.exempal.shiftcounter.core;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Profile("prod")
public final class CriticalChangeAuditFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        chain.doFilter(request, response);
        String action = auditAction(request);
        if (action != null) {
            log.info("auditAction={} actor={} method={} path={} result={}", action,
                    request.getRemoteUser(), request.getMethod(), request.getRequestURI(), response.getStatus());
        }
    }

    private static String auditAction(HttpServletRequest request) {
        if ("GET".equals(request.getMethod())) return null;
        String path = request.getRequestURI();
        if (path.startsWith("/api/settings") || path.equals("/settings")) return "settings-change";
        if (path.equals("/api/stoppages/recalculate")) return "manual-reconcile";
        if (path.startsWith("/api/stoppages/") && path.contains("/explanations")) {
            return "explanation-change";
        }
        return null;
    }
}
