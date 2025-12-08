package org.example.htmlfx.toolkits;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection databaseLink;

    public static Connection getConnection() {
        String databaseName = "library_management";
        String databaseUser = "root";
        String databasePassword = "123456";
        String databaseUrl = "jdbc:mysql://localhost:3306/" + databaseName;

        try{
            // Return existing open connection if available
            try {
                if (databaseLink != null && !databaseLink.isClosed()) {
                    return databaseLink;
                }
            } catch (SQLException ignored) {
                // proceed to create a new connection
            }
            Class.forName("com.mysql.cj.jdbc.Driver");
            databaseLink = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);

        } catch (Exception e) {
            e.printStackTrace();
            e.getCause();
        }
        return databaseLink;
    }
}