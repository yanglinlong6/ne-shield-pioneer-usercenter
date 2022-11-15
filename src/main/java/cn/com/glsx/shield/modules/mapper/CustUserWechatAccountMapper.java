package cn.com.glsx.shield.modules.mapper;

import cn.com.glsx.shield.modules.entity.CustUserWechatAccount;
import cn.com.glsx.usercenter.api.dto.response.CUser;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface CustUserWechatAccountMapper extends CommonBaseMapper<CustUserWechatAccount> {

    List<CUser> selectByUserIds(@Param("cuserIds") Collection<Long> cuserIds);

}