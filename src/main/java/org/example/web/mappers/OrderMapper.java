package org.example.web.mappers;

import org.example.web.dto.EmployeeRequest;
import org.example.web.dto.OrderRequest;
import org.example.web.dto.OrderResponse;
import org.example.web.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "client.id", target = "clientId")
    OrderResponse toResponse (Order entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(source = "clientId", target = "client.id")
    Order toEntity (OrderRequest req);
}
