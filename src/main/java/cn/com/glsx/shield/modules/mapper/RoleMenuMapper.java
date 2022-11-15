package cn.com.glsx.shield.modules.mapper;

import cn.com.glsx.shield.modules.entity.RoleMenu;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface RoleMenuMapper extends CommonBaseMapper<RoleMenu> {

    List<RoleMenu> selectByRoleId(@Param("roleId") Long roleId);

    List<RoleMenu> selectByMenuId(@Param("menuId") Long menuId);

    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    List<Long> selectMenuIdsByRoleIds(@Param("roleIds") Collection<Long> roleIds);

    List<Long> selectRoleIdsByMenuId(@Param("menuId") Long menuId);

    /**
     * 逻辑删除
     *
     * @param roleId
     * @return
     */
    int logicDelByRoleId(Long roleId);

    /**
     * 物理删除
     *
     * @param menuNo
     * @return
     */
    int deleteByMenuNo(Long menuNo);

}