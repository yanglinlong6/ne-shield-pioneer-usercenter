package cn.com.glsx.shield.modules.mapper;

import cn.com.glsx.shield.modules.entity.MenuPermission;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface MenuPermissionMapper extends CommonBaseMapper<MenuPermission> {

    List<MenuPermission> selectByMenuIds(@Param("menuIds") Collection<Long> menuIds);

}