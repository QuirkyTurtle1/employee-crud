package org.example.web.repository;

import org.example.web.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    @EntityGraph(attributePaths = {"client", "items", "items.product"})
    Optional<Order> findDetailedById(UUID id);

    boolean existsByClientId(UUID clientId);

    @EntityGraph(attributePaths = {"client", "items", "items.product"})
    List<Order> findByIdIn(Collection<UUID> ids);

}
