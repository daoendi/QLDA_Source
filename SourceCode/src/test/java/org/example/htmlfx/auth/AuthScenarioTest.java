package org.example.htmlfx.auth;

import org.example.htmlfx.testutils.TestLogger;
import org.example.htmlfx.toolkits.Checked;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Auth registration scenarios mapped to current codebase behavior.
 *
 * Notes:
 * - Current signup flow (SceneController.handleSignUp) only validates name, email (@gmail.com), and password non-empty.
 * - There is no confirm password field nor password strength rule in production code.
 * - Phone and DOB validations exist for member add (Member_Add) via Checked, not in admin signup.
 * - DB/UI interactions (success, duplicate username/email, cancel) require JavaFX + DB and are marked @Disabled here.
 */
public class AuthScenarioTest {

    @RegisterExtension
    TestLogger testLogger = new TestLogger();

    // AUTH-05: Email không hợp lệ (in Register) -> uses Checked.isValidEmail
    @Test
    @DisplayName("AUTH-05 | Email không hợp lệ: user@yahoo.com -> false")
    void auth05_invalid_email() {
        assertThat(Checked.isValidEmail("user@yahoo.com")).isFalse();
        assertThat(Checked.isValidEmail("user@gmail.com")).isTrue();
        // domain case must match exactly 'gmail.com'
        assertThat(Checked.isValidEmail("user@GMAIL.com")).isFalse();
    }

    // AUTH-06: Phone không hợp lệ (in Register/Member add) -> uses Checked.isValidPhone
    @Test
    @DisplayName("AUTH-06 | Phone không hợp lệ: 1234567890 -> false")
    void auth06_invalid_phone() {
        assertThat(Checked.isValidPhone("0123456789")).isTrue();
        assertThat(Checked.isValidPhone("1234567890")).isFalse();
        assertThat(Checked.isValidPhone("01234")).isFalse();
    }

    // AUTH-07: DOB sai format/giá trị (in Register/Member add) -> uses Checked.isValidDate with strict yyyy-MM-dd
    @Test
    @DisplayName("AUTH-07 | DOB sai: 01-12-2002, 2002-02-30 -> false; 2002-10-01 -> true")
    void auth07_invalid_dob() {
        assertThat(Checked.isValidDate("2002-10-01")).isTrue();
        assertThat(Checked.isValidDate("01-12-2002")).isFalse();
        assertThat(Checked.isValidDate("2002-02-30")).isFalse();
    }

    // The following scenarios are UI/DB-driven in current code and not unit-testable without adding UI harness + static mocking.

    // AUTH-01: Đăng ký thành công -> requires DB insert via SceneController.handleSignUp
    @Test
    @Disabled("Requires JavaFX UI + static DB mocking to simulate insert")
    @DisplayName("AUTH-01 | Đăng ký thành công (DB/UI integration)")
    void auth01_register_success_integration() {}

    // AUTH-02: Đăng ký thiếu trường bắt buộc -> UI-only check, alerts and early return
    @Test
    @Disabled("Handled via JavaFX controller alerts; not unit-testable without UI harness")
    @DisplayName("AUTH-02 | Thiếu trường bắt buộc (UI validation)")
    void auth02_missing_required_fields_ui() {}

    // AUTH-03: Đăng ký trùng username -> requires DB lookup in admins table
    @Test
    @Disabled("Requires mocking DatabaseConnection + PreparedStatements for duplicate checks")
    @DisplayName("AUTH-03 | Username trùng (DB duplicate)")
    void auth03_duplicate_username_db() {}

    // AUTH-04: Password và Confirm không khớp -> now validated via Checked.passwordsMatch
    @Test
    @DisplayName("AUTH-04 | Password-Confirm mismatch -> false")
    void auth04_password_confirm_mismatch() {
        assertThat(Checked.passwordsMatch("Admin@123", "Admin@321")).isFalse();
        assertThat(Checked.passwordsMatch("Admin@123", "Admin@123")).isTrue();
    }

    // AUTH-08: Password yếu -> validated via Checked.isStrongPassword
    @Test
    @DisplayName("AUTH-08 | Password yếu: '123' weak; 'Admin@123' strong")
    void auth08_password_strength_rule() {
        assertThat(Checked.isStrongPassword("123")).isFalse();
        assertThat(Checked.isStrongPassword("password")).isFalse();
        assertThat(Checked.isStrongPassword("Password1")).isFalse(); // missing special
        assertThat(Checked.isStrongPassword("Admin@123")).isTrue();
    }

    // AUTH-09: Hủy đăng ký -> UI back/cancel action
    @Test
    @Disabled("Cancel/back is a JavaFX navigation action; UI test only")
    @DisplayName("AUTH-09 | Hủy đăng ký (UI navigation)")
    void auth09_cancel_registration_ui() {}
}
