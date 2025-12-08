package org.example.htmlfx.income;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.example.htmlfx.testutils.TestLogger;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentTest {
    @RegisterExtension
    TestLogger testLogger = new TestLogger();

    @Test
    public void testConstructorsAndAccessors() {
        Payment p = new Payment("P1", "M1", "BK1", "1000", "2025-11-01");

        assertThat(p.getId()).isEqualTo("P1");
        assertThat(p.getMember_id()).isEqualTo("M1");
        assertThat(p.getBook_id()).isEqualTo("BK1");
        assertThat(p.getPayment()).isEqualTo("1000");
        assertThat(p.getOrder_date()).isEqualTo("2025-11-01");

        // Verify setters properly update values
        p.setId("P2");
        p.setMember_id("M2");
        p.setBook_id("BK2");
        p.setPrice("2500");
        p.setOrder_date("2025-12-01");

        assertThat(p.getId()).isEqualTo("P2");
        assertThat(p.getMember_id()).isEqualTo("M2");
        assertThat(p.getBook_id()).isEqualTo("BK2");
        assertThat(p.getPayment()).isEqualTo("2500");
        assertThat(p.getOrder_date()).isEqualTo("2025-12-01");
    }
}
