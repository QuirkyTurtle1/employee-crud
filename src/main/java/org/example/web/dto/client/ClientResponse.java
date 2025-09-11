package org.example.web.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

    @Schema(description = "Client UUID", example = "123e4567-e89b-12d3-a456-426614174000", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Client first name", example = "John")
    private String firstName;

    @Schema(description = "Client last name", example = "Doe")
    private String lastName;

    @Schema(description = "Client email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Client phone number (digits only or with +)", example = "+791858358552")
    private String phone;

    @Schema(description = "Client orders count", example = "3", accessMode = Schema.AccessMode.READ_ONLY)
    private Long ordersCount;
}
