package org.example.web.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.web.dto.order.OrderFilter;
import org.example.web.dto.order.OrderRequest;
import org.example.web.dto.order.OrderResponse;
import org.example.web.exception.NotFoundException;
import org.example.web.mappers.OrderMapper;
import org.example.web.model.Client;
import org.example.web.model.Order;
import org.example.web.model.OrderStatus;
import org.example.web.repository.ClientRepository;
import org.example.web.repository.OrderRepository;
import org.example.web.util.SpecBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepo;
    private final ClientRepository clientRepo;
    private final OrderMapper mapper;

    public OrderResponse create(OrderRequest req) {
        Client client = clientRepo.findById(req.getClientId())
                .orElseThrow(() -> new NotFoundException(req.getClientId()));

        Order entity = mapper.toEntity(req);
        entity.setClient(client);

        Order saved = orderRepo.save(entity);
        return mapper.toResponse(saved);
    }

    public OrderResponse updateStatus(UUID id, OrderStatus status) {
        Order entity = orderRepo.findById(id).orElseThrow(() -> new NotFoundException(id));

        entity.setStatus(status);

        return mapper.toResponse(entity);
    }

    public OrderResponse getOne(UUID id) {
        Order entity = orderRepo.findById(id).orElseThrow(() -> new NotFoundException(id));

        return mapper.toResponse(entity);
    }

    public void delete (UUID id) {
        Order entity = orderRepo.findById(id).orElseThrow(() -> new NotFoundException(id));

        orderRepo.delete(entity);
    }

    public Page<OrderResponse> findAll (OrderFilter filter, Pageable pageable) {
        Specification<Order> spec = Specification
                .where(SpecBuilder.<Order>eq("status", filter.status().orElse(null)))
                .and(SpecBuilder.between("createdAt",
                        filter.from().orElse(null),
                        filter.to().orElse(null)));

        return orderRepo.findAll(spec, pageable).map(mapper::toResponse);
    }

}
