package cn.com.glsx.neshield.modules.mapper;

import cn.com.glsx.neshield.modules.entity.User;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends CommonBaseMapper<User> {

    User selectById(@Param("id") Long id);

}