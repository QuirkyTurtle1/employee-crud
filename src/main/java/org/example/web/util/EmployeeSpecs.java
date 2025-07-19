package org.example.web.util;

import org.example.web.model.Employee;
import org.example.web.model.EmployeeRole;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public class EmployeeSpecs {
    public static Specification<Employee> firstNameContains (Optional<String> term) {
        return (root, query, criteriaBuilder) ->
                term.map(t -> criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")),
                        "%" + t.toLowerCase() + "%"))
                        .orElse(null);
    }
    public static Specification<Employee> lastNameContains (Optional<String> term) {
        return (root, query, criteriaBuilder) ->
                term.map(t -> criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")),
                                "%" + t.toLowerCase() + "%"))
                        .orElse(null);
    }

    public static Specification<Employee> hasRole (Optional<EmployeeRole> role) {
        return (root, query, criteriaBuilder) ->
                role.map(r -> criteriaBuilder.equal(root.get("role"), r))
                        .orElse(null);
    }
}
