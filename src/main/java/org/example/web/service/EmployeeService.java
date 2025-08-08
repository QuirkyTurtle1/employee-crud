package org.example.web.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.web.dto.employee.EmployeeFilter;
import org.example.web.dto.employee.EmployeeRequest;
import org.example.web.dto.employee.EmployeeResponse;
import org.example.web.exception.DuplicateEmailException;
import org.example.web.exception.NotFoundException;
import org.example.web.mappers.EmployeeMapper;
import org.example.web.model.Employee;
import org.example.web.repository.EmployeeRepository;
import org.example.web.util.EmployeeSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;


    public EmployeeResponse create ( EmployeeRequest employeeRequest) {
        if (repository.existsByEmailIgnoreCase(employeeRequest.getEmail())) {
            throw new DuplicateEmailException(employeeRequest.getEmail());
        }
        Employee entity = mapper.toEntity(employeeRequest);
        entity.setPassword(employeeRequest.getPassword());
        Employee saved = repository.save(entity);
        return mapper.toDto(saved);
    }


    public EmployeeResponse findById(UUID id) {
        Employee employee = repository.findById(id)
                .orElseThrow(()-> new NotFoundException("Employee",id));

        return mapper.toDto(employee);

    }



    public Page<EmployeeResponse> findAll(EmployeeFilter f, Pageable pageable) {
        Specification<Employee> spec = Specification.where(EmployeeSpecs.firstNameContains(f.firstName())).
                and(EmployeeSpecs.lastNameContains(f.lastName()))
                .and(EmployeeSpecs.hasRole(f.role()));

        return repository.findAll(spec, pageable).map(mapper::toDto);
    }

    public EmployeeResponse updateEmployee(UUID id, @Valid EmployeeRequest employeeRequest) {
        Employee employee = repository.findById(id).orElseThrow(() -> new NotFoundException("Employee",id));

        if (!employee.getEmail().equalsIgnoreCase(employeeRequest.getEmail())
                && repository.existsByEmailIgnoreCase(employeeRequest.getEmail())) {
            throw new DuplicateEmailException(employeeRequest.getEmail());
        }
        mapper.updateEntityFromDto(employeeRequest, employee);

        if (employeeRequest.getPassword() != null && !employeeRequest.getPassword().isBlank()) {
            employee.setPassword(employeeRequest.getPassword());
        }

        return mapper.toDto(employee);
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Employee",id);
        }
        repository.deleteById(id);
    }
}
