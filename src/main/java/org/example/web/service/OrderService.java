package org.example.web.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.example.web.repository.OrderProductRepository;
import org.example.web.repository.OrderRepository;
import org.example.web.repository.ProductRepository;
import org.example.web.util.OrderSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepo;
    private final ClientRepository clientRepo;
    private final ProductRepository productRepo;
    private final OrderProductRepository orderProductRepo;
    private final OrderMapper mapper;

    public OrderResponse create(OrderRequest req) {
        int requested = (req.getProducts() == null) ? 0 : req.getProducts().size();
        log.debug("Order create start: clientId={}, requestedItems={}", req.getClientId(), requested);

        Client client = fetchClient(req.getClientId());
        log.debug("Client loaded: id={}", client.getId());

        Map<UUID, Product> products = fetchProducts(req.getProducts());
        log.debug("Products loaded: {}", products.size());

        if (products.size() != requested) {
            log.warn("Some products are missing: requested={}, found={}", requested, products.size());
        }

        Set<OrderProduct> items = buildItems(req.getProducts(), products);

        OrderStatus status = (req.getStatus() != null) ? req.getStatus() : OrderStatus.NEW;
        log.debug("Compose order: clientId={}, status={}, itemCount={}", client.getId(), status, items.size());

        Order order = Order.builder()
                .client(client)
                .status(status)
                .items(items)
                .build();
        items.forEach(i -> i.setOrder(order));

        Order saved = orderRepo.save(order);
        log.info("Order saved: id={}", saved.getId());

        OrderResponse resp = getOne(saved.getId());
        log.debug("Order mapped to response: id={}", resp.getId());

        return resp;
    }

    public OrderResponse updateStatus(UUID id, OrderStatus status) {
        log.debug("Order updateStatus start: id={}, newStatus={}", id, status);

        Order entity = orderRepo.findById(id).orElseThrow(() -> new NotFoundException("Order", id));
        OrderStatus oldStatus = entity.getStatus();
        if (oldStatus == status) {
            log.debug("Order status unchanged: id={}, status={}", id, oldStatus);
            return getOne(id);
        }

        entity.setStatus(status);
        log.info("Order status updated: id={}, from={}, to={}", id, oldStatus, status);
        return getOne(id);
    }

    public OrderResponse getOne(UUID id) {
        log.debug("Order getOne start: id={}", id);

        Order entity = orderRepo.findDetailedById(id).orElseThrow(() -> new NotFoundException("Order", id));

        if (log.isDebugEnabled()) {
            int items = (entity.getItems() == null) ? 0 : entity.getItems().size();
            log.debug("Order loaded: id={}, itemCount={}", entity.getId(), items);
        }

        return mapper.toResponse(entity);
    }

    public void delete(UUID id) {
        log.debug("Order delete start: id={}", id);

        Order entity = orderRepo.findById(id).orElseThrow(() -> new NotFoundException("Order", id));

        orderRepo.delete(entity);
        log.info("Order deleted: id={}", id);
    }

    public Page<OrderResponse> findAll(OrderFilter filter, Pageable pageable) {
        log.debug("Find orders start: filter={}", filter);

        Specification<Order> spec = OrderSpecs.build(filter);
        Page<Order> page = orderRepo.findAll(spec, pageable);
        log.debug("Orders page loaded: number={}, returned={}, total={}",
                page.getNumber(), page.getNumberOfElements(), page.getTotalElements());

        if (page.isEmpty()) {
            log.debug("Find orders: empty result");
            return Page.empty(pageable);
        }

        List<UUID> ids = page.getContent().stream().map(Order::getId).toList();

        List<Order> detailed = orderRepo.findByIdIn(ids);

        Map<UUID, Order> byId = detailed.stream()
                .collect(Collectors.toMap(Order::getId, Function.identity()));

        List<OrderResponse> content = ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(mapper::toResponse)
                .toList();
        log.debug("Orders mapped: {}", content.size());

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    public OrderResponse addProduct(UUID orderId, @Valid OrderProductRequest req) {
        log.debug("Order addProduct start: orderId={}, productId={}, qty={}",
                orderId, req.getProductId(), req.getQuantity());

        if (orderProductRepo.existsByOrderIdAndProductId(orderId, req.getProductId())) {
            throw new DuplicateProductInOrderException(req.getProductId());
        }

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));

        if (log.isDebugEnabled()) {
            int currentItems = (order.getItems() == null) ? 0 : order.getItems().size();
            log.debug("Order loaded: id={}, currentItems={}", order.getId(), currentItems);
        }

        Product product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new NotFoundException("Product", req.getProductId()));
        log.debug("Product loaded: id={}", product.getId());

        OrderProduct item = OrderProduct.builder()
                .order(order)
                .product(product)
                .quantity(req.getQuantity())
                .build();

        order.getItems().add(item);
        orderRepo.flush();

        log.info("Order updated: item added: orderId={}, productId={}, qty={}",
                orderId, req.getProductId(), req.getQuantity());
        return getOne(orderId);
    }

    public OrderResponse changeProductQuantity(UUID orderId, UUID productId, @Min(1) int quantity) {
        log.debug("Order changeProductQuantity start: orderId={}, productId={}, newQty={}",
                orderId, productId, quantity);

        OrderProduct item = orderProductRepo.findByOrderIdAndProductId(orderId, productId)
                .orElseThrow(() -> new NotFoundException("Order item (product)", productId));

        int oldQuantity = item.getQuantity();
        if (oldQuantity == quantity) {
            log.debug("Quantity unchanged: orderId={}, productId={}, qty={}", orderId, productId, quantity);
            return getOne(orderId);
        }

        item.setQuantity(quantity);
        log.info("Order item quantity updated: orderId={}, productId={}, from={}, to={}",
                orderId, productId, oldQuantity, quantity);
        return getOne(orderId);
    }

    public OrderResponse removeProduct(UUID orderId, UUID productId) {
        log.debug("Order removeProduct start: orderId={}, productId={}", orderId, productId);

        int deleted = orderProductRepo.deleteByOrderIdAndProductId(orderId, productId);
        if (deleted == 0) {
            throw new NotFoundException("Order item (product)", productId);
        }

        log.info("Order updated: item removed: orderId={}, productId={}", orderId, productId);
        return getOne(orderId);
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
        Map<UUID, Product> map = productRepo.findAllById(unique).stream()
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
