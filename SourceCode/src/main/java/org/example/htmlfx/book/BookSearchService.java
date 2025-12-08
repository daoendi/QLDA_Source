package org.example.htmlfx.book;

import java.util.ArrayList;
import java.util.List;

public class BookSearchService {

    public interface BookProvider {
        List<Book> search(String keyword, int maxResults);
    }

    private final BookCache cache;
    private final BookProvider provider;

    public BookSearchService(BookCache cache, BookProvider provider) {
        this.cache = cache;
        this.provider = provider;
    }

    public List<Book> searchWithCache(String keyword, int maxResults) {
        if (keyword == null || keyword.isBlank()) return new ArrayList<>();
        String key = keyword.trim();
        if (cache.contains(key)) {
            return cache.get(key);
        }
        List<Book> res = provider.search(key, maxResults);
        cache.put(key, res);
        return res;
    }
}
