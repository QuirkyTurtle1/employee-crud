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
import org.example.web.dto.order.OrderFilter;
import org.example.web.dto.order.OrderRequest;
import org.example.web.dto.order.OrderResponse;
import org.example.web.dto.orderProduct.ChangeQuantityRequest;
import org.example.web.dto.orderProduct.OrderProductRequest;
import org.example.web.model.OrderStatus;
import org.example.web.service.OrderService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RequestMapping("/api/orders")
@RestController
@RequiredArgsConstructor
@Tag(name = "Orders")
public class OrderController {

    private final OrderService service;

    @Operation(summary = "Create a new order",
            description = "Creates an order with the given data and returns the created order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "409", description = "Conflict: client not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp":"2025-09-10T12:34:56Z",
                                      "status":409,
                                      "error":"Conflict",
                                      "path":"/api/orders",
                                      "code":"CONFLICT",
                                      "message":"Client with id '123e4567-e89b-12d3-a456-426614174000' does not exists",
                                      "requestId":"123e4567-e89b-12d3-a456-426614174000"
                                    }
                                    """)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody OrderRequest req) {
        long t0 = System.currentTimeMillis();
        log.info("POST /api/orders - create: clientId={}", req.getClientId());

        OrderResponse resp = service.create(req);

        log.info("POST /api/orders - success: orderId={},durationMs={}", resp.getId(), System.currentTimeMillis() - t0);
        return resp;
    }

    @Operation(summary = "Update order",
            description = "Updates the order with the given ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order updated"),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Conflict: Conflict: invalid status transition",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@Parameter(description = "Order ID", required = true)
                                      @PathVariable UUID id,
                                      @RequestParam OrderStatus status) {
        long t0 = System.currentTimeMillis();
        log.info("PATCH /api/orders/{id}/status - updateStatus: id={}, newStatus={}", id, status);

        OrderResponse resp = service.updateStatus(id, status);

        log.info("PATCH /api/orders/{id}/status - updateStatus: id={}, status={}, durationMs={}", id, status, System.currentTimeMillis() - t0);
        return resp;
    }

    @Operation(summary = "Get order by ID",
            description = "Returns a single order by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public OrderResponse getOne(@Parameter(description = "Order ID", required = true)
                                @PathVariable UUID id) {
        long t0 = System.currentTimeMillis();
        log.info("GET /api/orders/{id} - getOne: id={}", id);

        OrderResponse resp = service.getOne(id);

        log.info("GET /api/orders/{id} - success: id={}, durationMs={}", id, System.currentTimeMillis() - t0);
        return resp;
    }

    @Operation(summary = "Delete order",
            description = "Deletes the order with the given ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order deleted"),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Conflict: order has products",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(description = "Order ID", required = true)
                       @PathVariable UUID id) {
        long t0 = System.currentTimeMillis();
        log.info("DELETE /api/orders/{id} - delete: id={}", id);

        service.delete(id);

        log.info("DELETE /api/orders/{id}- success: id={}, durationMs={}", id, System.currentTimeMillis() - t0);
    }

    @Operation(summary = "List orders",
            description = "Returns a paginated list of orders with optional filtering.")
    @ApiResponse(responseCode = "200", description = "Page of orders")
    @GetMapping
    public Page<OrderResponse> list(@ParameterObject OrderFilter filter,
                                    @PageableDefault(size = 10,
                                            sort = "createdAt",
                                            direction = Sort.Direction.DESC) Pageable pageable) {
        long t0 = System.currentTimeMillis();
        log.info("GET /api/orders - list: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<OrderResponse> resp = service.findAll(filter, pageable);

        log.info("GET /api/orders - success: page={},returned={}, total={} durationMs={}",
                resp.getNumber(), resp.getNumberOfElements(), resp.getTotalElements(), System.currentTimeMillis() - t0);
        return resp;
    }

    @Operation(summary = "Add product to order",
            description = "Adds a product with the given quantity to an existing order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product added to order"),
            @ApiResponse(responseCode = "404", description = "Order or product not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Conflict: product already in order",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{orderId}")
    public OrderResponse addProduct(@Parameter(description = "Order ID", required = true)
                                    @PathVariable UUID orderId,
                                    @Valid @RequestBody OrderProductRequest req) {
        long t0 = System.currentTimeMillis();
        log.info("POST /api/orders/{orderId} - addProduct: orderId={}, productId={}, quantity={}",
                orderId, req.getProductId(), req.getQuantity());

        OrderResponse resp = service.addProduct(orderId, req);
        log.info("POST /api/orders/{orderId} - success: orderId={}, productId={}, quantity={}, durationMs={}",
                orderId, req.getProductId(), req.getQuantity(), System.currentTimeMillis() - t0);
        return resp;
    }

    @Operation(summary = "Change product quantity",
            description = "Changes the product quantity in an existing order..")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product quantity changed"),
            @ApiResponse(responseCode = "404", description = "Order or product not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Conflict: invalid quantity or order in non-editable status",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/{orderId}/items/{productId}")
    public OrderResponse changeProductQuantity(@Parameter(description = "Order ID", required = true)
                                               @PathVariable UUID orderId,
                                               @Parameter(description = "Product ID", required = true)
                                               @PathVariable UUID productId,
                                               @Valid @RequestBody ChangeQuantityRequest req) {
        long t0 = System.currentTimeMillis();
        log.info("PATCH /api/orders/{orderId}/items/{productId} - changeProductQuantity: orderId={}, productId={}, quantity={}",
                orderId, productId, req.quantity());

        OrderResponse resp = service.changeProductQuantity(orderId, productId, req.quantity());

        log.info("PATCH /api/orders/{orderId}/items/{productId} - success: orderId={}, productId={}, quantity={}, durationMs={}",
                orderId, productId, req.quantity(), System.currentTimeMillis() - t0);
        return resp;
    }

    @Operation(summary = "Remove product",
            description = " Removes product from the existing order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated order returned"),
            @ApiResponse(responseCode = "404", description = "Order or product not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Conflict: order in non-editable status",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{orderId}/items/{productId}")
    public OrderResponse removeProduct(@Parameter(description = "Order ID", required = true)
                                       @PathVariable UUID orderId,
                                       @Parameter(description = "Product ID", required = true)
                                       @PathVariable UUID productId) {
        long t0 = System.currentTimeMillis();
        log.info("DELETE /api/orders/{orderId}/items/{productId} - removeProduct: orderId={}, productId={}",
                orderId, productId);

        OrderResponse resp = service.removeProduct(orderId, productId);

        log.info("DELETE /api/orders/{orderId}/items/{productId} - success: orderId={}, productId={}, durationMs={}",
                orderId, productId, System.currentTimeMillis() - t0);
        return resp;
    }

}
