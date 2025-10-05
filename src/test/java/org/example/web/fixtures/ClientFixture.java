package org.example.web.fixtures;

import org.example.web.model.Client;
import org.example.web.model.Client.ClientBuilder;

public class ClientFixture {
    public static ClientBuilder defaultClient() {
        return Client.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+79001234567");
    }

    public static Client readyClient() {
        return defaultClient().build();
    }
}
