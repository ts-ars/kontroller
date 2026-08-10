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
        if ((request.getRequestURI().startsWith("/api/settings") || request.getRequestURI().equals("/settings"))
                && !"GET".equals(request.getMethod())) {
            log.info("auditAction=settings-change actor={} method={} path={} result={}",
                    request.getRemoteUser(), request.getMethod(), request.getRequestURI(), response.getStatus());
        }
    }
}
