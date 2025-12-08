package org.example.htmlfx.auth;

import org.example.htmlfx.testutils.TestLogger;
import org.example.htmlfx.toolkits.AuthService;
import org.example.htmlfx.toolkits.DatabaseConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @RegisterExtension
    TestLogger testLogger = new TestLogger();

    private AuthService service = new AuthService();

    @Test
    @DisplayName("AUTH-10 | Login success with newly registered (admin2/Adm1n@123)")
    void auth10_login_success() throws Exception {
        try (MockedStatic<DatabaseConnection> dc = mockStatic(DatabaseConnection.class)) {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dc.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);

            boolean ok = service.canLogin("admin2", "Admin@123");
            assertThat(ok).isTrue();

            verify(ps).setString(1, "admin2");
            verify(ps).setString(2, "admin2");
            verify(ps).setString(3, "Admin@123");
        }
    }

    @Test
    @DisplayName("AUTH-11 | Wrong password -> fail")
    void auth11_wrong_password() throws Exception {
        try (MockedStatic<DatabaseConnection> dc = mockStatic(DatabaseConnection.class)) {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dc.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            boolean ok = service.canLogin("admin2", "wrong123");
            assertThat(ok).isFalse();
        }
    }

    @Test
    @DisplayName("AUTH-12 | Non-existent account -> fail")
    void auth12_non_existent_account() throws Exception {
        try (MockedStatic<DatabaseConnection> dc = mockStatic(DatabaseConnection.class)) {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dc.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            boolean ok = service.canLogin("ghost", "123");
            assertThat(ok).isFalse();
        }
    }

    @Test
    @DisplayName("AUTH-13 | Required fields empty -> fail")
    void auth13_required_fields() {
        assertThat(service.canLogin("", "abc")).isFalse();
        assertThat(service.canLogin("user", "")).isFalse();
        assertThat(service.canLogin(null, "abc")).isFalse();
        assertThat(service.canLogin("user", null)).isFalse();
    }
}
