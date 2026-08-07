package com.exempal.shiftcounter.testsupport;

import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import javax.sql.DataSource;

public final class DatabaseCleanupTestExecutionListener extends AbstractTestExecutionListener {

    private static final int AFTER_TRANSACTION_LISTENER_ORDER = 3500;

    @Override
    public int getOrder() {
        return AFTER_TRANSACTION_LISTENER_ORDER;
    }

    @Override
    public void afterTestMethod(TestContext testContext) {
        ApplicationContext context = testContext.getApplicationContext();
        if (context.getBeanNamesForType(DataSource.class).length == 0) {
            return;
        }
        new DatabaseCleaner(context.getBean(DataSource.class)).clean();
    }
}
