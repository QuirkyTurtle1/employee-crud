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
import org.example.web.dto.client.ClientResponse;
import org.example.web.dto.product.ProductFilter;
import org.example.web.dto.product.ProductRequest;
import org.example.web.dto.product.ProductResponse;
import org.example.web.service.ProductService;
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
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Tag(name = "Products")
public class ProductController {

    private final ProductService service;

    @Operation(summary = "Create a new product",
            description = "Creates a product with the given data and returns the created product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "409", description = "Conflict: product with the same name already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp":"2025-09-10T12:34:56Z",
                                      "status":409,
                                      "error":"Conflict",
                                      "path":"/api/products",
                                      "code":"CONFLICT",
                                      "message":"Product with name 'Book' already exists",
                                      "requestId":"123e4567-e89b-12d3-a456-426614174000"
                                    }
                                    """)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest req) {
        long t0 = System.currentTimeMillis();
        log.info("POST /api/products - create: name={}", req.getName());

        ProductResponse resp = service.create(req);

        log.info("POST /api/products - success: id={}, durationMs={}", resp.getId(), System.currentTimeMillis() - t0);
        return resp;
    }

    @Operation(summary = "Get product by ID",
            description = "Returns a single product by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ProductResponse getOne(@Parameter(description = "Product ID", required = true)
                                  @PathVariable UUID id) {
        long t0 = System.currentTimeMillis();
        log.info("GET /api/products/{id} - getOne: id={}", id);

        ProductResponse resp = service.getOne(id);

        log.info("GET /api/products/{id} - success: id={}, durationMs={}", resp.getId(), System.currentTimeMillis() - t0);
        return resp;
    }

    @Operation(summary = "Update product",
            description = "Updates the product with the given ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Conflict: product with the same name already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public ProductResponse update(@Parameter(description = "Product ID", required = true)
                                      @PathVariable UUID id,
                                  @Valid @RequestBody ProductRequest req) {
        long t0 = System.currentTimeMillis();
        log.info("PUT /api/products/{id} - update: id={}", id);

        ProductResponse resp = service.update(id, req);

        log.info("PUT /api/products/{id} - success: id={}, durationMs={}",
                resp.getId(), System.currentTimeMillis() - t0);
        return resp;
    }

    @Operation(summary = "Delete product",
            description = "Deletes the product with the given ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Conflict: product is used in an order",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(description = "Product ID", required = true)
                           @PathVariable UUID id) {
        long t0 = System.currentTimeMillis();
        log.info("DELETE /api/products/{id} - delete: id={}", id);

        service.delete(id);

        log.info("DELETE /api/products/{id} - success: id={}, durationMs={}", id, System.currentTimeMillis() - t0);
    }

    @Operation(summary = "List products",
            description = "Returns a paginated list of products with optional filtering.")
    @ApiResponse(responseCode = "200", description = "Page of products")
    @GetMapping
    public Page<ProductResponse> list( @ParameterObject ProductFilter filter,
                                       @ParameterObject @PageableDefault(size = 10,
                                              sort = {"name", "price"},
                                              direction = Sort.Direction.ASC) Pageable pageable) {
        long t0 = System.currentTimeMillis();
        log.info("GET /api/products - list: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<ProductResponse> resp = service.list(filter, pageable);

        log.info("GET /api/products - success:  page={}, returned={}, total={}, durationMs={}",
                resp.getNumber(), resp.getNumberOfElements(), resp.getTotalElements(), System.currentTimeMillis() - t0);
        return resp;
    }


}
