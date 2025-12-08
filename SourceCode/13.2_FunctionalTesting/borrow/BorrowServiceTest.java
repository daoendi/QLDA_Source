package org.example.htmlfx.borrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BorrowServiceTest {

    private final BorrowService service = new BorrowService();

    private static class DbSeq {
        final Connection conn = mock(Connection.class);
        final PreparedStatement ps1 = mock(PreparedStatement.class); // member exists
        final PreparedStatement ps2 = mock(PreparedStatement.class); // remaining
        final PreparedStatement ps3 = mock(PreparedStatement.class); // insert
        final ResultSet rs1 = mock(ResultSet.class);
        final ResultSet rs2 = mock(ResultSet.class);
    }

    private DbSeq mockHappyPath(String memberId, String bookId, int remaining) throws Exception {
        DbSeq d = new DbSeq();
        when(d.conn.prepareStatement(anyString())).thenReturn(d.ps1, d.ps2, d.ps3);
        when(d.ps1.executeQuery()).thenReturn(d.rs1);
        when(d.ps2.executeQuery()).thenReturn(d.rs2);
        when(d.rs1.next()).thenReturn(true);
        when(d.rs1.getInt(1)).thenReturn(1); // member exists
        when(d.rs2.next()).thenReturn(true);
        when(d.rs2.getInt(1)).thenReturn(remaining);
        when(d.ps3.executeUpdate()).thenReturn(1);
        return d;
    }

    @Test
    @DisplayName("BOR-01 | Create valid borrow -> success and insert called")
    void bor01_create_valid() throws Exception {
        var d = mockHappyPath("MB2001","BK1001", 2);
        boolean ok = service.createBorrow(d.conn, "MB2001", "BK1001", "2025-12-01", "2025-12-10");
        assertThat(ok).isTrue();
        verify(d.ps3, times(1)).executeUpdate();
    }

    @Test
    @DisplayName("BOR-02 | Out of stock -> fail, no insert")
    void bor02_out_of_stock() throws Exception {
        var d = mockHappyPath("MB2001","BK1001", 0);
        boolean ok = service.createBorrow(d.conn, "MB2001", "BK1001", "2025-12-01", "2025-12-10");
        assertThat(ok).isFalse();
        verify(d.ps3, never()).executeUpdate();
    }

    @Test
    @DisplayName("BOR-03 | Member not exist -> fail")
    void bor03_member_not_exist() throws Exception {
        DbSeq d = new DbSeq();
        when(d.conn.prepareStatement(anyString())).thenReturn(d.ps1);
        when(d.ps1.executeQuery()).thenReturn(d.rs1);
        when(d.rs1.next()).thenReturn(true);
        when(d.rs1.getInt(1)).thenReturn(0); // member not exists
        boolean ok = service.createBorrow(d.conn, "MB9999", "BK1001", "2025-12-01", "2025-12-10");
        assertThat(ok).isFalse();
    }

    @Test
    @DisplayName("BOR-04 | Book not exist -> fail")
    void bor04_book_not_exist() throws Exception {
        DbSeq d = new DbSeq();
        when(d.conn.prepareStatement(anyString())).thenReturn(d.ps1, d.ps2);
        when(d.ps1.executeQuery()).thenReturn(d.rs1);
        when(d.ps2.executeQuery()).thenReturn(d.rs2);
        when(d.rs1.next()).thenReturn(true);
        when(d.rs1.getInt(1)).thenReturn(1);
        when(d.rs2.next()).thenReturn(false); // no book row
        boolean ok = service.createBorrow(d.conn, "MB2001", "BK9999", "2025-12-01", "2025-12-10");
        assertThat(ok).isFalse();
    }

    @Test
    @DisplayName("BOR-05 | Member borrows multiple books -> two inserts")
    void bor05_multiple_borrows() throws Exception {
        var d1 = mockHappyPath("MB2001","BK1001", 3);
        var d2 = mockHappyPath("MB2001","BK1004", 2);
        assertThat(service.createBorrow(d1.conn, "MB2001","BK1001","2025-12-01","2025-12-05")).isTrue();
        assertThat(service.createBorrow(d2.conn, "MB2001","BK1004","2025-12-01","2025-12-05")).isTrue();
        verify(d1.ps3, times(1)).executeUpdate();
        verify(d2.ps3, times(1)).executeUpdate();
    }

    @Test
    @DisplayName("BOR-06 | return < borrow -> fail")
    void bor06_return_before_borrow() throws Exception {
        DbSeq d = mockHappyPath("MB2001","BK1001", 2);
        boolean ok = service.createBorrow(d.conn, "MB2001", "BK1001", "2025-12-10", "2025-12-01");
        assertThat(ok).isFalse();
        verify(d.ps3, never()).executeUpdate();
    }
}
