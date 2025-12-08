package org.example.htmlfx.book;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.example.htmlfx.testutils.TestLogger;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class BookCacheTest {

    @RegisterExtension
    TestLogger testLogger = new TestLogger();

    @Test
    public void testPutGetContainsClearAndEviction() {
        BookCache cache = new BookCache();

        // populate cache with a few entries
        for (int i = 0; i < 5; i++) {
            cache.put("q" + i, Collections.singletonList(new Book("Title" + i, "Author")));
        }

        // verify recently added keys are present
        assertThat(cache.contains("q0")).isTrue();
        assertThat(cache.get("q1")).isNotNull();
        assertThat(cache.get("q1")).hasSize(1);
        assertThat(cache.get("q1").get(0).getTitle()).isEqualTo("Title1");

        // verify non-existing key returns null / not contained
        assertThat(cache.contains("not-exist")).isFalse();
        assertThat(cache.get("not-exist")).isNull();

        // clear the cache and verify entries removed
        cache.clear();
        assertThat(cache.contains("q0")).isFalse();
        // after clear, get should return null
        assertThat(cache.get("q0")).isNull();

        // Fill beyond MAX_CACHE_SIZE to trigger eviction of eldest entry
        for (int i = 0; i <= 50; i++) { // insert 51 entries
            cache.put("k" + i, Collections.singletonList(new Book("T" + i, "A")));
        }
        // the earliest key (k0) should have been evicted; latest should remain
        assertThat(cache.contains("k0")).isFalse();
        assertThat(cache.get("k0")).isNull();

        assertThat(cache.contains("k50")).isTrue();
        assertThat(cache.get("k50")).isNotNull();
        assertThat(cache.get("k50")).hasSize(1);
        assertThat(cache.get("k50").get(0).getTitle()).isEqualTo("T50");
    }
}
