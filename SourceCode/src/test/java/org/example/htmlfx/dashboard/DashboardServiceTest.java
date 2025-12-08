package org.example.htmlfx.dashboard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class DashboardServiceTest {

    private final DashboardService svc = new DashboardService();

    private static class Db {
        final Connection conn = mock(Connection.class);
        final PreparedStatement ps = mock(PreparedStatement.class);
        final ResultSet rs = mock(ResultSet.class);
    }

    private Db mockScalar(int value) throws Exception {
        Db d = new Db();
        when(d.conn.prepareStatement(anyString())).thenReturn(d.ps);
        when(d.ps.executeQuery()).thenReturn(d.rs);
        when(d.rs.next()).thenReturn(true);
        when(d.rs.getInt(1)).thenReturn(value);
        return d;
    }

    @Test
    @DisplayName("DASH-01 | Totals reflect DB counts")
    void totals_reflect_db_counts() throws Exception {
        // books
        var b = mockScalar(100);
        assertThat(svc.totalBooks(b.conn)).isEqualTo(100);
        // members
        var m = mockScalar(20);
        assertThat(svc.totalMembers(m.conn)).isEqualTo(20);
        // borrows today
        var br = mockScalar(5);
        assertThat(svc.totalBorrowsToday(br.conn)).isEqualTo(5);
    }

    @Test
    @DisplayName("DASH-02 | Totals update after CRUD (simulated)")
    void totals_update_after_crud() throws Exception {
        // simulate before and after add book
        var before = mockScalar(100);
        assertThat(svc.totalBooks(before.conn)).isEqualTo(100);
        var after = mockScalar(101);
        assertThat(svc.totalBooks(after.conn)).isEqualTo(101);
    }

    @Test
    @DisplayName("DASH-01 | Total payment sum")
    void total_payment_sum() throws Exception {
        Db d = new Db();
        when(d.conn.prepareStatement(anyString())).thenReturn(d.ps);
        when(d.ps.executeQuery()).thenReturn(d.rs);
        when(d.rs.next()).thenReturn(true);
        when(d.rs.getDouble(1)).thenReturn(12345.0);
        assertThat(svc.totalPayment(d.conn)).isEqualTo(12345.0);
    }
}
