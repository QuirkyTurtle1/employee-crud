package org.example.web.repository;

import org.example.web.model.Employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository {
    Employee save(Employee employee);
    Optional<Employee> findById (UUID id);
    List<Employee> findAll();
    void deleteById(UUID id);
    boolean existsByEmail(String email);
}
