package org.example.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.web.dto.client.ClientResponse;
import org.example.web.dto.product.ProductFilter;
import org.example.web.dto.product.ProductRequest;
import org.example.web.dto.product.ProductResponse;
import org.example.web.service.ProductService;
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
public class ProductController {

    private final ProductService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest req) {
        long t0 = System.currentTimeMillis();
        log.info("POST /api/products - create: name={}", req.getName());

        ProductResponse resp = service.create(req);

        log.info("POST /api/products - success: id={}, durationMs={}", resp.getId(), System.currentTimeMillis() - t0);
        return resp;
    }

    @GetMapping("/{id}")
    public ProductResponse getOne(@PathVariable UUID id) {
        long t0 = System.currentTimeMillis();
        log.info("GET /api/products/{id} - getOne: id={}", id);

        ProductResponse resp = service.getOne(id);

        log.info("GET /api/products/{id} - success: id={}, durationMs={}", resp.getId(), System.currentTimeMillis() - t0);
        return resp;
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable UUID id,
                                  @Valid @RequestBody ProductRequest req) {
        long t0 = System.currentTimeMillis();
        log.info("PUT /api/products/{id} - update: id={}", id);

        ProductResponse resp = service.update(id, req);

        log.info("PUT /api/products/{id} - success: id={}, durationMs={}",
                resp.getId(), System.currentTimeMillis() - t0);
        return resp;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        long t0 = System.currentTimeMillis();
        log.info("DELETE /api/products/{id} - delete: id={}", id);

        service.delete(id);

        log.info("DELETE /api/products/{id} - success: id={}, durationMs={}", id, System.currentTimeMillis() - t0);
    }

    @GetMapping
    public Page<ProductResponse> list(ProductFilter filter,
                                      @PageableDefault(size = 10,
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
