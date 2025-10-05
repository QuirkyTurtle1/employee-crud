package org.example.web.controller;

import org.example.web.dto.order.OrderRequest;
import org.example.web.dto.order.OrderResponse;
import org.example.web.dto.orderProduct.OrderProductRequest;
import org.example.web.fixtures.ClientFixture;
import org.example.web.fixtures.ProductFixture;
import org.example.web.model.Client;
import org.example.web.model.OrderStatus;
import org.example.web.model.Product;
import org.example.web.repository.ClientRepository;
import org.example.web.repository.OrderRepository;
import org.example.web.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;


import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    private UUID clientId;
    private UUID productId;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        clientRepository.deleteAll();
        productRepository.deleteAll();

        Client client = clientRepository.save(ClientFixture.readyClient());
        clientId = client.getId();

        Product product = productRepository.save(ProductFixture.readyProduct());
        productId = product.getId();
    }

    @Test
    void createOrder_whenValidRequest_returns201() throws Exception {
        // given
        OrderRequest request = new OrderRequest(
                clientId,
                OrderStatus.NEW,
                List.of(new OrderProductRequest(productId, 2))
        );

        // when + then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.clientId").value(clientId.toString()))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.itemsTotal").value(2))
                .andExpect(jsonPath("$.items[0].productId").value(productId.toString()))
                .andExpect(jsonPath("$.items[0].name").value("Book"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void createOrder_whenClientNotFound_returns404() throws Exception {
        UUID fakeClientId = UUID.randomUUID();
        OrderRequest request = new OrderRequest(
                fakeClientId,
                OrderStatus.NEW,
                List.of(new OrderProductRequest(productId, 2))
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message", containsString("Client")))
                .andExpect(jsonPath("$.path").value("/api/orders"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void createOrder_whenProductNotFound_returns404() throws Exception {
        UUID fakeProductId = UUID.randomUUID();
        OrderRequest request = new OrderRequest(
                clientId,
                OrderStatus.NEW,
                List.of(new OrderProductRequest(fakeProductId, 2))
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message", containsString("Product")))
                .andExpect(jsonPath("$.path").value("/api/orders"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void deleteProduct_whenProductInUse_returns409() throws Exception {

        OrderRequest request = new OrderRequest(
                clientId,
                OrderStatus.NEW,
                List.of(new OrderProductRequest(productId, 2))
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());


        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.code").value("PRODUCT_IN_USE"))
                .andExpect(jsonPath("$.message", containsString("Product")))
                .andExpect(jsonPath("$.path").value("/api/products/" + productId))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void deleteClient_whenClientInUse_returns409() throws Exception {

        OrderRequest request = new OrderRequest(
                clientId,
                OrderStatus.NEW,
                List.of(new OrderProductRequest(productId, 2))
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());


        mockMvc.perform(delete("/api/clients/{id}", clientId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message", containsString("Client")))
                .andExpect(jsonPath("$.path").value("/api/clients/" + clientId))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void getOrderById_whenOrderExists_returns200() throws Exception {
        // given
        OrderRequest request = new OrderRequest(
                clientId,
                OrderStatus.NEW,
                List.of(new OrderProductRequest(productId, 2))
        );
        // when
        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        OrderResponse createdOrder = objectMapper.readValue(responseJson, OrderResponse.class);
        // then
        mockMvc.perform(get("/api/orders/{id}", createdOrder.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdOrder.getId().toString()))
                .andExpect(jsonPath("$.clientId").value(clientId.toString()))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.itemsTotal").value(2))
                .andExpect(jsonPath("$.items[0].productId").value(productId.toString()))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void getOrderById_whenOrderNotFound_returns404() throws Exception {
        // given
        UUID fakeOrderId = UUID.randomUUID();

        // when
        // then
        mockMvc.perform(get("/api/orders/{id}", fakeOrderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message", containsString("Order")))
                .andExpect(jsonPath("$.path").value("/api/orders/" + fakeOrderId))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void updateOrderStatusById_whenUpdated_returns200() throws Exception {
        // given
        OrderRequest request = new OrderRequest(
                clientId,
                OrderStatus.NEW,
                List.of(new OrderProductRequest(productId, 2))
        );
        // when
        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        OrderResponse createdOrder = objectMapper.readValue(responseJson, OrderResponse.class);
        UUID orderId = createdOrder.getId();
        
        // then
        mockMvc.perform(patch("/api/orders/{id}/status?status=COMPLETED", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.clientId").value(clientId.toString()))
                .andExpect(jsonPath("$.itemsTotal").value(2));
    }
    @Test
    void updateOrderStatusById_whenOrderNotFound_returns404() throws Exception {
        UUID fakeOrderId = UUID.randomUUID();

        mockMvc.perform(patch("/api/orders/{id}/status?status=COMPLETED", fakeOrderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message", containsString("Order")))
                .andExpect(jsonPath("$.path").value("/api/orders/" + fakeOrderId + "/status"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void updateOrderStatusById_whenInvalidStatus_returns400() throws Exception {

        // given
        OrderRequest request = new OrderRequest(
                clientId,
                OrderStatus.NEW,
                List.of(new OrderProductRequest(productId, 2))
        );
        // when
        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        OrderResponse createdOrder = objectMapper.readValue(responseJson, OrderResponse.class);
        UUID orderId = createdOrder.getId();

        mockMvc.perform(patch("/api/orders/{id}/status?status=INVALID", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("Bad")))
                .andExpect(jsonPath("$.path").value("/api/orders/" + orderId + "/status"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }
    @Test
    void deleteOrder_whenExists_returns204() throws Exception {

        OrderRequest request = new OrderRequest(
                clientId,
                OrderStatus.NEW,
                List.of(new OrderProductRequest(productId, 2))
        );

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();


        String responseJson = result.getResponse().getContentAsString();
        OrderResponse createdOrder = objectMapper.readValue(responseJson, OrderResponse.class);
        UUID orderId = createdOrder.getId();


        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isNoContent());
    }
    @Test
    void deleteOrder_whenNotFound_returns404() throws Exception {
        UUID fakeOrderId = UUID.randomUUID();

        mockMvc.perform(delete("/api/orders/{id}", fakeOrderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message", containsString("Order")))
                .andExpect(jsonPath("$.path").value("/api/orders/" + fakeOrderId))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void getOrders_whenOrdersExist_returns200AndList() throws Exception {

        OrderRequest order1 = new OrderRequest(
                clientId,
                OrderStatus.NEW,
                List.of(new OrderProductRequest(productId, 2))
        );
        OrderRequest order2 = new OrderRequest(
                clientId,
                OrderStatus.NEW,
                List.of(new OrderProductRequest(productId, 1))
        );


        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].clientId").value(clientId.toString()))
                .andExpect(jsonPath("$.content[0].status").value("NEW"))
                .andExpect(jsonPath("$.content[0].items[0].productId").value(productId.toString()))
                .andExpect(jsonPath("$.content[0].items[0].quantity").exists())
                .andExpect(jsonPath("$.content[0].itemsTotal").exists())
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getOrders_whenNoOrders_returnsEmptyList() throws Exception {

        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.pageable").exists());
    }
}