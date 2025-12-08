package org.example.htmlfx.book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookService {

    public boolean addNewBook(Connection connection, Book book, int amount) {
        if (book == null) return false;
        if (book.getTitle() == null || book.getTitle().isBlank()) return false; // BOOK-02
        if (amount < 0) return false; // BOOK-05
        if (book.getPrice() < 0) return false;

        String checkSql = "SELECT book_id FROM books WHERE bookmark = ?";
        String insertSql = "INSERT INTO books (bookmark, book_name, book_author, price, time_of_borrow, quantity_in_store, borrowing) VALUES (?, ?, ?, ?, 0, ?, 0)";
        try {
            PreparedStatement check = connection.prepareStatement(checkSql);
            check.setString(1, book.getbookmark());
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                // duplicate by bookmark -> block per BOOK-03
                return false;
            }

            PreparedStatement insert = connection.prepareStatement(insertSql);
            insert.setString(1, book.getbookmark());
            insert.setString(2, book.getTitle());
            insert.setString(3, book.getAuthors());
            insert.setDouble(4, book.getPrice()); // BOOK-06 accepts 0
            insert.setInt(5, amount);
            int n = insert.executeUpdate();
            return n > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updatePrice(Connection connection, String bookmark, double newPrice) {
        if (bookmark == null || bookmark.isBlank()) return false;
        if (newPrice < 0) return false; // BOOK-05 equivalent for price
        String sql = "UPDATE books SET price = ? WHERE bookmark = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setDouble(1, newPrice);
            ps.setString(2, bookmark);
            return ps.executeUpdate() > 0; // BOOK-04
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteBook(Connection connection, String bookmark) {
        if (bookmark == null || bookmark.isBlank()) return false;
        String check = "SELECT borrowing FROM books WHERE bookmark = ?";
        String del = "DELETE FROM books WHERE bookmark = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(check);
            ps.setString(1, bookmark);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false; // not found
            int borrowing = rs.getInt("borrowing");
            if (borrowing > 0) {
                return false; // BOOK-10 prevent delete when borrowed
            }
            PreparedStatement d = connection.prepareStatement(del);
            d.setString(1, bookmark);
            return d.executeUpdate() > 0; // BOOK-09
        } catch (SQLException e) {
            return false;
        }
    }
}
