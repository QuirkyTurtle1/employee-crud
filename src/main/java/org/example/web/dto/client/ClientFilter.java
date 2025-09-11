package org.example.web.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.web.model.EmployeeRole;

import java.util.Optional;

public record ClientFilter(
        @Schema(description = "Filter by first name ", example = "John")
        String firstName,

        @Schema(description = "Filter by last name ", example = "Doe")
        String lastName,

        @Schema(description = "Filter by email", example = "john.doe@example.com")
        String email,

        @Schema(description = "Filter by phone (digits only or with +)", example = "+791858358552")
        String phone
) {
}
