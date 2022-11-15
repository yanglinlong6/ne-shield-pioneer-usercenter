package cn.com.glsx.shield.modules.service.permissionStrategy;

import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.shield.modules.entity.Department;
import cn.com.glsx.shield.modules.entity.Organization;
import cn.com.glsx.shield.modules.entity.User;
import cn.com.glsx.shield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.shield.modules.model.param.UserBO;
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
 * 本部门
 *
 * @author taoyr
 */
@Slf4j
@Component
public class SelfDepartmentStrategy extends PermissionStrategy {

    @Resource
    private DepartmentService departmentService;

    public SelfDepartmentStrategy() {
        super();
    }

    @Override
    public List<Long> permissionDepartmentIds() {
        return Lists.newArrayList(ShieldContextHolder.getDepartmentId());
    }

    @Override
    public List<Department> permissionDepartments() {
        Department department = departmentMapper.selectById(ShieldContextHolder.getDepartmentId());
        //log.info("用户{} {}部门为{}", ShieldContextHolder.getAccount(), UserConstants.RolePermitCastType.selfDepartment.getValue(), department.getDepartmentName());
        return Lists.newArrayList(department);
    }

    @Override
    public List<User> permissionUsers() {
        return userMapper.selectByDepartmentId(ShieldContextHolder.getDepartmentId());
    }

    @Override
    public List<User> permissionUsersByDepartmentId(Long departmentId) {
        //假设本人所在组织链：R-A-B-C-本人组织-X-Y-Z

        //departmentId全部上级组织节点（R-A-B-C-本人组织）
        List<Organization> superiorOrgList = organizationMapper.selectAllSuperiorBySubId(ShieldContextHolder.getDepartmentId());

        List<Long> superiorOrgIds = superiorOrgList.stream().map(Organization::getSuperiorId).collect(Collectors.toList());

        //只要departmentId在上级组织链上（R-A-B-C-本人组织），返回本部门全部用户
        if (superiorOrgIds.contains(departmentId)) {
            return this.permissionUsers();
        }
        return Lists.newArrayList();
    }

    /**
     * 2 self 或 selfDepartment
     * * 2.1 root 找自己根部门
     * * 2.2 非root
     * * 先用rootId找到与自己部门的深度，看是上级还是下级（包括用户本部门）
     * * 2.2.1 上级
     * * 找确定深度的上级部门 先用rootId找到与自己部门的深度，-1得到确定深度，从t_org得到department_id（单个）
     * * 2.2.2 下级（本部门）
     * * 返回空
     * * 封装（false，false）-设置userNum为1（self）或设置userNum为自己部门人数（selfDepartment）-如果是本部门设置hasChild为false，非本部门设置hasChild为true
     *
     * @param rootId
     * @return
     */
    @Override
    public List<DepartmentDTO> orgSimpleList(Long rootId) {

        Long userDeptId = ShieldContextHolder.getDepartmentId();

        Department department = departmentMapper.selectById(userDeptId);

        List<Department> departmentParamList = Lists.newArrayList(department);

        List<DepartmentDTO> departmentDTOList = departmentService.getDepartmentAssembled(departmentParamList, false, false);

        Integer userNumber = userMapper.countByCriterial(new UserBO().setDepartmentId(userDeptId));

        departmentDTOList.forEach(dep -> dep.setUserNumber(userNumber == null ? 0 : userNumber));

        departmentDTOList.forEach(dep -> {
            if (!dep.getId().equals(userDeptId)) {
                dep.setHasChildren(true);
            }
        });
        return departmentDTOList;
    }

    @Override
    public List<? extends TreeModel> orgTree(OrgTreeSearch search) {

        Long tenantId = ShieldContextHolder.getTenantId();

        Long deptId = ShieldContextHolder.getDepartmentId();

        search.setTenantId(tenantId);

        //查询组织到根组织的组织链 eg. A-B-C,由B得A-B
        Collection<Long> orgIds = this.getSuperiorIds(Lists.newArrayList(deptId));

        //模糊搜索组织到根组织的(多个)组织链 eg. A-B-C X-Y-Z
        if (StringUtils.isNotEmpty(search.getOrgName())) {
            orgIds = this.getSuperiorIds(search);
        }

        //查询组织到根组织、最后一级的完整组织链 eg. A-B-C,由B得A-B-C
        if (CollectionUtils.isNotEmpty(search.getOrgIds())) {
            Collection<Long> orgIds1 = this.getSuperiorIds(search.getOrgIds());

            Collection<Long> orgIds2 = this.getSubIds(search.getOrgIds());

            orgIds.addAll(orgIds1);
            orgIds.addAll(orgIds2);
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

        //查询用户组织到根组织的完整上级链 eg. u得组织为C，A-B-C,由u得A-B-C
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
        orgTreeModelList.stream().filter(otm -> Long.valueOf(otm.getId()).equals(deptId)).forEach(otm -> {
            Long orgId = Long.valueOf(otm.getId());
            Integer userNumber = recursiveDepartmentUserMap.get(orgId);
            otm.setUserNumber(userNumber == null ? 0 : userNumber);
            //用户子树
            if (search.getHasUserLeaf() && otm.getUserNumber() > 0) {
                List<OrgModel> userList = finalGroupByOrgIdUserMap.getOrDefault(orgId, Lists.newArrayList());
                List<OrgTreeModel> userTreeList = userList.stream().map(OrgTreeModel::new).collect(Collectors.toList());
                allTreeModelList.addAll(userTreeList);
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
        List<Long> permissionOrgIds = this.permissionDepartmentIds();
        //本部门
        List<Organization> allSubList = organizationMapper.selectSubOrgList(permissionOrgIds, null);

        return calculateNumberOfOrgUsers(orgIds, allSubList);
    }

}
