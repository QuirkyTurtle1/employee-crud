package org.example.web.service;

import lombok.RequiredArgsConstructor;
import org.example.web.dto.EmployeeRequest;
import org.example.web.dto.EmployeeResponse;
import org.example.web.model.Employee;
import org.example.web.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import static org.example.web.mappers.EmployeeMapper.mapToEmployee;
import static org.example.web.mappers.EmployeeMapper.mapToResponse;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;


    public EmployeeResponse addEmployee(EmployeeRequest employeeRequest) {
        Employee newEmployee = mapToEmployee(employeeRequest);
        Employee employee = employeeRepository.save(newEmployee);
        return mapToResponse(employee);
    }
}
