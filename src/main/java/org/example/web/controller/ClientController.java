package org.example.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.web.dto.ApiError;
import org.example.web.dto.client.ClientFilter;
import org.example.web.dto.client.ClientRequest;
import org.example.web.dto.client.ClientResponse;
import org.example.web.service.ClientService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@Tag(name = "Clients")
public class ClientController {

    private final ClientService service;

    @Operation(summary = "Create a new client",
            description = "Creates a client with the given data and returns the created client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Client created successfully"),
            @ApiResponse(responseCode = "409", description = "Conflict: client with the same email already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = """
                        {
                          "timestamp":"2025-09-10T12:34:56Z",
                          "status":409,
                          "error":"Conflict",
                          "path":"/api/clients",
                          "code":"CONFLICT",
                          "message":"Client with email 'jon@example.com' already exists",
                          "requestId":"123e4567-e89b-12d3-a456-426614174000"
                        }
                        """)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse create(@Valid @RequestBody ClientRequest req) {
        long t0 = System.currentTimeMillis();
        log.info("POST /api/clients - create: firstName={}, lastName={}", req.getFirstName(), req.getLastName());

        ClientResponse resp = service.create(req);

        log.info("POST /api/clients - success: id={}, durationMs={}", resp.getId(), System.currentTimeMillis() - t0);
        return resp;
    }

    @Operation(summary = "Get client by ID",
            description = "Returns a single client by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client found"),
            @ApiResponse(responseCode = "404", description = "Client not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ClientResponse getOne(@Parameter(description = "Client ID", required = true)
                                 @PathVariable UUID id) {
        long t0 = System.currentTimeMillis();
        log.info("GET /api/clients/{id} - getOne: id={}", id);

        ClientResponse resp = service.getOne(id);

        log.info("GET /api/clients/{id} - success: id={}, durationMs={}", resp.getId(), System.currentTimeMillis() - t0);
        return resp;
    }

    @Operation(summary = "Update client",
            description = "Updates the client with the given ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client updated"),
            @ApiResponse(responseCode = "404", description = "Client not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Conflict: client with the same email already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public ClientResponse update(@Parameter(description = "Client ID", required = true)
                                 @PathVariable UUID id,
                                 @Valid @RequestBody ClientRequest req) {
        long t0 = System.currentTimeMillis();
        log.info("PUT /api/clients/{id} - update: id={}", id);

        ClientResponse resp = service.update(id, req);

        log.info("PUT /api/clients/{id} - success: id={}, durationMs={}",
                resp.getId(), System.currentTimeMillis() - t0);
        return resp;
    }

    @Operation(summary = "Delete client",
            description = "Deletes the client with the given ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Client deleted"),
            @ApiResponse(responseCode = "404", description = "Client not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Conflict: client is referenced by other resources",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(description = "Client ID", required = true)
                       @PathVariable UUID id) {
        long t0 = System.currentTimeMillis();
        log.info("DELETE /api/clients/{id} - delete: id={}", id);

        service.delete(id);

        log.info("DELETE /api/clients/{id} - success: id={}, durationMs={}", id, System.currentTimeMillis() - t0);
    }

    @Operation(summary = "List clients",
            description = "Returns a paginated list of clients with optional filtering.")
    @ApiResponse(responseCode = "200", description = "Page of clients")
    @GetMapping
    public Page<ClientResponse> list(@ParameterObject ClientFilter filter,
                                     @ParameterObject @PageableDefault(size = 10,
                                             sort = "firstName",
                                             direction = Sort.Direction.ASC) Pageable pageable) {
        long t0 = System.currentTimeMillis();
        log.info("GET /api/clients - list: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<ClientResponse> resp = service.findAll(filter, pageable);

        log.info("GET /api/clients - success:  page={}, returned={}, total={}, durationMs={}",
                resp.getNumber(), resp.getNumberOfElements(), resp.getTotalElements(), System.currentTimeMillis() - t0);
        return resp;
    }


}
