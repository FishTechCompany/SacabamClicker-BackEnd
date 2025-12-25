package org.sacabam.sacabamclickerbe.mapper.auth;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.sacabam.sacabamclickerbe.dto.response.auth.RegisterResponse;
import org.sacabam.sacabamclickerbe.entity.User;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "userId", source = "id")
    RegisterResponse toRegisterResponse(User user);
}