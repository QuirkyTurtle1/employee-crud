package org.example.web.repository;

import org.example.web.model.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderProductRepository extends JpaRepository<OrderProduct, UUID> {

    boolean existsByProduct_Id(UUID productId);
    List<OrderProduct> findAllByOrder_Id(UUID orderId);
}
