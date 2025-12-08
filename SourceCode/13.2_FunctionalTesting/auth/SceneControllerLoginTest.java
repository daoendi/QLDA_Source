package org.example.htmlfx.auth;

import javafx.embed.swing.JFXPanel; // initializes JavaFX toolkit
import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.htmlfx.SceneController;
import org.example.htmlfx.testutils.TestLogger;
import org.example.htmlfx.toolkits.Alert;
import org.example.htmlfx.toolkits.DatabaseConnection;
import org.example.htmlfx.toolkits.Music;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SceneControllerLoginTest {

    @RegisterExtension
    TestLogger testLogger = new TestLogger();

    @BeforeAll
    static void initFx() {
        // Initialize JavaFX toolkit so we can construct controls
        new JFXPanel();
    }

    private void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    @DisplayName("AUTH-10 | handleSignIn success -> shows info and navigates")
    void handleSignIn_success_navigates() throws Exception {
        // Mock DB twice: first for AuthService.canLogin, second for admin detail fetch
        try (MockedStatic<DatabaseConnection> dc = mockStatic(DatabaseConnection.class);
             MockedStatic<Alert> alerts = mockStatic(Alert.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps1 = mock(PreparedStatement.class);
            PreparedStatement ps2 = mock(PreparedStatement.class);
            ResultSet rs1 = mock(ResultSet.class);
            ResultSet rs2 = mock(ResultSet.class);

            dc.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps1, ps2);
            when(ps1.executeQuery()).thenReturn(rs1);
            when(ps2.executeQuery()).thenReturn(rs2);

            // AuthService.canLogin -> true
            when(rs1.next()).thenReturn(true);

            // Admin details fetch
            when(rs2.next()).thenReturn(true);
            when(rs2.getString("admin_id")).thenReturn("1");
            when(rs2.getString("firstName")).thenReturn("Nguyen");
            when(rs2.getString("lastName")).thenReturn("B");
            when(rs2.getString("email")).thenReturn("admin2@gmail.com");
            when(rs2.getString("password")).thenReturn("Admin@123");
            when(rs2.getString("admin_name")).thenReturn("admin2");
            when(rs2.getString("gender")).thenReturn("M");
            when(rs2.getString("phone")).thenReturn("0123456789");
            when(rs2.getString("image")).thenReturn(null);
            when(rs2.getString("birth")).thenReturn("2002-10-01");

            // Spy controller and replace navigation with a no-op
            SceneController controller = spy(new SceneController());

            // prevent real music playback
            SceneController.setMusic(mock(Music.class));

            // Set required state: onSignin=true and input fields with credential/password
            setPrivate(controller, "onSignin", true);
            TextField user = new TextField("admin2");
            PasswordField pass = new PasswordField();
            pass.setText("Admin@123");
            setPrivate(controller, "email_signin", user);
            setPrivate(controller, "password_signin", pass);

            doNothing().when(controller).handleSwitchScene(any());

            controller.handleSignIn((ActionEvent) null);

            // verify info alert and navigation called
            alerts.verify(() -> Alert.showAlert(any(), eq("Success"), contains("Login success")));
            verify(controller, times(1)).handleSwitchScene(any());

            // verify parameters set to prepared statements
            verify(ps1).setString(1, "admin2");
            verify(ps1).setString(2, "admin2");
            verify(ps1).setString(3, "Admin@123");
        }
    }

    @Test
    @DisplayName("AUTH-11 | handleSignIn wrong password -> shows error, no navigation")
    void handleSignIn_wrong_password_shows_error() throws Exception {
        try (MockedStatic<DatabaseConnection> dc = mockStatic(DatabaseConnection.class);
             MockedStatic<Alert> alerts = mockStatic(Alert.class)) {

            Connection conn = mock(Connection.class);
            PreparedStatement ps1 = mock(PreparedStatement.class);
            ResultSet rs1 = mock(ResultSet.class);

            dc.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps1);
            when(ps1.executeQuery()).thenReturn(rs1);

            when(rs1.next()).thenReturn(false); // login fail

            SceneController controller = spy(new SceneController());
            setPrivate(controller, "onSignin", true);
            setPrivate(controller, "email_signin", new TextField("admin2"));
            PasswordField pass = new PasswordField();
            pass.setText("wrong123");
            setPrivate(controller, "password_signin", pass);

            doNothing().when(controller).handleSwitchScene(any());

            controller.handleSignIn((ActionEvent) null);

            alerts.verify(() -> Alert.showAlert(any(), eq("Error"), contains("Invalid credentials")));
            verify(controller, never()).handleSwitchScene(any());
        }
    }
}
