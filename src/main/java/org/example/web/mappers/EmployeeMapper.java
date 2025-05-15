package org.example.web.mappers;

import org.example.web.dto.EmployeeRequest;
import org.example.web.dto.EmployeeResponse;
import org.example.web.model.Employee;

public class EmployeeMapper {
    public static Employee mapToEmployee (EmployeeRequest employeeRequest) {
        Employee newEmployee = new Employee();
        newEmployee.setEmail(employeeRequest.getEmail());
        newEmployee.setFirstName(employeeRequest.getFirstName());
        newEmployee.setLastName(employeeRequest.getLastName());
        newEmployee.setPassword(employeeRequest.getPassword());
        newEmployee.setRole(employeeRequest.getRole());
        return newEmployee;
    }

    public static EmployeeResponse mapToResponse (Employee employee) {
        EmployeeResponse employeeResponse = new EmployeeResponse();
        employeeResponse.setEmail(employee.getEmail());
        employeeResponse.setId(employee.getId());
        employeeResponse.setFirstName(employee.getFirstName());
        employeeResponse.setLastName(employee.getLastName());
        employeeResponse.setRole(employee.getRole());
        return employeeResponse;
    }

}
