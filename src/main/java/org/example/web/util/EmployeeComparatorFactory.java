package org.example.web.util;

import org.example.web.dto.SortOrder;
import org.example.web.model.Employee;


import java.util.Comparator;
import java.util.Optional;

import static java.util.Comparator.*;

public final class EmployeeComparatorFactory {

    public static Comparator<Employee> build(Optional<SortOrder> order) {

        Comparator<Employee> base = comparing(Employee::getId);

        if (order.isEmpty()) {
            return base;
        }

        SortOrder sortOrder = order.get();

        Comparator<Employee> comparator = switch (sortOrder.field()) {
            case "lastName"  -> comparing(Employee::getLastName , String.CASE_INSENSITIVE_ORDER);
            case "firstName" -> comparing(Employee::getFirstName, String.CASE_INSENSITIVE_ORDER);
            case "email"     -> comparing(Employee::getEmail    , String.CASE_INSENSITIVE_ORDER);
            default          -> base;
        };

        return sortOrder.asc() ? comparator : comparator.reversed();

    }
}
