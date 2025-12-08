package org.example.htmlfx.dashboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardService {

    public int totalBooks(Connection conn) {
        return scalarInt(conn, "SELECT COUNT(*) FROM books");
    }

    public int totalMembers(Connection conn) {
        return scalarInt(conn, "SELECT COUNT(*) FROM members");
    }

    public int totalBorrowsToday(Connection conn) {
        return scalarInt(conn, "SELECT COUNT(*) FROM borrow WHERE borrow_date = CURDATE()");
    }

    public double totalPayment(Connection conn) {
        String sql = "SELECT SUM(price) FROM payment";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException ignored) {}
        return 0.0;
    }

    private int scalarInt(Connection conn, String sql) {
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}
        return 0;
    }
}
