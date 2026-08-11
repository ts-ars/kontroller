package com.exempal.shiftcounter.core;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CriticalChangeAuditFilterTest {
    @Test
    void auditsEveryCriticalManualMutationAndIgnoresReads() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(CriticalChangeAuditFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            CriticalChangeAuditFilter filter = new CriticalChangeAuditFilter();
            invoke(filter, "POST", "/api/settings/group-1");
            invoke(filter, "POST", "/api/stoppages/recalculate");
            invoke(filter, "PUT", "/api/stoppages/7/explanations/3");
            invoke(filter, "GET", "/api/stoppages/7/explanations");

            assertThat(appender.list).extracting(ILoggingEvent::getFormattedMessage)
                    .anyMatch(message -> message.contains("auditAction=settings-change"))
                    .anyMatch(message -> message.contains("auditAction=manual-reconcile"))
                    .anyMatch(message -> message.contains("auditAction=explanation-change"))
                    .noneMatch(message -> message.contains("method=GET"));
        } finally {
            logger.detachAppender(appender);
        }
    }

    private static void invoke(CriticalChangeAuditFilter filter, String method, String path) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRemoteUser("operator");
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
    }
}
