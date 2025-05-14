package org.example.web.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Employee {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private EmployeeRole role;
}
