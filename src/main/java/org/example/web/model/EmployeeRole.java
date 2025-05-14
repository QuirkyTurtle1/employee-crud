package org.example.web.model;

import lombok.Getter;

@Getter
public enum EmployeeRole {
    MANAGER("Менеджер"), ADMINISTRATOR("Администротор");

    private final String description;

    EmployeeRole(String description) {
        this.description = description;
    }

}
