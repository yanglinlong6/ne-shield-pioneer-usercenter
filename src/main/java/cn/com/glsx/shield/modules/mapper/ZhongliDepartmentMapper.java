package cn.com.glsx.shield.modules.mapper;

import cn.com.glsx.shield.modules.entity.ZhongliDepartment;
import cn.com.glsx.shield.modules.model.zhongli.ZhongliDepartmentModel;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ZhongliDepartmentMapper extends CommonBaseMapper<ZhongliDepartment> {

    List<ZhongliDepartmentModel> selectAllLevel();

}