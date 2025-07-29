package org.example.web.mappers;

import org.example.web.dto.employee.EmployeeRequest;
import org.example.web.dto.employee.EmployeeResponse;
import org.example.web.model.Employee;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.context.annotation.Primary;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Primary
@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "id", ignore = true)
    Employee toEntity(EmployeeRequest dto);

    EmployeeResponse toDto(Employee entity);

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateEntityFromDto(EmployeeRequest dto,
                             @MappingTarget Employee entity);

}
