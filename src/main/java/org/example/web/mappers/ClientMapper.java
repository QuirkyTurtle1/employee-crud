package org.example.web.mappers;

import org.example.web.dto.client.ClientRequest;
import org.example.web.dto.client.ClientResponse;
import org.example.web.model.Client;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.context.annotation.Primary;

import java.util.Map;
import java.util.UUID;

@Primary
@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "ordersCount",
            expression = "java(counts.getOrDefault(entity.getId(), 0L))")
    ClientResponse toResponse(Client entity, @Context Map<UUID, Long> counts);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Client toEntity (ClientRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity (ClientRequest req, @MappingTarget Client entity);
}
