package org.example.web.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.example.web.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Testcontainers
@EnableJpaRepositories(basePackages = "org.example.web.repository")
class OrderRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Test
    void findDetailedById_whenOrderExists_returnsOrderWithClientAndItems() {
        // given
        Client client = clientRepository.save(Client.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+79001234567")
                .build());

        Product book = productRepository.save(Product.builder()
                .name("Book")
                .description("Novel")
                .price(new BigDecimal("10.00"))
                .build());

        Order order = orderRepository.save(Order.builder()
                .client(client)
                .status(OrderStatus.NEW)
                .build());

        OrderProduct orderProduct = orderProductRepository.save(OrderProduct.builder()
                .order(order)
                .product(book)
                .quantity(2)
                .build());

        order.getItems().add(orderProduct);
        orderRepository.save(order);

        // when
        Optional<Order> found = orderRepository.findDetailedById(order.getId());

        // then
        assertTrue(found.isPresent());
        Order result = found.get();
        assertNotNull(result.getClient());
        assertEquals(client.getId(), result.getClient().getId());
        assertFalse(result.getItems().isEmpty());
        assertEquals("Book", result.getItems().iterator().next().getProduct().getName());
    }

    @Test
    void existsByClientId_whenClientHasOrders_returnsTrue() {
        // given
        Client client = clientRepository.save(Client.builder()
                .firstName("Anna")
                .lastName("Smith")
                .email("anna@example.com")
                .phone("+79005554433")
                .build());

        orderRepository.save(Order.builder()
                .client(client)
                .status(OrderStatus.NEW)
                .build());

        // when
        boolean exists = orderRepository.existsByClientId(client.getId());

        // then
        assertTrue(exists);
    }

    @Test
    void findByIdIn_whenOrdersExist_returnsCorrectOrders() {
        // given
        Client client = clientRepository.save(Client.builder()
                .firstName("Alex")
                .lastName("Ivanov")
                .email("alex@example.com")
                .phone("+79006667788")
                .build());

        Order o1 = orderRepository.save(Order.builder().client(client).status(OrderStatus.NEW).build());
        Order o2 = orderRepository.save(Order.builder().client(client).status(OrderStatus.PROCESSING).build());

        // when
        List<Order> found = orderRepository.findByIdIn(List.of(o1.getId(), o2.getId()));

        // then
        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(o -> o.getId().equals(o1.getId())));
        assertTrue(found.stream().anyMatch(o -> o.getId().equals(o2.getId())));
    }

    @Test
    void findByClientId_whenOrdersPaged_returnsCorrectPage() {
        // given
        Client client = clientRepository.save(Client.builder()
                .firstName("Dmitry")
                .lastName("Petrov")
                .email("dmitry@example.com")
                .phone("+79007778899")
                .build());

        for (int i = 0; i < 5; i++) {
            orderRepository.save(Order.builder()
                    .client(client)
                    .status(OrderStatus.NEW)
                    .build());
        }

        // when
        Page<Order> page = orderRepository.findByClientId(client.getId(), PageRequest.of(0, 3));

        // then
        assertEquals(3, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }
}