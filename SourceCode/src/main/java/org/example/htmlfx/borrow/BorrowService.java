package org.example.htmlfx.borrow;

import org.example.htmlfx.toolkits.Checked;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class BorrowService {

    public boolean createBorrow(Connection connection,
                                String memberId,
                                String bookId,
                                String borrowDate,
                                String returnDate) {
        if (memberId == null || memberId.isBlank() || bookId == null || bookId.isBlank()) return false;
        if (borrowDate != null && !borrowDate.isBlank() && !Checked.isValidDate(borrowDate)) return false;
        if (returnDate != null && !returnDate.isBlank() && !Checked.isValidDate(returnDate)) return false;

        if (borrowDate != null && !borrowDate.isBlank() && returnDate != null && !returnDate.isBlank()) {
            LocalDate b = LocalDate.parse(borrowDate);
            LocalDate r = LocalDate.parse(returnDate);
            if (r.isBefore(b)) return false; // BOR-06
        }

        if (!memberExists(connection, memberId)) return false; // BOR-03
        Integer remaining = getRemainingQuantity(connection, bookId);
        if (remaining == null) return false; // BOR-04
        if (remaining <= 0) return false; // BOR-02

        String sql = (returnDate != null && !returnDate.isBlank())
                ? "INSERT INTO borrow (member_id, book_id, borrow_date, returned_date) VALUES (?, ?, ?, ?)"
                : (borrowDate != null && !borrowDate.isBlank())
                    ? "INSERT INTO borrow (member_id, book_id, borrow_date) VALUES (?, ?, ?)"
                    : "INSERT INTO borrow (member_id, book_id, borrow_date) VALUES (?, ?, CURRENT_DATE)";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, memberId);
            ps.setString(2, bookId);
            if (sql.contains("?, ?, ?)") && !sql.contains("returned_date")) {
                // with explicit borrow_date only
                ps.setString(3, borrowDate);
            } else if (sql.contains("returned_date")) {
                ps.setString(3, borrowDate);
                ps.setString(4, returnDate);
            }
            return ps.executeUpdate() > 0; // BOR-01/05
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

    private Integer getRemainingQuantity(Connection connection, String bookId) {
        String sql = "SELECT remaining_quantity FROM books WHERE book_id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}
        return null;
    }
}
