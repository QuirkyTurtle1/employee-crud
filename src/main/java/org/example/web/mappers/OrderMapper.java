package org.example.web.mappers;

import org.example.web.dto.order.OrderRequest;
import org.example.web.dto.order.OrderResponse;
import org.example.web.dto.orderProduct.OrderProductResponse;
import org.example.web.model.Order;

import org.example.web.model.OrderProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.context.annotation.Primary;

@Primary
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "client.id", target = "clientId")
    @Mapping(target = "itemsTotal",
            expression = "java(entity.getItems() == null ? 0 : entity.getItems().stream().mapToInt(i -> i.getQuantity()).sum())")
    OrderResponse toResponse (Order entity);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "name",      source = "product.name")
    @Mapping(target = "price",     source = "product.price")
    OrderProductResponse toProductResponse (OrderProduct product);

}
