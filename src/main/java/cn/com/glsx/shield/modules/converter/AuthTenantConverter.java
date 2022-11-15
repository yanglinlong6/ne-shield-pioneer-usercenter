package cn.com.glsx.shield.modules.converter;

import cn.com.glsx.shield.modules.entity.Tenant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthTenantConverter {

    AuthTenantConverter INSTANCE = Mappers.getMapper(AuthTenantConverter.class);

    @Mappings(@Mapping(source = "id", target = "tenantId"))
    cn.com.glsx.auth.model.Tenant toAuthTenant(Tenant menu);

}
