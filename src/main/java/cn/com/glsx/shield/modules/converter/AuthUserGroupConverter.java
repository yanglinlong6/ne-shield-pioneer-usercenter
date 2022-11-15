package cn.com.glsx.shield.modules.converter;

import cn.com.glsx.shield.modules.entity.UserGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthUserGroupConverter {

    AuthUserGroupConverter INSTANCE = Mappers.getMapper(AuthUserGroupConverter.class);

    @Mappings(@Mapping(source = "id", target = "groupId"))
    cn.com.glsx.auth.model.UserGroup toAuthUserGroup(UserGroup group);

}
