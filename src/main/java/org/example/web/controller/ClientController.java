package org.example.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService service;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse create (@Valid @RequestBody ClientRequest req) {
        long t0 = System.currentTimeMillis();
        log.info("POST /api/clients - create: firstName={}, lastName={}", req.getFirstName(), req.getLastName());

        ClientResponse resp = service.create(req);

        log.info("POST /api/clients - success: id={}, durationMs={}", resp.getId(), System.currentTimeMillis() - t0);
        return resp;
    }

    @GetMapping("/{id}")
    public ClientResponse getOne (@PathVariable UUID id) {
        long t0 = System.currentTimeMillis();
        log.info("GET /api/clients/{id} - getOne: id={}", id);

        ClientResponse resp = service.getOne(id);

        log.info("GET /api/clients/{id} - success: id={}, durationMs={}", resp.getId(), System.currentTimeMillis() - t0);
        return resp;
    }

    @PutMapping("/{id}")
    public ClientResponse update (@PathVariable UUID id,
                                  @Valid @RequestBody ClientRequest req) {
        long t0 = System.currentTimeMillis();
        log.info("PUT /api/clients/{id} - update: id={}", id);

        ClientResponse resp = service.update(id, req);

        log.info("PUT /api/clients/{id} - success: id={}, durationMs={}",
                resp.getId(), System.currentTimeMillis() - t0);
        return resp;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete (@PathVariable UUID id) {
        service.delete(id);
    }

    @GetMapping
    public Page<ClientResponse> list (ClientFilter filter,
                                      @PageableDefault(size = 10, sort = "firstName")Pageable pageable) {
        long t0 = System.currentTimeMillis();
        log.info("GET /api/clients - list: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<ClientResponse> resp = service.findAll(filter, pageable);

        log.info("GET /api/clients - success:  page={}, returned={}, total={}, durationMs={}",
                resp.getNumber(), resp.getNumberOfElements(), resp.getTotalElements(), System.currentTimeMillis() - t0);
        return resp;
    }


}
