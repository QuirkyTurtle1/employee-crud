package org.example.web.repository;

import jakarta.validation.constraints.NotBlank;
import org.example.web.model.Employee;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryEmployeeRepository implements EmployeeRepository {

    private final Map<UUID, Employee> employeeMap = new ConcurrentHashMap<>();

    @Override
    public Employee save(Employee employee) {
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
    public boolean deleteById(UUID id) {
        return employeeMap.remove(id) != null;
    }

    @Override
    public boolean existsByEmail(String email) {
        return employeeMap.values()
                .stream()
                .anyMatch(employee -> employee.getEmail().equalsIgnoreCase(email));
    }
}
