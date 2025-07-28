package org.example.web.mappers;

import org.example.web.dto.ClientRequest;
import org.example.web.dto.ClientResponse;
import org.example.web.dto.EmployeeRequest;
import org.example.web.model.Client;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "ordersCount",
            expression = "java(entity.getOrders() == null ? 0 : entity.getOrders().size())")
    ClientResponse toResponse(Client entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Client toEntity (EmployeeRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity (ClientRequest req, @MappingTarget Client entity);
}
