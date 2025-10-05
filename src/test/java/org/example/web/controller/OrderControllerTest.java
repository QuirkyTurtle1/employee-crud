package org.example.web.controller;

import org.example.web.dto.order.OrderFilter;
import org.example.web.dto.order.OrderRequest;
import org.example.web.dto.order.OrderResponse;

import org.example.web.dto.orderProduct.OrderProductRequest;
import org.example.web.dto.orderProduct.OrderProductResponse;
import org.example.web.exception.DuplicateProductInOrderException;
import org.example.web.exception.NotFoundException;
import org.example.web.model.OrderStatus;
import org.example.web.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrder_whenValidRequest_returns201() throws Exception {
        //given
        UUID clientId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        OrderRequest request = new OrderRequest(clientId, OrderStatus.NEW, List.of(new OrderProductRequest(productId, 1)));
        OrderResponse response = OrderResponse.builder()
                .id(orderId)
                .clientId(clientId)
                .status(OrderStatus.NEW)
                .items(List.of(new OrderProductResponse(productId, "Bread", 1, BigDecimal.valueOf(19.99))))
                .itemsTotal(1)
                .build();

        //when
        when(orderService.create(any(OrderRequest.class))).thenReturn(response);

        //then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.clientId").value(clientId.toString()))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.itemsTotal").value(1))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void createOrder_whenClientNotFound_returns404() throws Exception {
        //given
        UUID missingClientId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        OrderRequest request = new OrderRequest(missingClientId, OrderStatus.NEW, List.of(new OrderProductRequest(productId, 1)));
        //when
        when(orderService.create(any())).thenThrow(new NotFoundException("Client", missingClientId));
        //then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(containsString("Client")))
                .andExpect(jsonPath("$.path").value("/api/orders"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }

    @ParameterizedTest
    @MethodSource("invalidOrderRequests")
    void createOrder_whenInvalidRequest_returns400(OrderRequest invalidRequest, String expectedMessage) throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message", containsString(expectedMessage)))
                .andExpect(jsonPath("$.path").value("/api/orders"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
        ;

    }

    static Stream<Arguments> invalidOrderRequests() {
        UUID clientId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        return Stream.of(
                Arguments.of(
                        new OrderRequest(clientId, OrderStatus.NEW, List.of(new OrderProductRequest(productId, 0))),
                        "quantity"
                ),
                Arguments.of(
                        new OrderRequest(null, OrderStatus.NEW, List.of(new OrderProductRequest(productId, 1))),
                        "clientId"
                ),
                Arguments.of(
                        new OrderRequest(clientId, OrderStatus.NEW, List.of()),
                        "products"
                )
        );
    }

    @Test
    void createOrder_whenDuplicateProduct_returns409() throws Exception {
        UUID clientId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderRequest request = new OrderRequest(
                clientId,
                OrderStatus.NEW,
                List.of(new OrderProductRequest(productId, 1), new OrderProductRequest(productId, 2))
        );


        when(orderService.create(any(OrderRequest.class)))
                .thenThrow(new DuplicateProductInOrderException(productId));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message", containsString("Duplicate product")))
                .andExpect(jsonPath("$.path").value("/api/orders"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void getOrderById_whenOrderExists_returns200() throws Exception {
        //given
        UUID clientId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderResponse response = OrderResponse.builder()
                .id(orderId)
                .clientId(clientId)
                .status(OrderStatus.NEW)
                .items(List.of(new OrderProductResponse(productId, "Bread", 1, BigDecimal.valueOf(19.99))))
                .itemsTotal(1)
                .build();

        //when
        when(orderService.getOne(orderId)).thenReturn(response);

        //then
        mockMvc.perform(get("/api/orders/{id}", orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.clientId").value(clientId.toString()))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.itemsTotal").value(1))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

    }

    @Test
    void getOrderById_whenOrderNotFound_returns404() throws Exception {
        //given
        UUID missingOrderId = UUID.randomUUID();
        //when
        when(orderService.getOne(missingOrderId)).thenThrow(new NotFoundException("Order", missingOrderId));
        //then
        mockMvc.perform(get("/api/orders/{id}", missingOrderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(containsString("Order")))
                .andExpect(jsonPath("$.path").value("/api/orders/" + missingOrderId))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void updateOrderStatusById_whenUpdated_returns200() throws Exception {
        //given
        UUID clientId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();


        OrderResponse response = OrderResponse.builder()
                .id(orderId)
                .clientId(clientId)
                .status(OrderStatus.COMPLETED)
                .items(List.of(new OrderProductResponse(productId, "Bread", 1, BigDecimal.valueOf(19.99))))
                .itemsTotal(1)
                .build();

        //when
        when(orderService.updateStatus(orderId,OrderStatus.COMPLETED )).thenReturn(response);

        //then
        mockMvc.perform(patch("/api/orders/{id}/status?status=COMPLETED", orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.clientId").value(clientId.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.itemsTotal").value(1))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

    }
    @Test
    void updateOrderStatusById_whenInvalidStatus_returns400() throws Exception {
        UUID orderId = UUID.randomUUID();

        mockMvc.perform(patch("/api/orders/{id}/status", orderId)
                        .param("status", "INVALID")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("Bad request")))
                .andExpect(jsonPath("$.path").value("/api/orders/" + orderId + "/status"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }
    @Test
    void updateOrderStatus_whenOrderNotFound_returns404() throws Exception {
        UUID missingOrderId = UUID.randomUUID();

        when(orderService.updateStatus(missingOrderId, OrderStatus.COMPLETED))
                .thenThrow(new NotFoundException("Order", missingOrderId));

        mockMvc.perform(patch("/api/orders/{id}/status", missingOrderId)
                        .param("status", "COMPLETED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(containsString("Order")))
                .andExpect(jsonPath("$.path").value("/api/orders/" + missingOrderId + "/status"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void deleteOrder_whenExists_returns204() throws Exception {
        UUID orderId = UUID.randomUUID();

        doNothing().when(orderService).delete(orderId);

        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteOrder_whenNotFound_returns404() throws Exception {
        UUID missingOrderId = UUID.randomUUID();

        doThrow(new NotFoundException("Order", missingOrderId))
                .when(orderService).delete(missingOrderId);

        mockMvc.perform(delete("/api/orders/{id}", missingOrderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message", containsString("Order")))
                .andExpect(jsonPath("$.path").value("/api/orders/" + missingOrderId))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }
    @Test
    void deleteOrder_whenInvalidUUID_returns400() throws Exception {
        mockMvc.perform(delete("/api/orders/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("Bad request")))
                .andExpect(jsonPath("$.path").value("/api/orders/invalid-uuid"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void getOrders_whenValidRequest_returns200() throws Exception {
        // given
        UUID clientId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        OrderResponse orderResponse = OrderResponse.builder()
                .id(orderId)
                .clientId(clientId)
                .status(OrderStatus.NEW)
                .items(List.of())
                .itemsTotal(0)
                .build();

        Page<OrderResponse> page = new PageImpl<>(
                List.of(orderResponse),
                PageRequest.of(0, 10, Sort.by("createdAt").descending()),
                1
        );

        when(orderService.findAll(any(OrderFilter.class), any(Pageable.class)))
                .thenReturn(page);

        // when + then
        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(orderId.toString()))
                .andExpect(jsonPath("$.content[0].clientId").value(clientId.toString()))
                .andExpect(jsonPath("$.content[0].status").value("NEW"))
                .andExpect(jsonPath("$.content[0].itemsTotal").value(0))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }
    @Test
    void getOrders_whenNoOrders_returnsEmptyPage() throws Exception {
        Page<OrderResponse> emptyPage = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 10),
                0
        );

        when(orderService.findAll(any(OrderFilter.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.number").value(0));
    }

}