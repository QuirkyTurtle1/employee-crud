package org.example.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.web.dto.EmployeeFilter;
import org.example.web.dto.EmployeeRequest;
import org.example.web.dto.EmployeeResponse;
import org.example.web.model.Employee;
import org.example.web.model.EmployeeRole;
import org.example.web.repository.EmployeeRepository;
import org.example.web.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/employees")
    public ResponseEntity<EmployeeResponse> create (@Valid @RequestBody EmployeeRequest employeeRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(employeeRequest));
    }

    @GetMapping("/employees/{id}")
    public EmployeeResponse getOne (@PathVariable UUID id) {
        return employeeService.findById(id);
    }

    @GetMapping("/employees")
    public Page<EmployeeResponse> listEmployees (EmployeeFilter filter,
                                                 @PageableDefault(size=20) Pageable pageable
    ) {
        return employeeService.findAll(filter, pageable);
    }

    @PutMapping("/employees/{id}")
    public EmployeeResponse updateEmployee (@PathVariable UUID id,
                                            @Valid @RequestBody EmployeeRequest employeeRequest) {
        return employeeService.updateEmployee(id, employeeRequest);
    }

    @DeleteMapping("/employees/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete (@PathVariable UUID id) {
        employeeService.delete(id);
    }
}
