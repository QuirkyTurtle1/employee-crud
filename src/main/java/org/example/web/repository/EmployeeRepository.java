package org.example.web.repository;

import org.example.web.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID>,
        JpaSpecificationExecutor<Employee> {

    boolean existsByEmailIgnoreCase(String email);
}
