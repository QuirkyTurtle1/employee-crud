package org.example.web.util;

import org.example.web.dto.EmployeeFilter;
import org.example.web.model.Employee;

import java.util.function.Predicate;

public final class EmployeePredicateFactory {
    public static Predicate<Employee> build (EmployeeFilter filter) {
        Predicate<Employee> predicate = employee -> true;
        if (filter.firstName().isPresent()) {
            predicate = predicate.and(emp -> emp.getFirstName()
                    .equalsIgnoreCase(filter.firstName().get()));
        }
        if (filter.role().isPresent()) {
            predicate = predicate.and(emp ->
                    emp.getRole() == filter.role().get());
        }
        return predicate;
    }
}
