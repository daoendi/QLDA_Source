package org.example.htmlfx.income;

import org.example.htmlfx.toolkits.Checked;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentService {

    public boolean createPayment(Connection connection,
                                 String memberId,
                                 String bookId,
                                 String paymentOrQuantity,
                                 String orderDate) {
        if (memberId == null || memberId.isBlank() || bookId == null || bookId.isBlank()) return false;
        // validate money/quantity: numeric and > 0
        int quantity;
        try {
            quantity = Integer.parseInt(paymentOrQuantity);
        } catch (NumberFormatException e) {
            return false; // PAY-02 non-numeric
        }
        if (quantity <= 0) return false; // PAY-02 negative/zero

        if (!memberExists(connection, memberId)) return false; // PAY-03
        if (!bookExists(connection, bookId)) return false;     // PAY-03

        // honor optional orderDate; if null -> current_timestamp
        String sql = (orderDate != null && !orderDate.isBlank())
                ? "INSERT INTO payment (member_id, book_id, quantity_of_order, order_date) VALUES (?, ?, ?, ?)"
                : "INSERT INTO payment (member_id, book_id, quantity_of_order, order_date) VALUES (?, ?, ?, current_timestamp)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, memberId);
            ps.setString(2, bookId);
            ps.setInt(3, quantity);
            if (sql.contains("?, ?)")) {
                ps.setString(4, orderDate);
            }
            return ps.executeUpdate() > 0; // PAY-01
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean memberExists(Connection connection, String memberId) {
        String sql = "SELECT COUNT(*) FROM members WHERE member_id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, memberId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException ignored) {}
        return false;
    }

    private boolean bookExists(Connection connection, String bookId) {
        String sql = "SELECT COUNT(*) FROM books WHERE book_id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException ignored) {}
        return false;
    }
}
