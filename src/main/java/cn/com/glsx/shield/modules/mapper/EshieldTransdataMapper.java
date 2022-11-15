package cn.com.glsx.shield.modules.mapper;

import cn.com.glsx.shield.modules.entity.EshieldTransdata;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EshieldTransdataMapper extends CommonBaseMapper<EshieldTransdata> {
    List<EshieldTransdata> selectByUserId(Long userid);

    EshieldTransdata selectByClassId(Integer classId);

    List<EshieldTransdata> selectByShopType(String shopType);
}