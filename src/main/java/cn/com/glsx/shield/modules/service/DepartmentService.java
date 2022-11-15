package cn.com.glsx.shield.modules.service;

import cn.com.glsx.admin.common.constant.Constants;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.shield.modules.entity.*;
import cn.com.glsx.shield.modules.mapper.*;
import cn.com.glsx.shield.modules.model.OrgSuperiorModel;
import cn.com.glsx.shield.modules.model.UserSuperiorOrgModel;
import cn.com.glsx.shield.modules.model.param.DepartmentSearch;
import cn.com.glsx.shield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.shield.modules.model.param.OrganizationSearch;
import cn.com.glsx.shield.modules.model.view.DepartmentCount;
import cn.com.glsx.shield.modules.model.view.DepartmentDTO;
import cn.com.glsx.usercenter.api.dto.request.FindOrgRequest;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.com.glsx.admin.common.constant.UserConstants.RolePermitCastType.*;

/**
 * @author: taoyr
 **/
@Service
public class DepartmentService {

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private OrganizationMapper organizationMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleTenantMapper roleTenantMapper;

    @Resource
    private RoleDataPermissionMapper roleDataPermissionMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserPathMapper userPathMapper;

    @Resource
    private TenantMapper tenantMapper;

    public List<Department> findByOrg(FindOrgRequest request) {
        return departmentMapper.selectByOrg(request);
    }

    /**
     * 获取当前用户权限所拥有的部门
     *
     * @return
     */
    public List<Long> getCurrentUserDepartmentIds() {
        Long roleId = ShieldContextHolder.getRoleId();
        Long userId = ShieldContextHolder.getUserId();
        return getRoleDepartment(userId, roleId);
    }

    public List<Long> getRoleDepartment(Long userId, Long roleId) {
        List<Department> departmentList = getUserRoleDepartments(userId, roleId);

        List<Long> departmentIdList = departmentList.stream().map(Department::getId).collect(Collectors.toList());

        return departmentIdList;
    }

    public List<Tenant> getUserRoleTenants(Long userId, Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            return Lists.newArrayList();
        }

        User user = userMapper.selectById(userId);
        if (oneself.getCode().equals(role.getRolePermissionType())) {
            Tenant tenant = tenantMapper.selectById(user.getTenantId());
            //自己 无租户权限
            return Lists.newArrayList(tenant);
        } else if (all.getCode().equals(role.getRolePermissionType())) {
            //全部租户
            return tenantMapper.selectAllNotDel();
        } else if (selfDepartment.getCode().equals(role.getRolePermissionType())) {
            //自己部门
            Tenant tenant = tenantMapper.selectById(user.getTenantId());
            return Lists.newArrayList(tenant);
        } else if (subDepartment.getCode().equals(role.getRolePermissionType())) {
            //自己部门+下级部门
            Tenant tenant = tenantMapper.selectById(user.getTenantId());
            return Lists.newArrayList(tenant);
        } else if (subordinate.getCode().equals(role.getRolePermissionType())) {
            //本人及下级 无租户权限
            Tenant tenant = tenantMapper.selectById(user.getTenantId());
            return Lists.newArrayList(tenant);
        } else if (assignTenants.getCode().equals(role.getRolePermissionType())) {
            List<RoleDataPermission> permissionTenants = roleDataPermissionMapper.selectByRoleId(roleId);
            List<Long> tenantIdList = permissionTenants.stream().map(RoleDataPermission::getRefId).collect(Collectors.toList());
            return tenantMapper.selectByIds(tenantIdList);
        }
        return Lists.newArrayList();
    }

    /**
     * 获取角色数据权限内的部门
     *
     * @param userId
     * @param roleId
     * @return
     */
    public List<Department> getUserRoleDepartments(Long userId, Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            return Lists.newArrayList();
        }

        if (oneself.getCode().equals(role.getRolePermissionType())) {
            //自己 不能看部门
            return Lists.newArrayList();
        } else if (all.getCode().equals(role.getRolePermissionType())) {
            //全部部门
            return departmentMapper.selectAllNotDel();
        } else if (selfDepartment.getCode().equals(role.getRolePermissionType())) {
            //自己部门
            User user = userMapper.selectById(userId);
            Department department = departmentMapper.selectById(user.getDepartmentId());
            return Lists.newArrayList(department);
        } else if (subDepartment.getCode().equals(role.getRolePermissionType())) {
            //自己部门+下级部门
            User user = userMapper.selectById(userId);

            List<Organization> subOrgList = organizationMapper.selectAllSubBySuperiorId(user.getDepartmentId());

            List<Long> departmentIdList = subOrgList.stream().map(Organization::getSubId).collect(Collectors.toList());

            return departmentMapper.selectByIds(departmentIdList);
        } else if (subordinate.getCode().equals(role.getRolePermissionType())) {
            //本人及下级 不能看部门
//            List<DepartmentUserCount> departmentUserCountList = userPathMapper.selectSubordinateDepartmentList(userId);
//            departmentList = departmentUserCountList.stream().map(DepartmentUserCount::getDepartmentId).collect(Collectors.toList());
            return Lists.newArrayList();
        } else if (assignTenants.getCode().equals(role.getRolePermissionType())) {
            List<RoleDataPermission> permissionTenants = roleDataPermissionMapper.selectByRoleId(roleId);
            List<Long> tenantIdList = permissionTenants.stream().map(RoleDataPermission::getRefId).collect(Collectors.toList());
            return departmentMapper.selectByTenantIds(tenantIdList);
        }
        return Lists.newArrayList();
    }

    /**
     * 获取角色数据权限内的用户
     *
     * @param userId
     * @param roleId
     * @return
     */
    public List<User> getUserRoleUsers(Long userId, Long roleId) {
        List<User> userList = Lists.newArrayList();

        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            return userList;
        }

        //List<Department> departmentList = this.getUserRoleDepartments(userId,roleId);

        if (oneself.getCode().equals(role.getRolePermissionType())) {
            //自己
            User user = userMapper.selectById(userId);
            userList.add(user);
            return userList;
        } else if (all.getCode().equals(role.getRolePermissionType())) {
            //全部用户
            return userMapper.selectAllNotDel();
        } else if (selfDepartment.getCode().equals(role.getRolePermissionType())) {
            //自己部门用户
            User user = userMapper.selectById(userId);
            return userMapper.selectByDepartmentId(user.getDepartmentId());
        } else if (subDepartment.getCode().equals(role.getRolePermissionType())) {
            //自己+下级部门用户
            User user = userMapper.selectById(userId);

            List<Organization> subOrgList = organizationMapper.selectAllSubBySuperiorId(user.getDepartmentId());

            List<Long> departmentIdList = subOrgList.stream().map(Organization::getSubId).collect(Collectors.toList());

            return userMapper.selectByDepartmentIds(departmentIdList);
        } else if (subordinate.getCode().equals(role.getRolePermissionType())) {
            //自己+下级用户
            List<UserPath> userPathList = userPathMapper.selectSubordinateBySuperiorId(userId);

            List<Long> subUserIdList = userPathList.stream().map(UserPath::getSubId).collect(Collectors.toList());

            return userMapper.selectByIds(subUserIdList);
        } else if (assignTenants.getCode().equals(role.getRolePermissionType())) {
            List<RoleDataPermission> assignTenants = roleDataPermissionMapper.selectByRoleId(roleId);

            List<Long> assignTenantIdList = assignTenants.stream().map(RoleDataPermission::getRefId).collect(Collectors.toList());

            return userMapper.selectByTenantIds(assignTenantIdList);
        }
        return userList;
    }

    /**
     * 根部门列表
     *
     * @param search
     * @return
     */
    public PageInfo<DepartmentDTO> rootDepartmentList(OrganizationSearch search) {
        Integer rolePermissionType = ShieldContextHolder.getRolePermissionType();

        Page page = null;

        DepartmentSearch deptSearch = new DepartmentSearch()
                .setEnableStatus(search.getEnableStatus())
                .setDepartmentName(search.getOrgName())
                .setIsRoot(Constants.IS_ROOT_DEPARTMENT);

        //非admin和非授权全部数据的用户只能看到自己所在的组织
        if (all.getCode().equals(rolePermissionType)) {
            //do nothing
        } else if (assignTenants.getCode().equals(rolePermissionType)) {
            //编辑角色，以角色租户可见度为准（roleId不为空）
            if (ObjectUtil.isNotNull(search.getRoleId())) {
                List<RoleTenant> roleTenantList = roleTenantMapper.selectByRoleId(search.getRoleId());

                List<Long> tenantIdList = roleTenantList.stream().map(RoleTenant::getTenantId).collect(Collectors.toList());

                deptSearch.setTenantIds(tenantIdList);
            } else {
                //非编辑角色，以指定数据权限可见度为准
                List<RoleDataPermission> permissionTenants = roleDataPermissionMapper.selectByRoleId(ShieldContextHolder.getRoleId());

                List<Long> tenantIdList = permissionTenants.stream().map(RoleDataPermission::getRefId).collect(Collectors.toList());

                deptSearch.setTenantIds(tenantIdList);
            }
        } else {
            deptSearch.setTenantId(ShieldContextHolder.getTenantId());
        }

        // 2020/12/4 模糊搜索
        Set<Long> superiorIds = Sets.newHashSet();
        if (StringUtils.isNotEmpty(search.getOrgName())) {
            //得到模糊查询得到的部门的所有上级id
            OrgTreeSearch treeSearch = new OrgTreeSearch()
                    .setTenantId(deptSearch.getTenantId())
                    .setOrgName(deptSearch.getDepartmentName())
                    .setEnableStatus(deptSearch.getEnableStatus())
                    .setTenantIds(deptSearch.getTenantIds());

            List<OrgSuperiorModel> orgSuperiorModelList = organizationMapper.selectSuperiorIdsByOrg(treeSearch);

            superiorIds = getSuperiorIds(orgSuperiorModelList);

            if (CollectionUtils.isEmpty(superiorIds)) {
                superiorIds.add(0L);
            }
            //置空模糊查询条件，根据上级id查根组织
            deptSearch.setOrgIds(superiorIds).setDepartmentName(null).setEnableStatus(null);
        }

        if (search.isForPage()) {
            page = PageHelper.startPage(search.getPageNumber(), search.getPageSize());
        }
        //因为前面已经过滤出精确id，所以这里要去掉搜索条件
        List<Department> rootList = departmentMapper.search(deptSearch);

        List<DepartmentDTO> departmentDTOList = this.getDepartmentAssembled(rootList, search.isHasChild(), search.isHasUserNumber());

        PageInfo<DepartmentDTO> pageInfo = new PageInfo<>(departmentDTOList);
        if (search.isForPage()) {
            pageInfo.setPages(page.getPages());//总页数
            pageInfo.setTotal(page.getTotal());//总条数
        }
        return pageInfo;
    }

    public List<DepartmentDTO> childrenList(OrganizationSearch search) {

        Department department = departmentMapper.selectById(search.getRootId());

        // 2020/12/4 模糊搜索
        if (StringUtils.isNotEmpty(search.getOrgName())) {
            //得到模糊查询得到的部门的所有上级id
            List<OrgSuperiorModel> orgSuperiorModelList = organizationMapper.selectSuperiorIdsByOrg(new OrgTreeSearch()
                    .setTenantId(department.getTenantId())
                    .setOrgName(search.getOrgName())
                    .setEnableStatus(search.getEnableStatus()));

            Set<Long> superiorIds = getSuperiorIds(orgSuperiorModelList);

            //置空模糊查询条件，根据上级id查根组织
            search.setTenantId(department.getTenantId()).setOrgIds(superiorIds).setOrgName(null);
        }

        List<Department> departmentList = organizationMapper.selectChildrenList(search);

        List<DepartmentDTO> departmentDTOList = this.getDepartmentAssembled(departmentList, true, false);

        if (ShieldContextHolder.isSuperAdmin() || all.getCode().equals(ShieldContextHolder.getRolePermissionType())) {
            return departmentDTOList;
        }

        //下面代码控制角色的可见权限
        List<Long> departmentIdList = this.getCurrentUserDepartmentIds();

        departmentDTOList = departmentDTOList.stream().filter(d -> departmentIdList.contains(d.getId())).collect(Collectors.toList());

        return departmentDTOList;
    }

    /**
     * 获取上级组织id
     *
     * @param superiorModelList
     * @return
     */
    public Set<Long> getSuperiorIds(List<OrgSuperiorModel> superiorModelList) {
        Set<Long> superiorIds = Sets.newHashSet();
        superiorModelList.forEach(osm -> {
            if (com.glsx.plat.common.utils.StringUtils.isNotEmpty(osm.getSuperiorIds())) {
                String[] ids = osm.getSuperiorIds().split(",");
                for (String id : ids) {
                    superiorIds.add(Long.valueOf(id));
                }
            }
        });
        return superiorIds;
    }

    /**
     * 封装部门数据
     *
     * @param departmentList
     * @param hasChild
     * @param hasUserNumber
     * @return
     */
    public List<DepartmentDTO> getDepartmentAssembled(List<Department> departmentList, final boolean hasChild, final boolean hasUserNumber) {

        List<Long> departmentIds = departmentList.stream().map(Department::getId).collect(Collectors.toList());

        List<DepartmentDTO> departmentDTOList = departmentList.stream().map(dep -> {
                    DepartmentDTO departmentDTO = new DepartmentDTO();
                    BeanUtils.copyProperties(dep, departmentDTO);
                    return departmentDTO;
                }
        ).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(departmentIds)) {
            if (hasChild) {
                List<Organization> organizationList = organizationMapper.selectSubOrgList(departmentIds, 1);

                Map<Long, List<Long>> subOrganizationMap = organizationList.stream().collect(Collectors.toMap(Organization::getSuperiorId, org -> Lists.newArrayList(org.getSubId()),
                        (List<Long> newValueList, List<Long> oldValueList) -> {
                            oldValueList.addAll(newValueList);
                            return oldValueList;
                        }));

                departmentDTOList.forEach(dep -> dep.setHasChildren(subOrganizationMap.get(dep.getId()) != null));
            }

            if (hasUserNumber) {
                Map<Long, Integer> recursiveDepartmentUser = countRecursiveDepartmentUser(departmentIds);

                departmentDTOList.forEach(dep -> {
                    Integer number = recursiveDepartmentUser.get(dep.getId());
                    dep.setUserNumber(number == null ? 0 : number);
                });
            }
        }
        return departmentDTOList;
    }

    /**
     * 统计部门及下级部门用户数 （批量）
     *
     * @param departmentIds
     * @return
     */
    public Map<Long, Integer> countRecursiveDepartmentUser(List<Long> departmentIds) {
        Map<Long, Integer> departmentUserMap = Maps.newHashMap();
        if (CollectionUtils.isEmpty(departmentIds)) {
            return departmentUserMap;
        }

        //只得到存在下级的组织
        List<Organization> allSubList = organizationMapper.selectSubOrgList(departmentIds, null);

        Map<Long, List<Long>> subDepartmentIdListMap = allSubList.stream().collect(Collectors.groupingBy(Organization::getSuperiorId))
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().map(Organization::getSubId)
                        .collect(Collectors.toList())));

        //加上没有下级的组织
        departmentIds.forEach(item -> {
            if (!subDepartmentIdListMap.containsKey(item)) {
                subDepartmentIdListMap.put(item, Lists.newArrayList());
            }
        });

        List<Long> subDepartmentIdList = allSubList.stream().map(Organization::getSubId).distinct().collect(Collectors.toList());

        List<DepartmentCount> departmentCountList = userMapper.countDepartmentsUser(subDepartmentIdList);

        Map<Long, Integer> departmentUserNumberMap = departmentCountList.stream().collect(Collectors.toMap(DepartmentCount::getDepartmentId, DepartmentCount::getUserNumber));

        for (Map.Entry<Long, List<Long>> entry : subDepartmentIdListMap.entrySet()) {
            Long parentId = entry.getKey();
            List<Long> subIdList = entry.getValue();

            if (!subIdList.contains(parentId)) {
                subIdList.add(parentId);
            }

            Integer departmentUserNumber = 0;
            for (Long subId : subIdList) {
                if (departmentUserNumberMap.get(subId) != null) {
                    departmentUserNumber += departmentUserNumberMap.get(subId);
                }
            }
            departmentUserMap.put(entry.getKey(), departmentUserNumber);
        }
        return departmentUserMap;
    }

    public List<Department> getTenantPathsByUserId(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Lists.newArrayList();
        }
        List<Organization> superiorOrgList = organizationMapper.selectAllSuperiorBySubId(user.getDepartmentId());

        List<Long> superiorOrgIds = superiorOrgList.stream().map(Organization::getSuperiorId).collect(Collectors.toList());

        List<Department> list = departmentMapper.selectByIds(superiorOrgIds);

        return list;
    }

    public List<UserSuperiorOrgModel> getTenantByUserIds(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Lists.newArrayList();
        }
        return departmentMapper.getTenantPathsByUserIds(userIds);
    }

    public List<Tenant> getRolePermissionTenants() {
        List<Tenant> tenantList = Lists.newArrayList();
        if (ShieldContextHolder.isSuperAdmin()) {
            tenantList = tenantMapper.selectAllNotDel();
        } else {
            List<RoleDataPermission> permissionTenants = roleDataPermissionMapper.selectByRoleId(ShieldContextHolder.getRoleId());

            List<Long> permissionTenantIdList = permissionTenants.stream().map(RoleDataPermission::getRefId).collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(permissionTenantIdList)) {
                tenantList = tenantMapper.selectByIds(permissionTenantIdList);
            }
        }
        return tenantList;
    }

    public List<Tenant> getRolePermissionTenants(Long roleId) {
        List<Tenant> tenantList = this.getRolePermissionTenants();

        //追加编辑角色已指定数据的权限 和当前登录用户角色指定权限冲突的数据处理
        if (ObjectUtil.isNotNull(roleId)) {

            List<Long> tenantIdList = tenantList.stream().map(Tenant::getId).collect(Collectors.toList());

            List<RoleDataPermission> permissionTenants = roleDataPermissionMapper.selectByRoleId(roleId);

            List<Long> roleTenantIdList = permissionTenants.stream().map(RoleDataPermission::getRefId).collect(Collectors.toList());

            roleTenantIdList.forEach(tId -> {
                if (!tenantIdList.contains(tId)) {
                    Tenant tenant = tenantMapper.selectById(tId);
                    tenantList.add(tenant);
                }
            });
        }
        return tenantList;
    }

}
