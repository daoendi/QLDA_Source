package org.example.htmlfx.user;

import org.example.htmlfx.toolkits.Checked;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class MemberServiceTest {

    private final MemberService service = new MemberService();

    @Test
    @DisplayName("MEM-01 | Add valid member -> success")
    void mem01_add_valid() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        boolean ok = service.addMember(conn,
                "Nguyen", "Van A", "Male", "2002-10-01", "0123456789", "a@gmail.com");
        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("MEM-02 | Email sai domain -> fail")
    void mem02_email_wrong_domain() {
        boolean ok = service.addMember(mock(Connection.class),
                "Nguyen", "Van A", "Male", "2002-10-01", "0123456789", "a@yahoo.com");
        assertThat(ok).isFalse();
    }

    @Test
    @DisplayName("MEM-03 | Phone sai định dạng -> fail")
    void mem03_phone_invalid() {
        boolean ok = service.addMember(mock(Connection.class),
                "Nguyen", "Van A", "Male", "2002-10-01", "1234567890", "a@gmail.com");
        assertThat(ok).isFalse();
    }

    @Test
    @DisplayName("MEM-04 | Email uppercase -> consistent with Checked (fail)")
    void mem04_email_uppercase() {
        boolean ok = service.addMember(mock(Connection.class),
                "Nguyen", "Van A", "Male", "2002-10-01", "0123456789", "A@GMAIL.COM");
        assertThat(ok).isFalse();
    }

    @Test
    @DisplayName("MEM-05 | Phone boundary lengths: 9/10/11")
    void mem05_phone_boundary() {
        // 9 digits -> false
        assertThat(Checked.isValidPhone("012345678")).isFalse();
        // 10 digits -> true
        assertThat(Checked.isValidPhone("0123456789")).isTrue();
        // 11 digits -> false
        assertThat(Checked.isValidPhone("01234567890")).isFalse();
    }

    @Test
    @DisplayName("MEM-06 | Search by ID/name")
    void mem06_search_members() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        var rs = mock(java.sql.ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("member_id")).thenReturn("MB2001");
        when(rs.getString("firstname")).thenReturn("Nguyen");
        when(rs.getString("lastname")).thenReturn("Van A");
        when(rs.getString("gender")).thenReturn("Male");
        when(rs.getString("birth")).thenReturn("2002-10-01");
        when(rs.getString("email")).thenReturn("a@gmail.com");
        when(rs.getString("phone")).thenReturn("0123456789");
        when(rs.getString("image")).thenReturn("/img/avt/member/image1.jpg");

        var list = service.searchMembers(conn, "MB2001");
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo("MB2001");
        assertThat(list.get(0).getFirstname()).isEqualTo("Nguyen");
    }
}
