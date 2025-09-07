package org.example.web.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.web.dto.order.OrderFilter;
import org.example.web.dto.order.OrderRequest;
import org.example.web.dto.order.OrderResponse;
import org.example.web.dto.orderProduct.ChangeQuantityRequest;
import org.example.web.dto.orderProduct.OrderProductRequest;
import org.example.web.model.OrderStatus;
import org.example.web.service.OrderService;
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
public class OrderController {

    private final OrderService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody OrderRequest req) {
        long t0 = System.currentTimeMillis();
        log.info("POST /api/orders - create: clientId={}", req.getClientId());

        OrderResponse resp = service.create(req);

        log.info("POST /api/orders - success: orderId={},durationMs={}", resp.getId(), System.currentTimeMillis() - t0);
        return resp;
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable UUID id,
                                      @RequestParam OrderStatus status) {
        long t0 = System.currentTimeMillis();
        log.info("PATCH /api/orders/{id}/status - updateStatus: id={}, newStatus={}", id, status);

        OrderResponse resp = service.updateStatus(id, status);

        log.info("PATCH /api/orders/{id}/status - updateStatus: id={}, status={}, durationMs={}", id, status, System.currentTimeMillis() - t0);
        return resp;
    }

    @GetMapping("/{id}")
    public OrderResponse getOne(@PathVariable UUID id) {
        long t0 = System.currentTimeMillis();
        log.info("GET /api/orders/{id} - getOne: id={}", id);

        OrderResponse resp = service.getOne(id);

        log.info("GET /api/orders/{id} - success: id={}, durationMs={}", id, System.currentTimeMillis() - t0);
        return resp;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        long t0 = System.currentTimeMillis();
        log.info("DELETE /api/orders/{id} - delete: id={}", id);

        service.delete(id);

        log.info("DELETE /api/orders/{id}- success: id={}, durationMs={}", id, System.currentTimeMillis() - t0);
    }

    @GetMapping
    public Page<OrderResponse> list(OrderFilter filter,
                                    @PageableDefault(size = 10,
                                            sort = "createdAt",
                                            direction = Sort.Direction.DESC) Pageable pageable) {
        long t0 = System.currentTimeMillis();
        log.info("GET /api/orders - list: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<OrderResponse> resp = service.findAll(filter, pageable);

        log.info("GET /api/orders- success: page={},returned={}, total={} durationMs={}",
                resp.getNumber(), resp.getNumberOfElements(), resp.getTotalElements(), System.currentTimeMillis() - t0);
        return resp;
    }

    @PostMapping("/{orderId}")
    public OrderResponse addProduct(@PathVariable UUID orderId,
                                    @Valid @RequestBody OrderProductRequest req) {
        long t0 = System.currentTimeMillis();
        log.info("POST /api/orders/{orderId} - addProduct: orderId={}, productId={}, quantity={}",
                orderId, req.getProductId(), req.getQuantity());

        OrderResponse resp = service.addProduct(orderId, req);
        log.info("POST /api/orders/{orderId} - success: orderId={}, productId={}, quantity={}, durationMs={}",
                orderId, req.getProductId(), req.getQuantity(), System.currentTimeMillis() - t0);
        return resp;
    }

    @PatchMapping("/{orderId}/items/{productId}")
    public OrderResponse changeProductQuantity(@PathVariable UUID orderId,
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

    @DeleteMapping("/{orderId}/items/{productId}")
    public OrderResponse removeProduct(@PathVariable UUID orderId,
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
