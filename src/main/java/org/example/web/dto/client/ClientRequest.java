package org.example.web.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequest {
    @Schema(description = "Client first name", example = "John")
    @NotBlank
    private String firstName;

    @Schema(description = "Client last name", example = "Doe")
    @NotBlank
    private String lastName;

    @Schema(description = "Client email address", example = "john.doe@example.com")
    @Email
    @NotBlank
    private String email;

    @Schema(description = "Client phone number (digits only or with +)", example = "+791858358552")
    @NotBlank
    private String phone;

}
