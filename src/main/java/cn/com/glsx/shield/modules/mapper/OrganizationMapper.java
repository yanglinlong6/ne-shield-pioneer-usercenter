package cn.com.glsx.shield.modules.mapper;

import cn.com.glsx.shield.modules.entity.Department;
import cn.com.glsx.shield.modules.entity.Organization;
import cn.com.glsx.shield.modules.model.OrgSuperiorModel;
import cn.com.glsx.shield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.shield.modules.model.param.OrganizationBO;
import cn.com.glsx.shield.modules.model.param.OrganizationSearch;
import cn.com.glsx.usercenter.api.dto.tree.OrgModel;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface OrganizationMapper extends CommonBaseMapper<Organization> {

    /**
     * 插入根节点路径
     *
     * @param organization
     * @return
     */
    int insertRootPath(Organization organization);

    /**
     * 插入闭包路径
     *
     * @param organization
     * @return
     */
    int insertOrgPath(Organization organization);

    /**
     * 删除闭包路径
     *
     * @param nodeId
     * @return
     */
    int deleteOrgAllPath(@Param("nodeId") Long nodeId);

    /**
     * 获取父级id（含自己）
     *
     * @param search
     * @return
     */
    List<OrgSuperiorModel> selectSuperiorIdsByOrg(OrgTreeSearch search);

    /**
     * 获取多个组织上级
     *
     * @param orgId
     * @return
     */
    List<OrgSuperiorModel> selectSuperiorIdsByOrgIds(@Param("orgIds") Collection<Long> orgId);

    /**
     * 获取组织机构列表，含父级id
     *
     * @param search
     * @return
     */
    List<OrgModel> selectOrgList(OrgTreeSearch search);

    /**
     * 得到上级节点,depth为层级
     *
     * @param superiorId
     * @param depth
     * @return
     */
    Organization selectSuperiorOrgByDepth(@Param("superiorId") Long superiorId, @Param("depth") Integer depth);

    /**
     * 得到特定层级的下级
     *
     * @param superiorId
     * @param depth
     * @return
     */
    List<Organization> selectSuperiorOrgsByDepths(@Param("superiorId") Long superiorId, @Param("depths") Collection<Integer> depth);

    /**
     * 查找确定深度的子节点列表
     *
     * @param superiorIds
     * @param depth
     * @return
     */
    List<Organization> selectSubOrgList(@Param("superiorIds") Collection<Long> superiorIds, @Param("depth") Integer depth);

    /**
     * 得到所有上级（包括自己）
     *
     * @param subId
     * @return
     */
    List<Organization> selectAllSuperiorBySubId(@Param("subId") Long subId);

    /**
     * 得到所有子节点（包括自己）
     *
     * @param superiorId
     * @return
     */
    List<Organization> selectAllSubBySuperiorId(@Param("superiorId") Long superiorId);

    /**
     * 得到所有子节点（包括自己）
     *
     * @param tenantIds
     * @param superiorId
     * @return
     */
    List<Organization> selectTenantsSubBySuperiorId(@Param("tenantIds") Collection<Long> tenantIds, @Param("superiorId") Long superiorId);

    /**
     * 找到根节点list
     *
     * @param subIds
     * @return
     */
    List<Organization> selectRootIdList(@Param("subIds") Collection<Long> subIds);

    /**
     * 找到根（包含根）下所有路径
     *
     * @param tenantId
     * @return
     */
    List<Organization> selectRootSubAllPaths(@Param("tenantId") Long tenantId);

    /**
     * 找到根路径
     *
     * @param subId
     * @return
     */
    Organization selectRootPathBySubId(@Param("subId") Long subId);

    /**
     * 查找子节点列表
     *
     * @param organizationSearch
     * @return
     */
    List<Department> selectChildrenList(OrganizationSearch organizationSearch);


    /**
     * 根据条件筛选符合的路径
     *
     * @param organizationBO
     * @return
     */
    List<Organization> selectList(OrganizationBO organizationBO);

}