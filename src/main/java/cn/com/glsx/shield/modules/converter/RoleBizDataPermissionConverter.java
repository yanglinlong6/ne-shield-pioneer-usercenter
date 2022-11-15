package cn.com.glsx.shield.modules.converter;

import cn.com.glsx.auth.model.BizDataPermission;
import cn.com.glsx.shield.modules.entity.RoleBizDataPermission;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RoleBizDataPermissionConverter {

    RoleBizDataPermissionConverter INSTANCE = Mappers.getMapper(RoleBizDataPermissionConverter.class);

    BizDataPermission bo2simple(RoleBizDataPermission item);

}
