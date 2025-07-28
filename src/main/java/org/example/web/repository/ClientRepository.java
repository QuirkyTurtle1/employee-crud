package org.example.web.repository;

import org.example.web.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    boolean existsByEmail(String email);
}
