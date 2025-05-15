package org.example.web.repository;

import jakarta.validation.constraints.NotBlank;
import org.example.web.model.Employee;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryEmployeeRepository implements EmployeeRepository {

    private final Map<UUID, Employee> employeeMap = new ConcurrentHashMap<>();

    @Override
    public Employee save(Employee employee) {
        if (employee.getId() == null) {
            employee.setId(UUID.randomUUID());
        }
        employeeMap.put(employee.getId(), employee);
        return employee;
    }

    @Override
    public Optional<Employee> findById(UUID id) {
        return Optional.ofNullable(employeeMap.get(id));
    }

    @Override
    public List<Employee> findAll() {
        return new ArrayList<>(employeeMap.values());
    }

    @Override
    public void deleteById(UUID id) {
        Employee removed = employeeMap.remove(id);
        if (removed== null) {
            throw new NoSuchElementException("Employee not found: " + id);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        return employeeMap.values()
                .stream()
                .anyMatch(employee -> employee.getEmail().equalsIgnoreCase(email));
    }
}
