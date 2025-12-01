package org.example.htmlfx.book;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BookCache {
    private static final int MAX_CACHE_SIZE = 50; // Giới hạn số lượng kết quả được cache

    private final Map<String, List<Book>> cache;

    public BookCache() {
        this.cache = new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, List<Book>> eldest) {
                return size() > MAX_CACHE_SIZE; // Xóa phần tử lâu không dùng nhất
            }
        };
    }

    public void put(String query, List<Book> books) {
        cache.put(query, books);
    }

    public List<Book> get(String query) {
        return cache.get(query);
    }

    public boolean contains(String query) {
        return cache.containsKey(query);
    }

    public void clear() {
        cache.clear();
    }
}
