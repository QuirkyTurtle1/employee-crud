package org.example.web.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.web.model.EmployeeRole;

import java.util.UUID;

@Getter
@Setter
public class EmployeeResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private EmployeeRole role;
}
