package org.example.web.util;

import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

public final class SpecBuilder {
    private SpecBuilder() {
    }

    public static <T> Specification<T> like(String field, String value) {
        return (root, q, cb) ->
                isBlank(value) ? cb.conjunction()
                        : cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
    }

    public static <T> Specification<T> eq(String field, Object value) {
        return (root, q, cb) ->
                value == null ? cb.conjunction()
                        : cb.equal(root.get(field), value);
    }

    public static <T, P extends Comparable<? super P>> Specification<T>
    between(String field, P from, P to) {
        return (root, q, cb) -> {
            Path<P> path = root.get(field);
            if (from != null && to != null) return cb.between(path, from, to);
            if (from != null)                return cb.greaterThanOrEqualTo(path, from);
            if (to != null)                  return cb.lessThanOrEqualTo(path, to);
            return cb.conjunction();
        };
    }



    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
