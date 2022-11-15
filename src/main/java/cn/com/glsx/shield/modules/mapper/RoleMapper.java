package cn.com.glsx.shield.modules.mapper;

import cn.com.glsx.shield.modules.entity.Role;
import cn.com.glsx.shield.modules.model.param.RoleSearch;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface RoleMapper extends CommonBaseMapper<Role> {

    Role selectById(@Param("id") Long id);

    List<Role> selectList(RoleSearch search);

    List<Role> selectAssignTenantsList(RoleSearch search);

    List<Role> selectVisibilityList(RoleSearch search);

    /**
     * 根据roleIds获取角色列表
     *
     * @param ids
     * @return
     */
    List<Role> selectByIds(@Param("ids") Collection<Long> ids);

    /**
     * 根据用户id查询角色列表
     *
     * @return
     */
    List<Role> selectUserRoleList(@Param("userId") Long userId);

    /**
     * 逻辑删除
     *
     * @param id
     * @return
     */
    int logicDeleteById(Long id);

    int selectCntByName(@Param("roleName") String roleName);

    Role selectByName(@Param("roleName") String roleName);

    List<Role> selectByVisibilityType(@Param("roleVisibility") Integer roleVisibility);

    List<Long> selectRoleIdsByVisibilityType(@Param("roleVisibility") Integer roleVisibility);

    List<Role> selectByTenantIds(@Param("tenantIds") Collection<Long> tenantIds);

}