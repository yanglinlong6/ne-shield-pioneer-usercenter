package cn.com.glsx.shield.modules.mapper;

import cn.com.glsx.shield.modules.entity.ZhongliAccount;
import cn.com.glsx.shield.modules.model.zhongli.ZhongliAccountModel;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ZhongliAccountMapper extends CommonBaseMapper<ZhongliAccount> {

    List<ZhongliAccountModel> selectByLevel(@Param("level") String level);

}