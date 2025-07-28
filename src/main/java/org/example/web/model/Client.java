package org.example.web.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue
    private UUID id;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    @OneToMany(mappedBy = "client",
            cascade = CascadeType.ALL)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

}
