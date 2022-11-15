package cn.com.glsx.shield.modules.mapper;

import cn.com.glsx.shield.modules.entity.RoleTenant;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface RoleTenantMapper extends CommonBaseMapper<RoleTenant> {

    List<RoleTenant> selectByRoleId(@Param("roleId") Long roleId);

    List<Long> selectTenantIdsByRoleId(@Param("roleId") Long roleId);

    List<Long> selectRoleIdsByTenantId(@Param("tenantId") Long tenantId);

    List<Long> selectRoleIdsByTenantIds(@Param("tenantIds") Collection<Long> tenantIds);

    List<RoleTenant> selectByTenantId(@Param("tenantId") Long tenantId);

    int logicDeleteByRoleId(@Param("roleId") Long roleId);

}