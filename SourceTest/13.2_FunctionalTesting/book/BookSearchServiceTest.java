package org.example.htmlfx.book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class BookSearchServiceTest {

    private Book mk(String id, String title) {
        Book b = new Book();
        b.setbookmark(id);
        b.setTitle(title);
        b.setAuthors("Author");
        return b;
    }

    @Test
    @DisplayName("SEARCH-01 | Search by title contains 'Clean'")
    void search_by_title() {
        BookCache cache = new BookCache();
        BookSearchService.BookProvider provider = (kw, max) -> {
            List<Book> list = new ArrayList<>();
            if (kw.contains("Clean")) {
                list.add(mk("BK1","Clean Code"));
                list.add(mk("BK2","The Clean Coder"));
            }
            return list;
        };
        BookSearchService svc = new BookSearchService(cache, provider);
        List<Book> res = svc.searchWithCache("Clean", 10);
        assertThat(res).extracting(Book::getTitle).contains("Clean Code");
    }

    @Test
    @DisplayName("SEARCH-02 | No results -> empty list")
    void search_no_results() {
        BookCache cache = new BookCache();
        BookSearchService.BookProvider provider = (kw, max) -> new ArrayList<>();
        BookSearchService svc = new BookSearchService(cache, provider);
        List<Book> res = svc.searchWithCache("no_such_book_zzz", 10);
        assertThat(res).isEmpty();
    }

    @Test
    @DisplayName("SEARCH-03 | Second query uses cache")
    void search_uses_cache_second_time() {
        AtomicInteger calls = new AtomicInteger(0);
        BookCache cache = new BookCache();
        BookSearchService.BookProvider provider = (kw, max) -> {
            calls.incrementAndGet();
            List<Book> list = new ArrayList<>();
            list.add(mk("BK1","Java in Action"));
            return list;
        };
        BookSearchService svc = new BookSearchService(cache, provider);
        List<Book> first = svc.searchWithCache("java", 10);
        List<Book> second = svc.searchWithCache("java", 10);
        assertThat(calls.get()).isEqualTo(1);
        assertThat(second).hasSize(1);
    }
}
