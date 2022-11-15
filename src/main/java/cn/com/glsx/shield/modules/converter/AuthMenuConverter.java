package cn.com.glsx.shield.modules.converter;

import cn.com.glsx.shield.modules.entity.Menu;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthMenuConverter {

    AuthMenuConverter INSTANCE = Mappers.getMapper(AuthMenuConverter.class);

    @Mappings(@Mapping(source = "id", target = "menuId"))
    cn.com.glsx.auth.model.Menu toAuthMentu(Menu menu);

}
