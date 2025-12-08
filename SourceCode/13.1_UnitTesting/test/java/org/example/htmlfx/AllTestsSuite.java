package org.example.htmlfx;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

@Suite
@SelectPackages("org.example.htmlfx")
/**
 * Test suite entry point that selects all tests under the package
 * `org.example.htmlfx`.
 *
 * Usage:
 * - Run as a JUnit Suite in your IDE, or
 * - From Maven: `mvn -Dtest=AllTestsSuite test`, or
 * - Run the `main` method directly (uses JUnit platform launcher on the test classpath).
 */
public class AllTestsSuite {
    public static void main(String[] args) {
        // Programmatically run the selected package and capture a summary
        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage("org.example.htmlfx"))
                .build();

        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();
        System.out.println("\n---- TEST SUMMARY ----");
        System.out.println("Found tests: " + summary.getTestsFoundCount());
        System.out.println("Started: " + summary.getTestsStartedCount());
        System.out.println("Succeeded: " + summary.getTestsSucceededCount());
        System.out.println("Failed: " + summary.getTestsFailedCount());
        System.out.println("Aborted: " + summary.getTestsAbortedCount());
        System.out.println("Skipped: " + summary.getTestsSkippedCount());

        if (summary.getTestsFailedCount() > 0) {
            System.out.println("\nFailed tests details:");
            summary.getFailures().forEach(f -> System.out.println(f.getTestIdentifier().getDisplayName() + " -> " + f.getException()));
            System.exit(1);
        }
    }
}
