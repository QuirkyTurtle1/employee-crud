package org.example.web.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.web.dto.order.OrderFilter;
import org.example.web.dto.order.OrderRequest;
import org.example.web.dto.order.OrderResponse;
import org.example.web.dto.orderProduct.OrderProductRequest;
import org.example.web.exception.DuplicateProductInOrderException;
import org.example.web.exception.NotFoundException;
import org.example.web.mappers.OrderMapper;
import org.example.web.model.Client;
import org.example.web.model.Order;
import org.example.web.model.OrderProduct;
import org.example.web.model.OrderStatus;
import org.example.web.model.Product;
import org.example.web.repository.ClientRepository;
import org.example.web.repository.OrderRepository;
import org.example.web.repository.ProductRepository;
import org.example.web.util.OrderSpecs;
import org.example.web.util.SpecBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepo;
    private final ClientRepository clientRepo;
    private final ProductRepository productRepository;
    private final OrderMapper mapper;

    public OrderResponse create(OrderRequest req) {
        Client client = fetchClient(req.getClientId());

        Map<UUID, Product> products = fetchProducts(req.getProducts());
        Set<OrderProduct> items = buildItems(req.getProducts(), products);

        OrderStatus status = req.getStatus() != null
                ? req.getStatus()
                : OrderStatus.NEW;

        Order order = Order.builder()                                   // 3
                .client(client)
                .status(status)
                .createdAt(Instant.now())
                .items(items)
                .build();
        items.forEach(i -> i.setOrder(order));

        return mapper.toResponse(orderRepo.save(order));
    }

    public OrderResponse updateStatus(UUID id, OrderStatus status) {
        Order entity = orderRepo.findById(id).orElseThrow(() -> new NotFoundException("Order", id));

        entity.setStatus(status);


        return mapper.toResponse(entity);
    }

    public OrderResponse getOne(UUID id) {
        Order entity = orderRepo.findById(id).orElseThrow(() -> new NotFoundException("Order", id));

        return mapper.toResponse(entity);
    }

    public void delete(UUID id) {
        Order entity = orderRepo.findById(id).orElseThrow(() -> new NotFoundException("Order", id));

        orderRepo.delete(entity);
    }

    public Page<OrderResponse> findAll(OrderFilter filter, Pageable pageable) {
        Specification<Order> spec = OrderSpecs.build(filter);

        return orderRepo.findAll(spec, pageable).map(mapper::toResponse);
    }

    public OrderResponse addProduct(UUID orderId, @Valid OrderProductRequest req) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));

        boolean exists = order.getItems().stream()
                .anyMatch(i -> i.getProduct().getId().equals(req.getProductId()));
        if (exists) {
            throw new DuplicateProductInOrderException(req.getProductId());
        }

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(()-> new NotFoundException("Product", req.getProductId())  );

        OrderProduct item = OrderProduct.builder()
                .order(order)
                .product(product)
                .quantity(req.getQuantity())
                .build();

        order.getItems().add(item);

        return mapper.toResponse(order);
    }

    public OrderResponse changeProductQuantity(UUID orderId, UUID productId, @Min(1) int quantity) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));

        OrderProduct item = order.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Order item (product)", productId));

        item.setQuantity(quantity);
        return mapper.toResponse(order);
    }

    public OrderResponse removeProduct(UUID orderId, UUID productId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));

        boolean removed = order.getItems().removeIf(
                i -> i.getProduct().getId().equals(productId)
        );

        if (!removed) {
            throw new NotFoundException("Order item (product)", productId);
        }

        return mapper.toResponse(order);
    }

    /**
     * private helpers
     */

    private Client fetchClient(UUID clientId) {
        return clientRepo.findById(clientId).orElseThrow(() -> new NotFoundException("Client", clientId));
    }

    /**
     * грузим все товары одним запросом, заодно проверяем дубликаты productId
     */
    private Map<UUID, Product> fetchProducts(List<OrderProductRequest> list) {

        // дубликаты productId в самом запросе
        Set<UUID> unique = new HashSet<>();
        list.forEach(r -> {
            if (!unique.add(r.getProductId())) {
                throw new DuplicateProductInOrderException(r.getProductId());
            }
        });

        // сами продукты
        Map<UUID, Product> map = productRepository.findAllById(unique).stream()
                .collect(toMap(Product::getId, p -> p));

        // недостающие id
        unique.forEach(id -> {
            if (!map.containsKey(id)) {
                throw new NotFoundException("Product", id);
            }
        });
        return map;
    }

    private Set<OrderProduct> buildItems(List<OrderProductRequest> orderProductRequestsList,
                                         Map<UUID, Product> products) {

        return orderProductRequestsList.stream()
                .map(r -> OrderProduct.builder()
                        .product(products.get(r.getProductId()))
                        .quantity(r.getQuantity())
                        .build())
                .collect(Collectors.toSet());
    }



}
