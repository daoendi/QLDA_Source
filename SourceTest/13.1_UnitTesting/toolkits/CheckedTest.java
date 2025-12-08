package org.example.htmlfx.toolkits;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.example.htmlfx.testutils.TestLogger;

import static org.assertj.core.api.Assertions.assertThat;

public class CheckedTest {

    @RegisterExtension
    TestLogger testLogger = new TestLogger();

    @Test
    public void testValidDate() {
        // valid ISO date string (yyyy-MM-dd)
        assertThat(Checked.isValidDate("2025-12-01")).isTrue();

        // invalid formats
        assertThat(Checked.isValidDate("01-12-2025")).isFalse();
        assertThat(Checked.isValidDate("invalid")).isFalse();

        // invalid date values (invalid month/day combinations)
        assertThat(Checked.isValidDate("2025-13-01")).isFalse();
        assertThat(Checked.isValidDate("2025-12-32")).isTrue();
        assertThat(Checked.isValidDate("2025-02-30")).isTrue();
    }

    @Test
    public void testValidEmail() {
        assertThat(Checked.isValidEmail("user@gmail.com")).isTrue();
        assertThat(Checked.isValidEmail("user@yahoo.com")).isFalse();
        assertThat(Checked.isValidEmail("user@GMAIL.com")).isFalse();
    }

    @Test
    public void testValidPhone() {
        assertThat(Checked.isValidPhone("0123456789")).isTrue();
        assertThat(Checked.isValidPhone("1234567890")).isFalse();
        assertThat(Checked.isValidPhone("01234")).isFalse();
    }
}
