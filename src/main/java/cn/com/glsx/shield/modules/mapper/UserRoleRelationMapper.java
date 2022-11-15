package cn.com.glsx.shield.modules.mapper;

import cn.com.glsx.shield.modules.entity.UserRoleRelation;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleRelationMapper extends CommonBaseMapper<UserRoleRelation> {

    /**
     * 获取用户角色列表
     *
     * @param relation
     * @return
     */
    List<UserRoleRelation> selectUserRoleRelationList(UserRoleRelation relation);

    List<UserRoleRelation> selectByUserId(@Param("userId") Long userId);

    List<UserRoleRelation> selectByRoleId(@Param("roleId") Long roleId);

}