package org.example.web.dto;

import org.example.web.model.EmployeeRole;

import java.util.Optional;

public record EmployeeFilter(
        Optional<String> firstName,
        Optional<EmployeeRole> role,
        Optional<String> sort
) {
}
