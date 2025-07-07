package org.example.web.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.web.dto.EmployeeFilter;
import org.example.web.dto.EmployeeRequest;
import org.example.web.dto.EmployeeResponse;
import org.example.web.exception.DuplicateEmailException;
import org.example.web.mappers.EmployeeMapper;
import org.example.web.model.Employee;
import org.example.web.repository.EmployeeRepository;
import org.example.web.util.EmployeeComparatorFactory;
import org.example.web.util.EmployeePredicateFactory;
import org.example.web.util.SortParser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;


@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository repository;

    private final EmployeeMapper mapper;


    public EmployeeResponse create (@Valid EmployeeRequest employeeRequest) {
        if (repository.existsByEmailIgnoreCase(employeeRequest.getEmail())) {
            throw new DuplicateEmailException("Email already in use");
        }
        Employee entity = mapper.toEntity(employeeRequest);
        entity.setPassword(employeeRequest.getPassword());
        repository.save(entity);
        return mapper.toDto(entity);
    }

//    public void deleteEmployee(UUID id) {
//        try {
//            employeeRepository.deleteById(id);
//        } catch (NoSuchElementException e) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
//        }
//    }
//
//    public EmployeeResponse getEmployeeById(UUID id) {
//        Employee employee = employeeRepository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
//                        "Employee not found: " + id));
//        return mapToResponse(employee);
//
//    }
//
//    public List<EmployeeResponse> listEmployees(EmployeeFilter filter) {
//        Predicate<Employee> predicate = EmployeePredicateFactory.build(filter);
//        Comparator<Employee> comparator = EmployeeComparatorFactory
//                .build(SortParser.parse(filter.sort()));
//        return employeeRepository.findAll().stream()
//                .filter(predicate)
//                .sorted(comparator)
//                .map(EmployeeMapper::mapToResponse)
//                .toList();
//    }
//
//    public EmployeeResponse updateEmployee(UUID id, EmployeeRequest employeeRequest) {
//        Employee employee = employeeRepository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
//                        "Employee not found: " + id));
//
//        if (!employee.getEmail().equalsIgnoreCase(employeeRequest.getEmail())
//                && employeeRepository.existsByEmail(employeeRequest.getEmail())) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT,
//                    "Email already in use");
//        }
//
//        EmployeeMapper.updateEmployee(employee, employeeRequest);
//        Employee saved = employeeRepository.save(employee);
//        return mapToResponse(saved);
//    }
}
