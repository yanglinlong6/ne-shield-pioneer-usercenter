package cn.com.glsx.shield.modules.mapper;

import cn.com.glsx.shield.modules.entity.Tenant;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface TenantMapper extends CommonBaseMapper<Tenant> {

    Tenant selectById(@Param("id") Long tenantId);

    List<Tenant> selectAllNotDel();

    List<Tenant> selectByIds(@Param("ids") Collection<Long> ids);

    int logicDeleteById(@Param("id") Long id);

}