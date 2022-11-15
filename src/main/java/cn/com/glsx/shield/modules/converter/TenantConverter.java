package cn.com.glsx.shield.modules.converter;

import cn.com.glsx.shield.modules.entity.Tenant;
import cn.com.glsx.shield.modules.model.view.TenantDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TenantConverter {

    TenantConverter INSTANCE = Mappers.getMapper(TenantConverter.class);

    @Mappings({@Mapping(source = "id", target = "tenantId")})
    TenantDTO do2dto(Tenant tenant);

}
