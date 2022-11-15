package cn.com.glsx.shield.modules.mapper;

import cn.com.glsx.shield.modules.entity.RoleBizDataPermission;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface RoleBizDataPermissionMapper extends CommonBaseMapper<RoleBizDataPermission> {

    List<RoleBizDataPermission> selectByRoleId(@Param("roleId") Long roleId);

    List<RoleBizDataPermission> selectByRoleIds(@Param("roleIds") Collection<Long> roleIds);

    int updateVisibilityByRoleIdAndType(@Param("roleId") Long roleId, @Param("type") Integer type, @Param("visibility") Integer visibility);

    int logicDeleteByRoleId(@Param("roleId") Long roleId);

    int logicDeleteByRoleIdAndType(@Param("roleId") Long roleId, @Param("type") Integer type);

}