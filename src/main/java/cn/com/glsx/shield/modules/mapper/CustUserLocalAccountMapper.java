package cn.com.glsx.shield.modules.mapper;

import cn.com.glsx.shield.modules.entity.CustUserLocalAccount;
import cn.com.glsx.usercenter.api.dto.response.CUser;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface CustUserLocalAccountMapper extends CommonBaseMapper<CustUserLocalAccount> {

    List<CUser> selectByUserIds(@Param("cuserIds") Collection<Long> cuserIds);

    List<CUser> selectByRelUserIds(@Param("relUserIds") Collection<Long> relUserIds);

}