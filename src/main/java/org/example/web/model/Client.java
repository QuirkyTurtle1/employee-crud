package org.example.web.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @Email
    private String email;

    private String phone;

    @OneToMany(mappedBy = "client")
    @BatchSize(size = 50)
    @Builder.Default
    private Set<Order> orders = new HashSet<>();

}
