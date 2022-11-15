package cn.com.glsx.shield.modules.mapper;

import cn.com.glsx.shield.modules.entity.RoleDataPermission;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleDataPermissionMapper extends CommonBaseMapper<RoleDataPermission> {

    List<RoleDataPermission> selectByRoleId(@Param("roleId") Long roleId);

    List<Long> selectRefIdsByRoleId(@Param("roleId") Long roleId);

    int logicDeleteByRoleId(@Param("roleId") Long roleId);

}