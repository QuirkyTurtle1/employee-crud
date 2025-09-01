package org.example.web.repository;

import org.example.web.model.Client;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID>, JpaSpecificationExecutor<Client> {
    boolean existsByEmailIgnoreCase(String email);

    interface ClientOrderCount {
        UUID getClientId();
        long getCnt();
    }

    @Query("""
      select o.client.id as clientId, count(o.id) as cnt
      from Order o
      where o.client.id in :ids
      group by o.client.id
    """)
    List<ClientOrderCount> countOrdersByClientIds(Collection<UUID> ids);

    @Query("select count(o.id) from Order o where o.client.id = :clientId")
    long countOrdersByClientId(@Param("clientId") UUID clientId);

    @EntityGraph(attributePaths = {"orders", "orders.items", "orders.items.product"})
    Optional<Client> findDetailedById(UUID id);
}
