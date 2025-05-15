package org.example.web.service;

import lombok.RequiredArgsConstructor;
import org.example.web.dto.EmployeeRequest;
import org.example.web.dto.EmployeeResponse;
import org.example.web.mappers.EmployeeMapper;
import org.example.web.model.Employee;
import org.example.web.repository.EmployeeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.example.web.mappers.EmployeeMapper.mapToEmployee;
import static org.example.web.mappers.EmployeeMapper.mapToResponse;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;


    public EmployeeResponse addEmployee(EmployeeRequest employeeRequest) {
        if (employeeRepository.existsByEmail(employeeRequest.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        Employee newEmployee = mapToEmployee(employeeRequest);
        Employee employee = employeeRepository.save(newEmployee);
        return mapToResponse(employee);
    }

    public void deleteEmployee(UUID id) {
        try {
            employeeRepository.deleteById(id);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    public EmployeeResponse getEmployeeById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Employee not found: " + id));
        return mapToResponse(employee);

    }

    public List<EmployeeResponse> listEmployees() {
        return employeeRepository.findAll().stream()
                .map(EmployeeMapper::mapToResponse)
                .toList();
    }

    public EmployeeResponse updateEmployee(UUID id, EmployeeRequest employeeRequest) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Employee not found: " + id));

        if (!employee.getEmail().equalsIgnoreCase(employeeRequest.getEmail())
                && employeeRepository.existsByEmail(employeeRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email already in use");
        }

        EmployeeMapper.updateEmployee(employee, employeeRequest);
        Employee saved = employeeRepository.save(employee);
        return mapToResponse(saved);
    }
}
