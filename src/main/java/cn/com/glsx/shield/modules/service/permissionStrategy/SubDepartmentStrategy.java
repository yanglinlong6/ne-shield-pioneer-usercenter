package cn.com.glsx.shield.modules.service.permissionStrategy;

import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.shield.modules.entity.Department;
import cn.com.glsx.shield.modules.entity.Organization;
import cn.com.glsx.shield.modules.entity.User;
import cn.com.glsx.shield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.shield.modules.model.param.UserSearch;
import cn.com.glsx.shield.modules.model.view.DepartmentDTO;
import cn.com.glsx.shield.modules.service.DepartmentService;
import cn.com.glsx.usercenter.api.dto.tree.OrgModel;
import cn.com.glsx.usercenter.api.dto.tree.OrgTreeModel;
import com.glsx.plat.common.model.TreeModel;
import com.glsx.plat.common.utils.StringUtils;
import com.glsx.plat.common.utils.TreeModelUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 本部门及下属部门
 *
 * @author taoyr
 */
@Slf4j
@Component
public class SubDepartmentStrategy extends PermissionStrategy {

    @Resource
    private DepartmentService departmentService;

    public SubDepartmentStrategy() {
        super();
    }

    @Override
    public Set<Long> permissionDepartmentIds() {
        Long departmentId = ShieldContextHolder.getDepartmentId();

        List<Organization> subOrgList = organizationMapper.selectAllSubBySuperiorId(departmentId);

        return subOrgList.stream().map(Organization::getSubId).collect(Collectors.toSet());
    }

    @Override
    public List<Department> permissionDepartments() {

        Long departmentId = ShieldContextHolder.getDepartmentId();

        List<Organization> subOrgList = organizationMapper.selectAllSubBySuperiorId(departmentId);

        List<Long> subOrgIdList = subOrgList.stream().map(Organization::getSubId).collect(Collectors.toList());

        List<Department> list = departmentMapper.selectByIds(subOrgIdList);

        //log.info("用户{} {}部门数为{}", ShieldContextHolder.getAccount(), UserConstants.RolePermitCastType.subDepartment.getValue(), list.size());
        return list;
    }

    @Override
    public List<User> permissionUsers() {
        Set<Long> departmentIds = this.permissionDepartmentIds();
        return userMapper.selectByDepartmentIds(departmentIds);
    }

    /**
     * 获取一个组织的本身及下级组织的用户
     *
     * @param departmentId
     * @return
     */
    @Override
    public List<User> permissionUsersByDepartmentId(Long departmentId) {

        //假设本人所在组织链：R-A-B-C-本人组织-X-Y-Z

        //departmentId全部上级组织节点（R-A-B-C-本人组织）
        List<Organization> superiorOrgList = organizationMapper.selectAllSuperiorBySubId(ShieldContextHolder.getDepartmentId());

        List<Long> superiorOrgIdList = superiorOrgList.stream().map(Organization::getSuperiorId).collect(Collectors.toList());

        //departmentId全部下级组织节点（本人组织-X-Y-Z）
        List<Organization> subOrgList = organizationMapper.selectAllSubBySuperiorId(departmentId);

        List<Long> subOrgIdList = subOrgList.stream().map(Organization::getSubId).collect(Collectors.toList());

        //如果departmentId在上级组织链上(R-A-B-C-本人组织)，返回全部可见部门用户
        if (superiorOrgIdList.contains(departmentId)) {
            return this.permissionUsers();
        }
        //如果departmentId在下级组织链上(本人组织-X-Y-Z)，返回departmentId部门用户
        if (subOrgIdList.contains(departmentId)) {
            return userMapper.selectByDepartmentIds(subOrgIdList);
        }
        return Lists.newArrayList();
    }

    /**
     * 3 subDepartment
     * * 3.1 root找自己根部门
     * * 3.2 非root
     * * 先用rootId找到与自己部门的深度，看是上级还是下级（包括用户本部门）
     * * 3.2.1 上级
     * * 找深度-1的上级部门 从t_org得到department_id（单个）
     * * 3.2.2 下级（本部门）
     * * 找深度+1的下级部门 从t_org得到department_id列表
     * * subDepartment 封装（true，false）-设置userNum为用户部门和用户所有与下级部门userNum（用户部门或用户上级部门）
     * * -设置userNum为当前部门和所有下级部门的userNum（用户下级部门）
     *
     * @param rootId
     * @return
     */
    @Override
    public List<DepartmentDTO> orgSimpleList(Long rootId) {

        Long userDeptId = ShieldContextHolder.getDepartmentId();

        List<DepartmentDTO> departmentDTOList;

        List<Department> departmentParamList = Lists.newArrayList();

        //提取根节点
        if (rootId == null) {
            Organization root = organizationMapper.selectRootPathBySubId(userDeptId);
            rootId = root.getSuperiorId();
        }

        List<Organization> organizationList = organizationMapper.selectSubOrgList(Lists.newArrayList(rootId), 1);

        if (CollectionUtils.isNotEmpty(organizationList)) {
            List<Long> departmentIdList = organizationList.stream().map(Organization::getSubId).collect(Collectors.toList());

            List<Department> departmentList = departmentMapper.selectByIds(departmentIdList);

            departmentParamList.addAll(departmentList);
        }

        departmentDTOList = departmentService.getDepartmentAssembled(departmentParamList, true, true);

        return departmentDTOList;
    }

    @Override
    public List<? extends TreeModel> orgTree(OrgTreeSearch search) {

        Long tenantId = ShieldContextHolder.getTenantId();

        Long deptId = ShieldContextHolder.getDepartmentId();

        //设置租户范围
        search.setTenantId(tenantId);

        //查询组织到根组织的组织链 eg. A-B-C,由B得A-B
        Collection<Long> superiorOrgIdList = this.getSuperiorIds(Lists.newArrayList(deptId));

        //获取权限可见(下级)部门
        Set<Long> subDepartmentIdList = this.permissionDepartmentIds();

        Collection<Long> orgIds = Sets.newHashSet();

        //加入上级部门
        orgIds.addAll(superiorOrgIdList);

        //加入下级部门
        orgIds.addAll(subDepartmentIdList);

        //模糊搜索组织到根组织的(单个)组织链 eg. A-B-C
        if (StringUtils.isNotEmpty(search.getOrgName())) {
            Collection<Long> superiorIdSet = this.getSuperiorIds(search);
            if (CollectionUtils.isNotEmpty(superiorIdSet)) {
                //取交集=搜索到的+权限内的
                Set<Long> intersectionIdSet = orgIds.stream().filter(superiorIdSet::contains).collect(Collectors.toSet());

                orgIds = intersectionIdSet;
            }
        }

        //查询组织到根组织、最后一级的完整组织链 eg. A-B-C,由B得A-B-C
        if (CollectionUtils.isNotEmpty(search.getOrgIds())) {
            Collection<Long> orgIds1 = this.getSuperiorIds(search.getOrgIds());

            Collection<Long> orgIds2 = this.getSubIds(search.getOrgIds());

            orgIds.addAll(orgIds1);
            orgIds.addAll(orgIds2);
        }

        //查询组织到根组织的组织链 eg. A-B-C,由B得A-B
        if (CollectionUtils.isNotEmpty(search.getSupOrgIds())) {
            orgIds = this.getSuperiorIds(search.getSupOrgIds());
        }

        //查询组织到最后一级组织的组织链 eg. A-B-C,由B得B-C，这种会丢失根组织，只能以线性结构返回
        if (CollectionUtils.isNotEmpty(search.getSubOrgIds())) {
            Map<Long, Set<Long>> subOrgIdListMap = getSubIdsMap(search.getSubOrgIds());

            //选中的节点及下级节点
            Set<Long> selectSubOrgIds = Sets.newHashSet();

            for (Map.Entry<Long, Set<Long>> entry : subOrgIdListMap.entrySet()) {
                selectSubOrgIds.addAll(entry.getValue());
            }

            orgIds = selectSubOrgIds;
        }

        //查询用户u组织到根组织的完整上级链 eg. u得组织为C，A-B-C,由u得A-B-C
        if (CollectionUtils.isNotEmpty(search.getUserIds())) {
            orgIds = this.getUserOrgSuperiorIds(search.getUserIds());
        }

        //如果有查询条件，如果没有过滤到需要查询的组织链，返回空数据
        if ((StringUtils.isNotEmpty(search.getOrgName())
                || CollectionUtils.isNotEmpty(search.getOrgIds())
                || CollectionUtils.isNotEmpty(search.getSupOrgIds())
                || CollectionUtils.isNotEmpty(search.getSubOrgIds())
                || CollectionUtils.isNotEmpty(search.getUserIds())) && CollectionUtils.isEmpty(orgIds)) {
            return Lists.newArrayList();
        }

        search.setOrgIds(orgIds);

        List<OrgModel> modelList = organizationMapper.selectOrgList(search);

        List<Long> departmentIdList = modelList.stream().map(OrgModel::getOrgId).collect(Collectors.toList());
        //计算用户数
        Map<Long, Integer> recursiveDepartmentUserMap = this.calculateNumberOfOrgUsers(departmentIdList);

        List<OrgTreeModel> orgTreeModelList = modelList.stream().map(OrgTreeModel::new).collect(Collectors.toList());

        List<OrgTreeModel> allTreeModelList = Lists.newArrayList(orgTreeModelList);

        //查询需要的用户,然后放到对应的组织节点的child,如果需要返回用户子节点的话
        Map<Long, List<OrgModel>> groupByOrgIdUserMap = Maps.newHashMap();
        if (search.getHasUserLeaf() && CollectionUtils.isNotEmpty(departmentIdList)) {
            List<OrgModel> allUserList = userMapper.selectUserOrgModels(new UserSearch().setDepartmentIds(departmentIdList).setUserIds(search.getUserIds()));
            groupByOrgIdUserMap = allUserList.stream().collect(Collectors.groupingBy(OrgModel::getParentId));
        }

        Map<Long, List<OrgModel>> finalGroupByOrgIdUserMap = groupByOrgIdUserMap;
        orgTreeModelList.forEach(otm -> {
            Long orgId = Long.valueOf(otm.getId());
            //这里控制从用户自己部门开始才有用户
            if (orgId.equals(deptId) || !superiorOrgIdList.contains(orgId)) {
                Integer userNumber = recursiveDepartmentUserMap.get(orgId);
                otm.setUserNumber(userNumber == null ? 0 : userNumber);

                //用户子树
                if (search.getHasUserLeaf() && otm.getUserNumber() > 0) {
                    List<OrgModel> userList = finalGroupByOrgIdUserMap.getOrDefault(orgId, Lists.newArrayList());
                    List<OrgTreeModel> userTreeList = userList.stream().map(OrgTreeModel::new).collect(Collectors.toList());
                    allTreeModelList.addAll(userTreeList);
                }
            }
        });

        if (search.getReturnTrees()) {
            List<? extends TreeModel> orgTree = TreeModelUtil.fastConvertByRootMark(allTreeModelList, 1);
            return orgTree;
        }
        return allTreeModelList;
    }

    @Override
    public Map<Long, Integer> calculateNumberOfOrgUsers(Collection<Long> orgIds) {
        Set<Long> permissionOrgIds = this.permissionDepartmentIds();
        //本部门及下级部门
        List<Organization> allSubList = organizationMapper.selectSubOrgList(permissionOrgIds, null);

        return calculateNumberOfOrgUsers(orgIds, allSubList);
    }

}
