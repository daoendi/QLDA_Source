package org.example.htmlfx.testutils;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class TestLogger implements BeforeTestExecutionCallback, AfterTestExecutionCallback, TestWatcher {

    private static final String START = "test.start";
    private static final String STATUS = "test.status";
    private static final String TCID = "test.tcid";
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        String name = context.getDisplayName();
        String className = context.getRequiredTestClass().getName();
        Instant now = Instant.now();
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(getStoreKey(context), now);
        // Assign a short sequential TestCase ID and store it for later lines
        String assigned = (String) context.getStore(ExtensionContext.Namespace.GLOBAL).get(getStoreKey(context) + ":tcid");
        if (assigned == null) {
            int n = COUNTER.incrementAndGet();
            assigned = String.format("T%03d", n);
            context.getStore(ExtensionContext.Namespace.GLOBAL).put(getStoreKey(context) + ":tcid", assigned);
        }
        System.out.println("[TEST START] " + now + " [TC=" + assigned + "] " + className + "#" + name);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        Instant start = (Instant) context.getStore(ExtensionContext.Namespace.GLOBAL).remove(getStoreKey(context));
        Instant end = Instant.now();
        long millis = start != null ? Duration.between(start, end).toMillis() : -1;
        String className = context.getRequiredTestClass().getName();
        String name = context.getDisplayName();
        String assigned = (String) context.getStore(ExtensionContext.Namespace.GLOBAL).get(getStoreKey(context) + ":tcid");
        String status = (String) context.getStore(ExtensionContext.Namespace.GLOBAL).remove(getStoreKey(context) + ":status");
        if (status == null) status = "UNKNOWN";
        System.out.println("[TEST END] " + end + " [TC=" + assigned + "] [" + status + "] " + className + "#" + name + " (duration=" + millis + " ms)");
        // Note: if the test failed, TestWatcher.testFailed has already printed failure details.
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        String assigned = (String) context.getStore(ExtensionContext.Namespace.GLOBAL).get(getStoreKey(context) + ":tcid");
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(getStoreKey(context) + ":status", "PASS");
        System.out.println("[TEST SUCCESS] " + Instant.now() + " [TC=" + assigned + "] " + context.getRequiredTestClass().getName() + "#" + context.getDisplayName());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        String assigned = (String) context.getStore(ExtensionContext.Namespace.GLOBAL).get(getStoreKey(context) + ":tcid");
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(getStoreKey(context) + ":status", "FAIL");
        System.out.println("[TEST FAILED] " + Instant.now() + " [TC=" + assigned + "] " + context.getRequiredTestClass().getName() + "#" + context.getDisplayName() + " - " + cause.getClass().getName() + ": " + cause.getMessage());
        // Print full stack trace to aid debugging
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        cause.printStackTrace(pw);
        System.out.println(sw.toString());
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        String assigned = (String) context.getStore(ExtensionContext.Namespace.GLOBAL).get(getStoreKey(context) + ":tcid");
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(getStoreKey(context) + ":status", "ABORTED");
        System.out.println("[TEST ABORTED] " + Instant.now() + " [TC=" + assigned + "] " + context.getRequiredTestClass().getName() + "#" + context.getDisplayName() + " - " + cause);
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        String assigned = (String) context.getStore(ExtensionContext.Namespace.GLOBAL).get(getStoreKey(context) + ":tcid");
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(getStoreKey(context) + ":status", "DISABLED");
        System.out.println("[TEST DISABLED] " + Instant.now() + " [TC=" + assigned + "] " + context.getRequiredTestClass().getName() + "#" + context.getDisplayName() + " - " + reason.orElse("no reason"));
    }

    private String getStoreKey(ExtensionContext context) {
        return START + ":" + context.getRequiredTestClass().getName() + ":" + context.getDisplayName();
    }

    private String getTestCaseId(ExtensionContext context) {
        String className = context.getRequiredTestClass().getSimpleName();
        String method = context.getTestMethod().map(Method::getName).orElse(context.getDisplayName());
        String base = className + "#" + method;
        // append short hex hash for compact uniqueness
        String hash = Integer.toHexString(base.hashCode()).toUpperCase();
        return base + "|" + hash;
    }
}
