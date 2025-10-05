package org.example.web.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.web.dto.product.ProductFilter;
import org.example.web.dto.product.ProductRequest;
import org.example.web.dto.product.ProductResponse;
import org.example.web.exception.DuplicateProductNameException;
import org.example.web.exception.NotFoundException;
import org.example.web.exception.ProductInUseException;
import org.example.web.mappers.ProductMapper;
import org.example.web.model.Client;
import org.example.web.model.Product;
import org.example.web.repository.OrderProductRepository;
import org.example.web.repository.ProductRepository;
import org.example.web.util.SpecBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository repo;
    private final ProductMapper mapper;
    private final OrderProductRepository orderProductRepo;

    public ProductResponse create(ProductRequest req) {
        log.debug("Product create start: name={}", req.getName());
        if (repo.existsByNameIgnoreCase(req.getName())) {
            throw new DuplicateProductNameException(req.getName());
        }
        Product entity = mapper.toEntity(req);

        Product saved = repo.save(entity);
        log.info("Product created: id={}", saved.getId());

        return mapper.toResponse(saved);
    }

    public ProductResponse getOne(UUID id) {
        log.debug("Product getOne start: id={}", id);

        Product entity = repo.findById(id).orElseThrow(() -> new NotFoundException("Product", id));
        return mapper.toResponse(entity);
    }

    public ProductResponse update(UUID id, ProductRequest req) {
        log.debug("Product update start: id={}, newName={}, newPrice={}",
                id, req.getName(), req.getPrice());
        Product entity = repo.findById(id).orElseThrow(() -> new NotFoundException("Product", id));
        if (!entity.getName().equalsIgnoreCase(req.getName())
                && repo.existsByNameIgnoreCaseAndIdNot(req.getName(), id)) {
            throw new DuplicateProductNameException(req.getName());
        }
        mapper.updateEntity(req, entity);
        log.info("Product updated: id={}, newName={}, newPrice={}",
                id, entity.getName(), entity.getPrice());
        return mapper.toResponse(entity);
    }

    public void delete(UUID id) {
        log.debug("Product delete start: id={}", id);

        Product entity = repo.findById(id).orElseThrow(()-> new NotFoundException("Product",    id));

        repo.delete(entity);
        log.info("Product deleted: id={}", id);
    }

    public Page<ProductResponse> list(ProductFilter filter, Pageable pageable) {
        log.debug("Find products start: filter={}", filter);
        Specification<Product> spec = Specification
                .where(SpecBuilder.<Product>like("name", filter.name()))
                .and(SpecBuilder.between("price",
                        filter.priceMin(),
                        filter.priceMax()));

        Page<Product> page = repo.findAll(spec, pageable);
        log.debug("Products page loaded: number={}, returned={}, total={}",
                page.getNumber(), page.getNumberOfElements(), page.getTotalElements());

        Page<ProductResponse> resp = page.map(mapper::toResponse);
        log.debug("Products mapped: {}", resp.getNumberOfElements());
        return resp;
    }


}
