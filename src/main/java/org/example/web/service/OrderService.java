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
        int requested = req.getProducts() == null ? 0 : req.getProducts().size();
        log.debug("Начало создания заказа: clientId={}, requestedItems={}", req.getClientId(), requested);

        Client client = fetchClient(req.getClientId());
        log.debug("Клиент найден: {}", client.getId());

        Map<UUID, Product> products = fetchProducts(req.getProducts());
        log.debug("Продукты найдены: {}", products.size());

        if (products.size() != requested) {
            log.warn("Некоторые продукты отсутствуют: запрошено={}, найдено={}",
                    requested, products.size());
        }

        Set<OrderProduct> items = buildItems(req.getProducts(), products);

        OrderStatus status = req.getStatus() != null
                ? req.getStatus()
                : OrderStatus.NEW;
        log.debug("Создаю заказ: clientId={}, status={}, itemCount={}", client.getId(), status, items.size());

        Order order = Order.builder()
                .client(client)
                .status(status)
                .items(items)
                .build();
        items.forEach(i -> i.setOrder(order));

        Order saved = orderRepo.save(order);
        log.info("Заказ сохранён: orderId={}", saved.getId());

        OrderResponse resp = getOne(saved.getId());
        log.debug("Заказ преобразован в ответ: id={}", resp.getId());

        return resp;
    }

    public OrderResponse updateStatus(UUID id, OrderStatus status) {
        log.debug("Заказ updateStatus начало: id={}, newStatus={}", id, status);

        Order entity = orderRepo.findById(id).orElseThrow(() -> new NotFoundException("Order", id));
        OrderStatus oldStatus = entity.getStatus();
        if (oldStatus == status) {
            log.debug("Статус заказа не изменен: id={}, status={}", id, oldStatus);
            return getOne(id);
        }

        entity.setStatus(status);
        log.info("Статус заказа обновлен: id={}, from={}, to={}", id, oldStatus, status);
        return getOne(id);
    }

    public OrderResponse getOne(UUID id) {
        log.debug("Заказ getOne начало: id={}", id);

        Order entity = orderRepo.findDetailedById(id).orElseThrow(() -> new NotFoundException("Order", id));

        if (log.isDebugEnabled()) {
            int items = (entity.getItems() == null) ? 0 : entity.getItems().size();
            log.debug("Заказ был загружен: id={}, itemCount={}", entity.getId(), items);
        }

        return mapper.toResponse(entity);
    }

    public void delete(UUID id) {
        log.debug("Заказ delete начало: id={}", id);

        Order entity = orderRepo.findById(id).orElseThrow(() -> new NotFoundException("Order", id));

        orderRepo.delete(entity);
        log.info("Заказ удален: id={}", id);
    }

    public Page<OrderResponse> findAll(OrderFilter filter, Pageable pageable) {
        log.debug("Список заказов начало: orderFilter={}", filter);

        Specification<Order> spec = OrderSpecs.build(filter);
        Page<Order> page = orderRepo.findAll(spec, pageable);
        log.debug("Загруженная страница заказов: number={}, returned={}, total={}",
                page.getNumber(), page.getNumberOfElements(), page.getTotalElements());
        if (page.isEmpty()) {
            log.debug("Найденные заказы: пусто");
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
        log.debug("Найденные заказы: {}", content.size());

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    public OrderResponse addProduct(UUID orderId, @Valid OrderProductRequest req) {
        log.debug("Добавление продукта в заказ начало: orderId={}, productId={}, quantity={}",
                orderId, req.getProductId(), req.getQuantity());

        if (orderProductRepo.existsByOrderIdAndProductId(orderId, req.getProductId())) {
            throw new DuplicateProductInOrderException(req.getProductId());
        }

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
        log.debug("Заказ загружен: id={}, currentItems={}",
                order.getId(), order.getItems().size());

        Product product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new NotFoundException("Product", req.getProductId()));
        log.debug("Продукт загружен: id={}", product.getId());

        OrderProduct item = OrderProduct.builder()
                .order(order)
                .product(product)
                .quantity(req.getQuantity())
                .build();

        order.getItems().add(item);
        orderRepo.flush();
        log.info("Продукт был добавлен в заказ: orderId={}, productId={}, quantity={}",
                orderId, req.getProductId(), req.getQuantity());
        return getOne(orderId);
    }

    public OrderResponse changeProductQuantity(UUID orderId, UUID productId, @Min(1) int quantity) {
        log.debug("Изменение количество продукта в заказе начало: orderId={}, productId={}, newQuantity={}",
                orderId, productId, quantity);

        OrderProduct item = orderProductRepo.findByOrderIdAndProductId(orderId, productId)
                .orElseThrow(() -> new NotFoundException("Order item (product)", productId));

        int oldQuantity = item.getQuantity();
        if (oldQuantity == quantity) {
            log.debug("Количество не изменилось: orderId={}, productId={}, quantity={}", orderId, productId, quantity);
            return getOne(orderId);
        }

        item.setQuantity(quantity);
        log.info("Было изменено количество продукта в заказе: orderId={}, productId={}, from={}, to={}",
                orderId, productId, oldQuantity, quantity);
        return getOne(orderId);
    }

    public OrderResponse removeProduct(UUID orderId, UUID productId) {
        log.debug("Удаление продукта из заказа начало: orderId={}, productId={}", orderId, productId);
        int deleted = orderProductRepo.deleteByOrderIdAndProductId(orderId, productId);
        if (deleted == 0) {
            throw new NotFoundException("Order item (product)", productId);
        }
        log.debug("Продукт был удален из заказа: orderId={}, productId={}", orderId, productId);

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
