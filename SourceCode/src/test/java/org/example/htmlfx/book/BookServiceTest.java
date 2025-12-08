package org.example.htmlfx.book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BookServiceTest {

    private BookService service = new BookService();

    private Book book(String bookmark, String title, String authors, double price) {
        Book b = new Book();
        b.setbookmark(bookmark);
        b.setTitle(title);
        b.setAuthors(authors);
        b.setPrice(price);
        return b;
    }

    @Test
    @DisplayName("BOOK-02 | Add missing title -> fail, no insert")
    void add_missing_title_fails() throws Exception {
        Connection conn = mock(Connection.class);
        boolean ok = service.addNewBook(conn, book("BK1002","", "A", 50000), 3);
        assertThat(ok).isFalse();
        verify(conn, never()).prepareStatement(anyString());
    }

    @Test
    @DisplayName("BOOK-03 | Add duplicate ID (bookmark) -> fail")
    void add_duplicate_bookmark_fails() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement check = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(check);
        when(check.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true); // exists
        boolean ok = service.addNewBook(conn, book("BK1001","Any","Auth", 50000), 2);
        assertThat(ok).isFalse();
        verify(check, times(1)).executeQuery();
    }

    @Test
    @DisplayName("BOOK-04 | Update price -> success")
    void update_price_success() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);
        boolean ok = service.updatePrice(conn, "BK1001", 150000);
        assertThat(ok).isTrue();
        verify(ps).setDouble(1, 150000);
        verify(ps).setString(2, "BK1001");
    }

    @Test
    @DisplayName("BOOK-05 | Add with negative amount -> fail")
    void add_negative_amount_fails() throws Exception {
        Connection conn = mock(Connection.class);
        boolean ok = service.addNewBook(conn, book("BKX","T","A", 1000), -1);
        assertThat(ok).isFalse();
        verify(conn, never()).prepareStatement(anyString());
    }

    @Test
    @DisplayName("BOOK-06 | Add with price=0 -> success, inserts 0 price")
    void add_price_zero_success() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement check = mock(PreparedStatement.class);
        PreparedStatement insert = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(check, insert);
        when(check.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false); // not exists
        when(insert.executeUpdate()).thenReturn(1);
        boolean ok = service.addNewBook(conn, book("BK1003","T","A", 0), 2);
        assertThat(ok).isTrue();
        verify(insert).setDouble(4, 0);
        verify(insert).setInt(5, 2);
    }

    @Test
    @DisplayName("BOOK-09 | Delete not borrowed -> success")
    void delete_not_borrowed_success() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement check = mock(PreparedStatement.class);
        PreparedStatement del = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(check, del);
        when(check.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("borrowing")).thenReturn(0);
        when(del.executeUpdate()).thenReturn(1);
        boolean ok = service.deleteBook(conn, "BK1002");
        assertThat(ok).isTrue();
        verify(del).setString(1, "BK1002");
    }

    @Test
    @DisplayName("BOOK-10 | Delete borrowed -> blocked")
    void delete_borrowed_blocked() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement check = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(check);
        when(check.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("borrowing")).thenReturn(3);
        boolean ok = service.deleteBook(conn, "BKX");
        assertThat(ok).isFalse();
        // delete statement should not be called because prepareStatement was only invoked once
        verify(conn, times(1)).prepareStatement(anyString());
    }
}
