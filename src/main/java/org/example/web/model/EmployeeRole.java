package org.example.web.model;

import lombok.Getter;

@Getter
public enum EmployeeRole {
    MANAGER("Менеджер"), ADMINISTRATOR("Администратор");

    private final String description;

    EmployeeRole(String description) {
        this.description = description;
    }

}
