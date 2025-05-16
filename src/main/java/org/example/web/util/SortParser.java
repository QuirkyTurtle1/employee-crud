package org.example.web.util;

import org.example.web.dto.SortOrder;

import java.util.Optional;

public final class SortParser {
    public static Optional<SortOrder> parse (Optional<String> string) {
        return string.map(s -> {
            String[] str = s.split(",");
            return new SortOrder(str[0],
                    str.length < 2 || !"desc".equalsIgnoreCase(str[1]));

        });
    }
}
