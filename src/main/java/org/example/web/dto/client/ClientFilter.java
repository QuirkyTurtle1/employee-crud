package org.example.web.dto.client;

import org.example.web.model.EmployeeRole;

import java.util.Optional;

public record ClientFilter(
        Optional<String> firstName,
        Optional<String> lastName,
        Optional<String> email,
        Optional<String> phone
) {
}
