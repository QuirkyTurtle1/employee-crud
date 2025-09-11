package org.example.web.util;

import jakarta.persistence.criteria.JoinType;
import org.example.web.dto.order.OrderFilter;
import org.example.web.model.Order;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class OrderSpecs {
    private OrderSpecs() {}
    public static Specification<Order> hasProduct(UUID productId) {
        return (root, q, cb) -> {
            if (productId == null) return cb.conjunction();
            q.distinct(true);
            var items = root.join("items", JoinType.INNER);
            return cb.equal(items.get("product").get("id"), productId);
        };
    }

    public static Specification<Order> build(OrderFilter f) {
        return Specification.where(SpecBuilder.<Order>eq("status", f.status()))
                .and(SpecBuilder.between("createdAt", f.from(), f.to()))
                .and(hasProduct(f.productId()));
    }
}
