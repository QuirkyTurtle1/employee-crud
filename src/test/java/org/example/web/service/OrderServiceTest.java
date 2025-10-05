package org.example.web.service;

import org.example.web.dto.order.OrderFilter;
import org.example.web.dto.order.OrderRequest;
import org.example.web.dto.order.OrderResponse;
import org.example.web.dto.orderProduct.OrderProductRequest;
import org.example.web.dto.orderProduct.OrderProductResponse;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepo;
    @Mock
    private ClientRepository clientRepo;
    @Mock
    private ProductRepository productRepo;
    @Mock
    private OrderProductRepository orderProductRepo;
    @Mock
    private OrderMapper mapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_whenValidRequest_savesOrderAndReturnsResponse() {
        // given
        UUID clientId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Client client = new Client();
        client.setId(clientId);

        Product product = new Product();
        product.setId(productId);
        product.setName("Book");

        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setProduct(product);
        orderProduct.setQuantity(2);

        Order order = Order.builder()
                .id(UUID.randomUUID())
                .client(client)
                .status(OrderStatus.NEW)
                .items(Set.of(orderProduct))
                .build();

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(order.getId())
                .clientId(clientId)
                .status(OrderStatus.NEW)
                .itemsTotal(2)
                .build();

        when(clientRepo.findById(clientId)).thenReturn(Optional.of(client));
        when(productRepo.findAllById(any())).thenReturn(List.of(product));
        when(orderRepo.save(any(Order.class))).thenReturn(order);
        when(orderRepo.findDetailedById(order.getId())).thenReturn(Optional.of(order));
        when(mapper.toResponse(order)).thenReturn(expectedResponse);

        OrderRequest request = new OrderRequest(
                clientId,
                OrderStatus.NEW,
                List.of(new OrderProductRequest(productId, 2))
        );

        // when
        OrderResponse response = orderService.create(request);

        // then
        assertEquals(expectedResponse. getId(), response.getId());
        assertEquals(expectedResponse.getClientId(), response.getClientId());
        assertEquals(expectedResponse.getItemsTotal(), response.getItemsTotal());
        verify(orderRepo).save(any(Order.class));
        verify(orderRepo).findDetailedById(order.getId());
        verify(mapper).toResponse(order);
    }

    @Test
    void createOrder_whenClientNotFound_throwsNotFoundException() {
        UUID fakeClientId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(clientRepo.findById(fakeClientId)).thenReturn(Optional.empty());

        OrderRequest request = new OrderRequest(
                fakeClientId,
                OrderStatus.NEW,
                List.of(new OrderProductRequest(productId, 1))
        );

        assertThrows(NotFoundException.class, () -> orderService.create(request));

        verify(productRepo, never()).findAllById(any());
    }

    @Test
    void createOrder_whenDuplicateProductInOrder_throwsDuplicateException() {
        UUID clientId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Client client = new Client();
        client.setId(clientId);

        when(clientRepo.findById(clientId)).thenReturn(Optional.of(client));

        OrderRequest request = new OrderRequest(
                clientId,
                OrderStatus.NEW,
                List.of(
                        new OrderProductRequest(productId, 1),
                        new OrderProductRequest(productId, 2)
                )
        );

        assertThrows(DuplicateProductInOrderException.class, () -> orderService.create(request));
    }

    @Test
    void updateStatus_whenOrderExists_updatesAndReturnsResponse() {
        // given
        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.NEW);

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .status(OrderStatus.COMPLETED)
                .build();

        when(orderRepo.findById(orderId)).thenReturn(Optional.of(order));

        when(orderRepo.findDetailedById(orderId)).thenReturn(Optional.of(order));
        when(mapper.toResponse(order)).thenReturn(expectedResponse);

        // when
        OrderResponse response = orderService.updateStatus(orderId, OrderStatus.COMPLETED);

        // then
        assertEquals(OrderStatus.COMPLETED, response.getStatus());
        verify(orderRepo).findById(orderId);
        verify(mapper).toResponse(order);
    }

    @Test
    void updateStatus_whenOrderNotFound_throwsNotFoundException() {
        UUID fakeOrderId = UUID.randomUUID();
        when(orderRepo.findById(fakeOrderId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> orderService.updateStatus(fakeOrderId, OrderStatus.COMPLETED));

        verify(orderRepo, never()).findDetailedById(any());
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void updateStatus_whenStatusIsNull_throwsIllegalArgumentException() {
        UUID orderId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateStatus(orderId, null));
    }

    @Test
    void updateStatus_whenStatusUnchanged_returnsSameResponseWithoutUpdate() {
        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.NEW);

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .status(OrderStatus.NEW)
                .build();

        when(orderRepo.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepo.findDetailedById(orderId)).thenReturn(Optional.of(order));
        when(mapper.toResponse(order)).thenReturn(expectedResponse);

        // when
        OrderResponse response = orderService.updateStatus(orderId, OrderStatus.NEW);

        // then
        assertEquals(OrderStatus.NEW, response.getStatus());
        verify(orderRepo, never()).save(any());
    }

    @Test
    void deleteOrder_whenOrderExists_deletesSuccessfully() {
        // given
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);

        when(orderRepo.findById(orderId)).thenReturn(Optional.of(order));

        // when
        orderService.delete(orderId);

        // then
        verify(orderRepo).delete(order);
    }

    @Test
    void deleteOrder_whenOrderNotFound_throwsNotFoundException() {
        // given
        UUID orderId = UUID.randomUUID();
        when(orderRepo.findById(orderId)).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> orderService.delete(orderId));

        verify(orderRepo, never()).delete(any(Order.class));
    }

    @Test
    void deleteOrder_whenIntegrityViolationOccurs_throwsDataIntegrityViolationException() {
        // given
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);

        when(orderRepo.findById(orderId)).thenReturn(Optional.of(order));
        doThrow(DataIntegrityViolationException.class)
                .when(orderRepo).delete(order);

        // then
        assertThrows(DataIntegrityViolationException.class, () -> orderService.delete(orderId));
    }

    @Test
    void getOne_whenOrderExists_returnsMappedResponse() {
        UUID orderId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Client client = new Client();
        client.setId(clientId);

        Product product = new Product();
        product.setId(productId);
        product.setName("Book");
        product.setPrice(BigDecimal.valueOf(19.99));

        Order order = Order.builder()
                .id(orderId)
                .client(client)
                .status(OrderStatus.NEW)
                .items(Set.of(
                        OrderProduct.builder()
                                .product(product)
                                .quantity(2)
                                .build()
                ))
                .build();

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .clientId(clientId)
                .status(OrderStatus.NEW)
                .items(List.of(
                        new OrderProductResponse(productId, "Book", 2, BigDecimal.valueOf(19.99))
                ))
                .itemsTotal(2)
                .build();

        when(orderRepo.findDetailedById(orderId)).thenReturn(Optional.of(order));
        when(mapper.toResponse(order)).thenReturn(expectedResponse);

        OrderResponse actualResponse = orderService.getOne(orderId);

        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getClientId(), actualResponse.getClientId());
        assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
        assertEquals(expectedResponse.getItemsTotal(), actualResponse.getItemsTotal());

        verify(orderRepo).findDetailedById(orderId);
        verify(mapper).toResponse(order);
    }

    @Test
    void getOne_whenOrderNotFound_throwsNotFoundException() {
        // given
        UUID missingId = UUID.randomUUID();
        when(orderRepo.findDetailedById(missingId)).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class,
                () -> orderService.getOne(missingId));

        verify(orderRepo).findDetailedById(missingId);
    }

    @Test
    void findAll_whenOrdersExist_returnsPageOfResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        OrderFilter filter = new OrderFilter(null, null, null, null);

        UUID clientId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Client client = new Client();
        client.setId(clientId);

        Product product = new Product();
        product.setId(productId);
        product.setName("Book");
        product.setPrice(BigDecimal.valueOf(19.99));

        Order order = Order.builder()
                .id(orderId)
                .client(client)
                .status(OrderStatus.NEW)
                .items(Set.of(
                        OrderProduct.builder()
                                .product(product)
                                .quantity(2)
                                .build()
                ))
                .build();

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .clientId(clientId)
                .status(OrderStatus.NEW)
                .items(List.of(new OrderProductResponse(productId, "Book", 2, BigDecimal.valueOf(19.99))))
                .itemsTotal(2)
                .build();

        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);

        when(orderRepo.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(orderRepo.findByIdIn(List.of(orderId))).thenReturn(List.of(order));
        when(mapper.toResponse(order)).thenReturn(expectedResponse);

        Page<OrderResponse> result = orderService.findAll(filter, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(orderId, result.getContent().get(0).getId());
        assertEquals(OrderStatus.NEW, result.getContent().get(0).getStatus());

        verify(orderRepo).findAll(any(Specification.class), eq(pageable));
        verify(orderRepo).findByIdIn(List.of(orderId));
        verify(mapper).toResponse(order);
    }

    @Test
    void findAll_whenNoOrders_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        OrderFilter filter = new OrderFilter(null, null, null, null);

        Page<Order> emptyPage = Page.empty(pageable);

        when(orderRepo.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        Page<OrderResponse> result = orderService.findAll(filter, pageable);

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(orderRepo).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findAll_whenRepositoryThrowsException_throwsRuntimeException() {

        OrderFilter filter = new OrderFilter(null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        when(orderRepo.findAll(any(Specification.class), eq(pageable)))
                .thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.findAll(filter, pageable));

        assertEquals("Database error", exception.getMessage());

        verify(orderRepo, never()).findByIdIn(anyList());
        verify(mapper, never()).toResponse(any());
    }

}