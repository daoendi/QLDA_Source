package org.example.htmlfx.book;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.example.htmlfx.testutils.TestLogger;

import static org.assertj.core.api.Assertions.assertThat;

public class BookTest {

    @RegisterExtension
    TestLogger testLogger = new TestLogger();

    /**
     * Verify getters/setters and equality semantics for Book.
     * Two Book instances with the same ID should be equal.
     */
    @Test
    public void testGettersSettersAndEquals() {
        Book b1 = new Book();
        b1.setID("ID-123");
        b1.setTitle("Test Title");
        b1.setAuthors("Author A");
        b1.setAmount(5);
        b1.setPrice(9.99);

        assertThat(b1.getID()).isEqualTo("ID-123");
        assertThat(b1.getTitle()).isEqualTo("Test Title");
        assertThat(b1.getAuthors()).isEqualTo("Author A");
        assertThat(b1.getAmount()).isEqualTo(5);
        assertThat(b1.getPrice()).isEqualTo(9.99);

        Book b2 = new Book();
        b2.setID("ID-123");

        // Two books with the same ID must be equal
        assertThat(b1).isEqualTo(b2);
        assertThat(b1.hashCode()).isEqualTo(b2.hashCode());

        Book b3 = new Book();
        b3.setID("DIFFERENT");
        assertThat(b1).isNotEqualTo(b3);
    }
}
