package org.example.web.mappers;

import org.example.web.dto.product.ProductRequest;
import org.example.web.dto.product.ProductResponse;
import org.example.web.model.Product;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.context.annotation.Primary;



import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Primary
@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity (ProductRequest req);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateEntity (ProductRequest req, @MappingTarget Product entity);

    ProductResponse toResponse (Product entity);
}
