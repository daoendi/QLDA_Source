package org.example.htmlfx.income;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PaymentServiceTest {

    private final PaymentService service = new PaymentService();

    private static class DbSeq {
        final Connection conn = mock(Connection.class);
        final PreparedStatement psMember = mock(PreparedStatement.class);
        final PreparedStatement psBook = mock(PreparedStatement.class);
        final PreparedStatement psInsert = mock(PreparedStatement.class);
        final ResultSet rsMember = mock(ResultSet.class);
        final ResultSet rsBook = mock(ResultSet.class);
    }

    private DbSeq mockExistence(boolean memberExists, boolean bookExists) throws Exception {
        DbSeq d = new DbSeq();
        when(d.conn.prepareStatement(anyString())).thenReturn(d.psMember, d.psBook, d.psInsert);
        when(d.psMember.executeQuery()).thenReturn(d.rsMember);
        when(d.psBook.executeQuery()).thenReturn(d.rsBook);
        when(d.rsMember.next()).thenReturn(true);
        when(d.rsMember.getInt(1)).thenReturn(memberExists ? 1 : 0);
        when(d.rsBook.next()).thenReturn(true);
        when(d.rsBook.getInt(1)).thenReturn(bookExists ? 1 : 0);
        when(d.psInsert.executeUpdate()).thenReturn(1);
        return d;
    }

    @Test
    @DisplayName("PAY-01 | Create valid payment -> success")
    void pay01_create_valid() throws Exception {
        var d = mockExistence(true, true);
        boolean ok = service.createPayment(d.conn, "MB2001", "BK1001", "2", "2025-12-01 00:00:00");
        assertThat(ok).isTrue();
        verify(d.psInsert, times(1)).executeUpdate();
    }

    @Test
    @DisplayName("PAY-02 | Negative/alpha payment -> fail")
    void pay02_invalid_payment() throws Exception {
        var d = mockExistence(true, true);
        assertThat(service.createPayment(d.conn, "MB2001", "BK1001", "-100", null)).isFalse();
        assertThat(service.createPayment(d.conn, "MB2001", "BK1001", "abc", null)).isFalse();
        verify(d.conn, atLeastOnce()).prepareStatement(anyString());
    }

    @Test
    @DisplayName("PAY-03 | Member/book not exist -> fail")
    void pay03_member_or_book_missing() throws Exception {
        var d1 = mockExistence(false, true);
        assertThat(service.createPayment(d1.conn, "MB9999", "BK1001", "1", null)).isFalse();
        var d2 = mockExistence(true, false);
        assertThat(service.createPayment(d2.conn, "MB2001", "BK9999", "1", null)).isFalse();
    }
}
