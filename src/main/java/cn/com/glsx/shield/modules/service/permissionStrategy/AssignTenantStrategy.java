package cn.com.glsx.shield.modules.service.permissionStrategy;

import cn.com.glsx.admin.common.constant.Constants;
import cn.com.glsx.shield.modules.entity.Department;
import cn.com.glsx.shield.modules.entity.Organization;
import cn.com.glsx.shield.modules.entity.Tenant;
import cn.com.glsx.shield.modules.entity.User;
import cn.com.glsx.shield.modules.model.param.DepartmentSearch;
import cn.com.glsx.shield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.shield.modules.model.param.OrganizationSearch;
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
 * @author taoyr
 */
@Slf4j
@Component
public class AssignTenantStrategy extends PermissionStrategy {

    @Resource
    private DepartmentService departmentService;

    public AssignTenantStrategy() {
        super();
    }

    @Override
    public List<Long> permissionDepartmentIds() {
        List<Department> departmentList = this.permissionDepartments();
        return departmentList.stream().map(Department::getId).collect(Collectors.toList());
    }

    @Override
    public List<Department> permissionDepartments() {
        List<Tenant> tenantList = departmentService.getRolePermissionTenants();
        List<Long> tenantIdList = tenantList.stream().map(Tenant::getId).collect(Collectors.toList());
        return departmentMapper.selectByTenantIds(tenantIdList);
    }

    @Override
    public List<User> permissionUsers() {
        List<Department> departmentList = this.permissionDepartments();
        List<Long> departmentIdList = departmentList.stream().map(Department::getId).collect(Collectors.toList());
        return userMapper.selectByDepartmentIds(departmentIdList);
    }

    @Override
    public List<User> permissionUsersByDepartmentId(Long departmentId) {
        //权限可见租户
        List<Tenant> tenantList = departmentService.getRolePermissionTenants();

        List<Long> tenantIdList = tenantList.stream().map(Tenant::getId).collect(Collectors.toList());
        //departmentId全部下级组织节点
        List<Organization> subOrgList = organizationMapper.selectTenantsSubBySuperiorId(tenantIdList, departmentId);

        List<Long> subOrgIdList = subOrgList.stream().map(Organization::getSubId).collect(Collectors.toList());

        return userMapper.selectByDepartmentIds(subOrgIdList);
    }

    @Override
    public List<DepartmentDTO> orgSimpleList(Long rootId) {
        List<Department> departmentParamList;

        List<Tenant> tenantList = departmentService.getRolePermissionTenants();
        List<Long> tenantIdList = tenantList.stream().map(Tenant::getId).collect(Collectors.toList());

        if (rootId == null) {
            DepartmentSearch search = new DepartmentSearch();
            search.setIsRoot(Constants.IS_ROOT_DEPARTMENT);
            search.setTenantIds(tenantIdList);
            departmentParamList = departmentMapper.search(search);
        } else {
            OrganizationSearch search = new OrganizationSearch();
            search.setRootId(rootId);
            search.setTenantIds(tenantIdList);
            departmentParamList = organizationMapper.selectChildrenList(search);
        }
        return departmentService.getDepartmentAssembled(departmentParamList, true, true);
    }

    @Override
    public List<? extends TreeModel> orgTree(OrgTreeSearch search) {

        Collection<Long> orgIds = Sets.newHashSet();

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

        //查询用户组织到根组织的完整上级链 eg. u得组织为C，A-B-C,由u得A-B-C
        if (CollectionUtils.isNotEmpty(search.getUserIds())) {
            orgIds = this.getUserOrgSuperiorIds(search.getUserIds());
        }

        //指定租户数据权限
        List<Tenant> tenantList = departmentService.getRolePermissionTenants();

        Set<Long> tenantIds = tenantList.stream().map(Tenant::getId).collect(Collectors.toSet());

        //如果指定租户没有，返回空数据
        if (CollectionUtils.isEmpty(tenantIds)) {
            return Lists.newArrayList();
        }

        //如果有查询条件，如果没有过滤到需要查询的组织链，返回空数据
        if ((StringUtils.isNotEmpty(search.getOrgName())
                || CollectionUtils.isNotEmpty(search.getOrgIds())
                || CollectionUtils.isNotEmpty(search.getSupOrgIds())
                || CollectionUtils.isNotEmpty(search.getSubOrgIds())
                || CollectionUtils.isNotEmpty(search.getUserIds())) && CollectionUtils.isEmpty(orgIds)) {
            return Lists.newArrayList();
        }

        //指定数据权限租户
        search.setTenantIds(tenantIds);
        //过滤出的组织
        search.setOrgIds(orgIds);

        List<OrgModel> modelList = organizationMapper.selectOrgList(search);

        List<Long> departmentIdList = modelList.stream().map(OrgModel::getOrgId).collect(Collectors.toList());
        //计算用户数
        Map<Long, Integer> recursiveDepartmentUserMap = departmentService.countRecursiveDepartmentUser(departmentIdList);

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
        return null;
    }
}
