package org.example.web.repository;

import org.example.web.model.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderProductRepository extends JpaRepository<OrderProduct, UUID> {

    boolean existsByProduct_Id(UUID productId);
    boolean existsByOrderIdAndProductId(UUID orderId, UUID productId);
    Optional<OrderProduct> findByOrderIdAndProductId(UUID orderId, UUID productId);
    int deleteByOrderIdAndProductId(UUID orderId, UUID productId);
}
