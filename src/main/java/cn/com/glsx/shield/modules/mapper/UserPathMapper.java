package cn.com.glsx.shield.modules.mapper;

import cn.com.glsx.shield.modules.entity.UserPath;
import cn.com.glsx.shield.modules.model.view.DepartmentUserCount;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface UserPathMapper extends CommonBaseMapper<UserPath> {

    /**
     * 插入根节点路径
     *
     * @param userPath
     * @return
     */
    int insertRootPath(UserPath userPath);

    /**
     * 插入闭包路径
     *
     * @param userPath
     * @return
     */
    int insertUserPath(UserPath userPath);

    /**
     * 删除到上级闭包路径
     *
     * @param nodeId
     * @return
     */
    int deleteUserSuperiorPath(Long nodeId);

    /**
     * 删除到下级闭包路径
     *
     * @param nodeId
     * @return
     */
    int deleteUserSubPath(Long nodeId);

    /**
     * 删除全部闭包路径
     *
     * @param nodeId
     * @return
     */
    int deleteUserAllPath(Long nodeId);

    /**
     * 找到根路径
     *
     * @param tenantId
     * @return
     */
    List<UserPath> selectRootSubAllPaths(@Param("tenantId") Long tenantId);

    /**
     * 找到根路径
     *
     * @param subId
     * @return
     */
    UserPath selectRootPathBySubId(@Param("subId") Long subId);

    /**
     * 得到用户所有上级用户（含自己）
     *
     * @param subId
     * @return
     */
    List<UserPath> selectAllSuperiorBySubId(@Param("subId") Long subId);

    /**
     * 得到用户所有下级用户（含自己）
     *
     * @param superiorId
     * @return
     */
    List<UserPath> selectSubordinateBySuperiorId(@Param("superiorId") Long superiorId);

    /**
     * 统计用户下级各部门用户数
     *
     * @param superiorId
     * @return
     */
    //List<DepartmentUserCount> selectSubordinateDepartmentList(@Param("superiorId") Long superiorId);

    /**
     * 统计用户下级各部门用户数
     *
     * @param superiorId
     * @param userIds
     * @param orgIds
     * @param subOrgIds
     * @return
     */
    List<DepartmentUserCount> selectSubordinateDepartmentList(@Param("superiorId") Long superiorId,
                                                              @Param("userIds") Collection<Long> userIds,
                                                              @Param("orgIds") Collection<Long> orgIds,
                                                              @Param("subOrgIds") Collection<Long> subOrgIds);

}