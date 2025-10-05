package org.example.web.service;

import org.example.web.dto.order.OrderFilter;
import org.example.web.dto.order.OrderRequest;
import org.example.web.dto.order.OrderResponse;
import org.example.web.dto.orderProduct.OrderProductRequest;
import org.example.web.exception.DuplicateProductInOrderException;
import org.example.web.exception.NotFoundException;
import org.example.web.model.Client;
import org.example.web.model.Order;
import org.example.web.model.OrderProduct;
import org.example.web.model.OrderStatus;
import org.example.web.model.Product;
import org.example.web.repository.ClientRepository;
import org.example.web.repository.OrderProductRepository;
import org.example.web.repository.OrderRepository;
import org.example.web.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceIT {
    @Autowired
    private OrderService orderService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Test
    void createOrder_whenValidRequest_savesSuccessfully() {
        // given
        Client client = clientRepository.save(Client.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+79001234567")
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Book")
                .description("Some book")
                .price(BigDecimal.valueOf(19.99))
                .build());

        OrderRequest request = new OrderRequest(
                client.getId(),
                OrderStatus.NEW,
                List.of(new OrderProductRequest(product.getId(), 2))
        );

        // when
        OrderResponse response = orderService.create(request);

        // then
        assertNotNull(response.getId());
        assertEquals(client.getId(), response.getClientId());
        assertEquals(OrderStatus.NEW, response.getStatus());
        assertEquals(1, response.getItems().size());
        assertEquals(2, response.getItemsTotal());

        Order savedOrder = orderRepository.findById(response.getId())
                .orElseThrow(() -> new AssertionError("Order not found in DB"));

        assertEquals(client.getId(), savedOrder.getClient().getId());
        assertEquals(OrderStatus.NEW, savedOrder.getStatus());
        assertEquals(1, savedOrder.getItems().size());
    }

    @Test
    void createOrder_whenClientNotFound_throwsNotFoundException() {
        // given
        UUID fakeClientId = UUID.randomUUID();

        Product product = productRepository.save(Product.builder()
                .name("Book")
                .description("Some book")
                .price(BigDecimal.valueOf(19.99))
                .build());

        OrderRequest request = new OrderRequest(
                fakeClientId,
                OrderStatus.NEW,
                List.of(new OrderProductRequest(product.getId(), 1))
        );

        // when + then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> orderService.create(request));

        assertTrue(exception.getMessage().contains("Client"));
    }

    @Test
    void createOrder_whenProductNotFound_throwsNotFoundException() {
        // given
        Client client = clientRepository.save(Client.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.smith@example.com")
                .phone("+79001112233")
                .build());

        UUID fakeProductId = UUID.randomUUID();

        OrderRequest request = new OrderRequest(
                client.getId(),
                OrderStatus.NEW,
                List.of(new OrderProductRequest(fakeProductId, 2))
        );

        // when + then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> orderService.create(request));

        assertTrue(exception.getMessage().contains("Product"));
    }

    @Test
    void createOrder_whenDuplicateProducts_throwsDuplicateProductException() {
        // given
        Client client = clientRepository.save(Client.builder()
                .firstName("Bob")
                .lastName("Johnson")
                .email("bob.johnson@example.com")
                .phone("+79003334455")
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Laptop")
                .description("Gaming laptop")
                .price(BigDecimal.valueOf(999.99))
                .build());

        OrderRequest request = new OrderRequest(
                client.getId(),
                OrderStatus.NEW,
                List.of(
                        new OrderProductRequest(product.getId(), 1),
                        new OrderProductRequest(product.getId(), 2)
                )
        );

        // when + then
        DuplicateProductInOrderException exception = assertThrows(DuplicateProductInOrderException.class,
                () -> orderService.create(request));

        assertTrue(exception.getMessage().contains(product.getId().toString()));
    }

    @Test
    void updateStatus_whenValidRequest_updatesStatusSuccessfully() {
        // given
        Client client = clientRepository.save(Client.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+79001234567")
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Keyboard")
                .description("Mechanical keyboard")
                .price(BigDecimal.valueOf(49.99))
                .build());

        OrderRequest request = new OrderRequest(
                client.getId(),
                OrderStatus.NEW,
                List.of(new OrderProductRequest(product.getId(), 1))
        );

        OrderResponse created = orderService.create(request);

        // when
        OrderResponse updated = orderService.updateStatus(created.getId(), OrderStatus.COMPLETED);

        // then
        assertEquals(OrderStatus.COMPLETED, updated.getStatus());
        assertEquals(created.getId(), updated.getId());
    }

    @Test
    void updateStatus_whenOrderNotFound_throwsNotFoundException() {
        // given
        UUID missingOrderId = UUID.randomUUID();

        // when + then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> orderService.updateStatus(missingOrderId, OrderStatus.NEW));

        assertTrue(exception.getMessage().contains("Order"));
    }

    @Test
    void updateStatus_whenSameStatus_doesNotChangeAnything() {
        // given
        Client client = clientRepository.save(Client.builder()
                .firstName("Mike")
                .lastName("Smith")
                .email("mike.smith@example.com")
                .phone("+79009998877")
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Mouse")
                .description("Wireless mouse")
                .price(BigDecimal.valueOf(25.50))
                .build());

        OrderRequest request = new OrderRequest(
                client.getId(),
                OrderStatus.NEW,
                List.of(new OrderProductRequest(product.getId(), 1))
        );

        OrderResponse created = orderService.create(request);

        // when
        OrderResponse updated = orderService.updateStatus(created.getId(), OrderStatus.NEW);

        // then
        assertEquals(OrderStatus.NEW, updated.getStatus());
        assertEquals(created.getId(), updated.getId());
    }

    @Test
    void updateStatus_whenStatusIsNull_throwsException() {
        // given
        Client client = clientRepository.save(Client.builder()
                .firstName("Kate")
                .lastName("Wilson")
                .email("kate.wilson@example.com")
                .phone("+79005554433")
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Monitor")
                .description("Full HD Monitor")
                .price(BigDecimal.valueOf(129.99))
                .build());

        OrderRequest request = new OrderRequest(
                client.getId(),
                OrderStatus.NEW,
                List.of(new OrderProductRequest(product.getId(), 1))
        );

        OrderResponse created = orderService.create(request);

        // when + then
        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateStatus(created.getId(), null));
    }

    @Test
    void deleteOrder_whenOrderExists_deletesSuccessfully() {
        // given
        Client client = clientRepository.save(Client.builder()
                .firstName("Kate")
                .lastName("Wilson")
                .email("kate.wilson@example.com")
                .phone("+79005554433")
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Monitor")
                .description("Full HD Monitor")
                .price(BigDecimal.valueOf(129.99))
                .build());

        OrderRequest request = new OrderRequest(
                client.getId(),
                OrderStatus.NEW,
                List.of(new OrderProductRequest(product.getId(), 1))
        );

        OrderResponse created = orderService.create(request);
        UUID orderId = created.getId();

        assertTrue(orderRepository.findById(orderId).isPresent());

        // when
        orderService.delete(orderId);

        // then
        assertFalse(orderRepository.findById(orderId).isPresent());
    }

    @Test
    void deleteOrder_whenOrderNotFound_throwsNotFoundException() {
        // given
        UUID fakeId = UUID.randomUUID();

        // when + then
        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                orderService.delete(fakeId)
        );

        assertTrue(ex.getMessage().contains("Order with id " + fakeId));
    }

    @Test
    void deleteOrder_whenOrderExists_cascadesToOrderProducts() {
        // given
        Client client = clientRepository.save(Client.builder()
                .firstName("Kate")
                .lastName("Wilson")
                .email("kate.wilson@example.com")
                .phone("+79005554433")
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Monitor")
                .description("Full HD Monitor")
                .price(BigDecimal.valueOf(129.99))
                .build());

        OrderRequest request = new OrderRequest(
                client.getId(),
                OrderStatus.NEW,
                List.of(new OrderProductRequest(product.getId(), 3))
        );

        OrderResponse created = orderService.create(request);
        UUID orderId = created.getId();

        assertTrue(orderRepository.findById(orderId).isPresent());
        List<OrderProduct> orderProductsBefore = orderProductRepository.findAll();
        assertFalse(orderProductsBefore.isEmpty());

        // when
        orderService.delete(orderId);

        // then
        assertFalse(orderRepository.findById(orderId).isPresent());

        List<OrderProduct> orderProductsAfter = orderProductRepository.findAll();
        assertTrue(orderProductsAfter.isEmpty(), "Order products should be deleted via cascade");
    }

    @Test
    void getOne_whenOrderExists_returnsOrderResponse() {
        // given
        Client client = clientRepository.save(
                Client.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .email("john.doe@example.com")
                        .phone("+79001234567")
                        .build()
        );

        Product product = productRepository.save(
                Product.builder()
                        .name("Book")
                        .description("Some book")
                        .price(BigDecimal.valueOf(19.99))
                        .build()
        );

        OrderRequest request = new OrderRequest(
                client.getId(),
                OrderStatus.NEW,
                List.of(new OrderProductRequest(product.getId(), 2))
        );

        OrderResponse created = orderService.create(request);

        // when
        OrderResponse found = orderService.getOne(created.getId());

        // then
        assertEquals(created.getId(), found.getId());
        assertEquals(created.getClientId(), found.getClientId());
        assertEquals(created.getItemsTotal(), found.getItemsTotal());
        assertEquals(OrderStatus.NEW, found.getStatus());
        assertFalse(found.getItems().isEmpty());
        assertEquals(product.getName(), found.getItems().get(0).getName());
    }

    @Test
    void getOne_whenOrderNotFound_throwsNotFoundException() {
        // given
        UUID fakeOrderId = UUID.randomUUID();

        // when + then
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> orderService.getOne(fakeOrderId)
        );

        assertTrue(ex.getMessage().contains("Order with id " + fakeOrderId));
    }

    @Test
    void findAll_whenOrdersExist_returnsPagedOrders() {
        // given
        Client client = clientRepository.save(
                Client.builder()
                        .firstName("Alice")
                        .lastName("Smith")
                        .email("alice@example.com")
                        .phone("+79005553322")
                        .build()
        );

        Product product = productRepository.save(
                Product.builder()
                        .name("Notebook")
                        .description("A5 size")
                        .price(BigDecimal.valueOf(14.99))
                        .build()
        );

        for (int i = 0; i < 3; i++) {
            OrderRequest req = new OrderRequest(
                    client.getId(),
                    OrderStatus.NEW,
                    List.of(new OrderProductRequest(product.getId(), i + 1))
            );
            orderService.create(req);
        }

        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        // when
        Page<OrderResponse> page = orderService.findAll(new OrderFilter(null, null, null, null), pageable);

        // then
        assertEquals(3, page.getTotalElements());
        assertEquals(1, page.getTotalPages());
        assertEquals(3, page.getContent().size());
        assertTrue(page.getContent().stream().allMatch(o -> o.getClientId().equals(client.getId())));
    }

    @Test
    void findAll_whenFilteredByStatus_returnsOnlyMatchingOrders() {
        // given
        Client client = clientRepository.save(
                Client.builder()
                        .firstName("Bob")
                        .lastName("Brown")
                        .email("bob@example.com")
                        .phone("+79001112233")
                        .build()
        );

        Product product = productRepository.save(
                Product.builder()
                        .name("Pen")
                        .description("Blue ink")
                        .price(BigDecimal.valueOf(2.99))
                        .build()
        );

        OrderRequest newOrder = new OrderRequest(
                client.getId(),
                OrderStatus.NEW,
                List.of(new OrderProductRequest(product.getId(), 1))
        );
        OrderRequest completedOrder = new OrderRequest(
                client.getId(),
                OrderStatus.COMPLETED,
                List.of(new OrderProductRequest(product.getId(), 2))
        );
        orderService.create(newOrder);
        orderService.create(completedOrder);

        OrderFilter filter = new OrderFilter("COMPLETED", null, null, null);


        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<OrderResponse> page = orderService.findAll(filter, pageable);

        // then
        assertEquals(1, page.getTotalElements());
        assertEquals(OrderStatus.COMPLETED, page.getContent().get(0).getStatus());
    }

    @Test
    void findAll_whenNoOrders_returnsEmptyPage() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        OrderFilter filter = new OrderFilter(null, null, null, null);

        // when
        Page<OrderResponse> page = orderService.findAll(filter, pageable);

        // then
        assertTrue(page.isEmpty());
        assertEquals(0, page.getTotalElements());
    }
}