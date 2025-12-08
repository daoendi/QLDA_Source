package org.example.htmlfx.toolkits;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    public boolean canLogin(String credential, String password) {
        if (credential == null || credential.isBlank() || password == null || password.isBlank()) {
            return false;
        }

        String passwordQuery = "SELECT * FROM admins WHERE (email = ? OR admin_name = ?) AND password = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement passwordStatement = connection.prepareStatement(passwordQuery)) {
            passwordStatement.setString(1, credential);
            passwordStatement.setString(2, credential);
            passwordStatement.setString(3, password);
            try (ResultSet rs = passwordStatement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}
