package org.example.htmlfx.borrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.example.htmlfx.testutils.TestLogger;

import static org.assertj.core.api.Assertions.assertThat;

public class BorrowTest {
    @RegisterExtension
    TestLogger testLogger = new TestLogger();

    @Test
    public void testConstructorsAndAccessors() {
        Borrow b1 = new Borrow("B1", "Member A", "BookX", "Name A", "2025-11-01", "2025-11-10", "borrowed");

        assertThat(b1.getId()).isEqualTo("B1");
        assertThat(b1.getMember_id()).isEqualTo("Member A");
        assertThat(b1.getBook_id()).isEqualTo("BookX");
        assertThat(b1.getName()).isEqualTo("Name A");
        assertThat(b1.getBorrowDate()).isEqualTo("2025-11-01");
        assertThat(b1.getReturnDate()).isEqualTo("2025-11-10");
        assertThat(b1.getStatus()).isEqualTo("borrowed");

        // Verify setters update the corresponding fields
        b1.setId("B2");
        b1.setMember_id("M2");
        b1.setBook_id("BK2");
        b1.setName("Name B");
        b1.setBorrowDate("2025-12-01");
        b1.setReturnTime("2025-12-15");
        b1.setStatus("returned");

        assertThat(b1.getId()).isEqualTo("B2");
        assertThat(b1.getMember_id()).isEqualTo("M2");
        assertThat(b1.getBook_id()).isEqualTo("BK2");
        assertThat(b1.getName()).isEqualTo("Name B");
        assertThat(b1.getBorrowDate()).isEqualTo("2025-12-01");
        assertThat(b1.getReturnDate()).isEqualTo("2025-12-15");
        assertThat(b1.getStatus()).isEqualTo("returned");
    }
}
