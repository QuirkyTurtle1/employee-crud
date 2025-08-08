package org.example.web.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository repo;
    private final ProductMapper mapper;
    private final OrderProductRepository orderProductRepo;

    public ProductResponse create(ProductRequest req) {
        if (repo.existsByNameIgnoreCase(req.getName())) {
            throw new DuplicateProductNameException(req.getName());
        }
        Product entity = mapper.toEntity(req);
        Product saved = repo.save(entity);

        return mapper.toResponse(saved);
    }

    public ProductResponse getOne(UUID id) {
        Product entity = repo.findById(id).orElseThrow(() -> new NotFoundException("Product",id));
        return mapper.toResponse(entity);
    }

    public ProductResponse update(UUID id, ProductRequest req) {
        Product entity = repo.findById(id).orElseThrow(() -> new NotFoundException("Product",id));
        if (!entity.getName().equalsIgnoreCase(req.getName())
                && repo.existsByNameIgnoreCase(req.getName())) {
            throw new DuplicateProductNameException(req.getName());
        }
        mapper.updateEntity(req, entity);
        return mapper.toResponse(entity);
    }

    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("Product",id);
        }
        if (orderProductRepo.existsByProduct_Id(id)) {
            throw new ProductInUseException(id);
        }
        repo.deleteById(id);
    }

    public Page<ProductResponse> list(
            ProductFilter filter,
            Pageable pageable) {

        Specification<Product> spec = Specification
                .where(SpecBuilder.<Product>like("name", filter.name().orElse(null)))
                .and(SpecBuilder.between("price",
                        filter.priceMin().orElse(null),
                        filter.priceMax().orElse(null)));

        return repo.findAll(spec, pageable).map(mapper::toResponse);
    }
}
