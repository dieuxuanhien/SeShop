package com.seshop.shared.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class PageUtils {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private PageUtils() {
    }

    public static Pageable page(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);
        return PageRequest.of(safePage, safeSize == 0 ? DEFAULT_SIZE : safeSize);
    }
}
