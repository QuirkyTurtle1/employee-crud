package org.example.web.fixtures;

import org.example.web.model.Client;
import org.example.web.model.Product;
import org.example.web.model.Product.ProductBuilder;

import java.math.BigDecimal;

public class ProductFixture {
    public static ProductBuilder defaultProduct() {
        return Product.builder()
                .name("Book")
                .description("Some book")
                .price(BigDecimal.valueOf(19.99));
    }

    public static Product readyProduct() {
        return defaultProduct().build();
    }
}
