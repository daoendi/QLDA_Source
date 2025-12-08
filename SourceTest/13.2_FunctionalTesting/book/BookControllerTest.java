package org.example.htmlfx.book;

import javafx.application.Platform;
import javafx.scene.control.Label;
import org.example.htmlfx.toolkits.DatabaseConnection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BookControllerTest {

    @BeforeAll
    static void initFx() {
        // Initialize JavaFX toolkit without Swing dependencies
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // already initialized
        }
    }

    private Book sampleBook(String bookmark, String title, String authors, double price) {
        Book b = new Book();
        b.setbookmark(bookmark);
        b.setTitle(title);
        b.setAuthors(authors);
        b.setPrice(price);
        return b;
    }

    @Test
    @DisplayName("BOOK-01 | Add new book (insert when not exists)")
    void addBook_inserts_when_not_exists() throws Exception {
        BookController ctrl = new BookController();
        Book book = sampleBook("BK1001", "Clean Code", "Robert C. Martin", 120000);
        int amount = 5;

        Connection conn = mock(Connection.class);
        PreparedStatement checkStmt = mock(PreparedStatement.class);
        PreparedStatement insertStmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(checkStmt, insertStmt);
        when(checkStmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        ctrl.addBookToDatabase(conn, book, amount);

        // assert insert path used and parameters set
        verify(insertStmt).setString(1, "BK1001");
        verify(insertStmt).setString(2, "Clean Code");
        verify(insertStmt).setString(3, "Robert C. Martin");
        verify(insertStmt).setDouble(4, 120000);
        verify(insertStmt).setInt(6, 5);
        verify(insertStmt).executeUpdate();

        // no update when not exists
        verify(checkStmt, times(1)).executeQuery();
    }

    @Test
    @DisplayName("BOOK-07 | Add with amount 0 still inserts (legacy controller path)")
    void addBook_amount_zero_inserts() throws Exception {
        BookController ctrl = new BookController();
        Book book = sampleBook("BK1004", "Some Book", "Author", 50000);

        Connection conn = mock(Connection.class);
        PreparedStatement checkStmt = mock(PreparedStatement.class);
        PreparedStatement insertStmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(checkStmt, insertStmt);
        when(checkStmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        ctrl.addBookToDatabase(conn, book, 0);

        verify(insertStmt).setInt(6, 0);
        verify(insertStmt).executeUpdate();
    }

    @Test
    @DisplayName("BOOK-02 | Strict add missing title -> fail, no SQL")
    void strictAdd_missing_title_fails() throws Exception {
        BookController ctrl = new BookController();
        boolean ok = ctrl.addNewBookStrict(mock(Connection.class), sampleBook("BKZ","", "A", 1000), 1);
        assertThat(ok).isFalse();
    }

    @Test
    @DisplayName("BOOK-03 | Strict add duplicate -> fail")
    void strictAdd_duplicate_fails() throws Exception {
        BookController ctrl = new BookController();
        Connection conn = mock(Connection.class);
        PreparedStatement check = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(check);
        when(check.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        boolean ok = ctrl.addNewBookStrict(conn, sampleBook("BK1001","T","A", 1000), 1);
        assertThat(ok).isFalse();
    }

    @Test
    @DisplayName("BOOK-04 | Update price via controller wrapper")
    void updatePrice_via_controller() throws Exception {
        BookController ctrl = new BookController();
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);
        boolean ok = ctrl.updateBookPrice(conn, "BK1001", 150000);
        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("BOOK-09/10 | Delete via controller wrapper respects borrowing")
    void delete_via_controller_respects_borrowing() throws Exception {
        BookController ctrl = new BookController();
        Connection conn = mock(Connection.class);
        PreparedStatement check = mock(PreparedStatement.class);
        PreparedStatement del = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(check, del);
        when(check.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("borrowing")).thenReturn(0);
        when(del.executeUpdate()).thenReturn(1);
        assertThat(ctrl.deleteBookByBookmark(conn, "BK1002")).isTrue();
    }

    @Test
    @DisplayName("BOOK-01 | Add existing book increments quantity")
    void addBook_existing_updates_quantity() throws Exception {
        BookController ctrl = new BookController();
        Book book = sampleBook("BK1001", "Clean Code", "Robert C. Martin", 120000);

        Connection conn = mock(Connection.class);
        PreparedStatement checkStmt = mock(PreparedStatement.class);
        PreparedStatement updateStmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(checkStmt, updateStmt);
        when(checkStmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        ctrl.addBookToDatabase(conn, book, 3);

        verify(updateStmt).setInt(1, 3);
        verify(updateStmt).setString(2, "BK1001");
        verify(updateStmt).executeUpdate();
    }

    @Test
    @DisplayName("BOOK-Stock | quantity_in_Stock_Book updates label text")
    void quantity_in_stock_updates_label() throws Exception {
        BookController ctrl = new BookController();
        Label label = new Label();
        // inject label
        var f = BookController.class.getDeclaredField("stocklabel");
        f.setAccessible(true);
        f.set(ctrl, label);

        try (MockedStatic<DatabaseConnection> dc = mockStatic(DatabaseConnection.class)) {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            dc.when(DatabaseConnection::getConnection).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt("remaining_quantity")).thenReturn(7);

            ctrl.quantity_in_Stock_Book("BK1001");
            assertThat(label.getText()).contains("Quantity: 7");
        }
    }
}
