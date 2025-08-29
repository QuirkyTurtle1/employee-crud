package org.example.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.web.dto.client.ClientFilter;
import org.example.web.dto.client.ClientRequest;
import org.example.web.dto.client.ClientResponse;
import org.example.web.service.ClientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService service;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse create (@Valid @RequestBody ClientRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public ClientResponse getOne (@PathVariable UUID id) {
        return service.getOne(id);
    }

    @PutMapping("/{id}")
    public ClientResponse update (@PathVariable UUID id,
                                  @Valid @RequestBody ClientRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete (@PathVariable UUID id) {
        service.delete(id);
    }

    @GetMapping
    public Page<ClientResponse> list (ClientFilter filter,
                                      @PageableDefault(size = 10, sort = "firstName")Pageable pageable) {
        return service.findAll(filter, pageable);
    }


}
