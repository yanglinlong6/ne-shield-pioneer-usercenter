package cn.com.glsx.shield.modules.service;

import cn.com.glsx.auth.model.BizDataPermission;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.shield.common.exception.UserCenterException;
import cn.com.glsx.shield.modules.converter.RoleBizDataPermissionConverter;
import cn.com.glsx.shield.modules.converter.RoleConverter;
import cn.com.glsx.shield.modules.entity.*;
import cn.com.glsx.shield.modules.mapper.*;
import cn.com.glsx.shield.modules.model.param.RoleBO;
import cn.com.glsx.shield.modules.model.param.RoleSearch;
import cn.com.glsx.shield.modules.model.view.RoleDTO;
import cn.com.glsx.shield.modules.model.view.SimpleRoleDTO;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.glsx.plat.common.utils.StringUtils;
import com.glsx.plat.exception.SystemMessage;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.com.glsx.admin.common.constant.UserConstants.RolePermitCastType.all;
import static cn.com.glsx.admin.common.constant.UserConstants.RolePermitCastType.assignTenants;
import static cn.com.glsx.admin.common.constant.UserConstants.RoleVisibility.*;

/**
 * @author: taoyr
 **/
@Slf4j
@Service
public class RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    private RoleTenantMapper roleTenantMapper;

    @Resource
    private RoleDataPermissionMapper roleDataPermissionMapper;

    @Resource
    private RoleBizDataPermissionMapper roleBizDataPermissionMapper;

    public PageInfo<RoleDTO> search(RoleSearch search) {

        List<Role> list = Lists.newArrayList();

        Integer rolePermission = ShieldContextHolder.getRolePermissionType();

        //角色数据正常只由超管和运营可见，如果账号角色菜单分配了，那他只能看共享角色+指定租户角色
        Page page = PageHelper.startPage(search.getPageNumber(), search.getPageSize());
        if (ShieldContextHolder.isSuperAdmin() || all.getCode().equals(rolePermission)) {
            //全部
            list = roleMapper.selectList(search);
        } else if (assignTenants.getCode().equals(rolePermission)) {
            //指定数据权限(当前登录用户对应账号角色的数据权限指定的租户下面的角色)
            list = roleMapper.selectAssignTenantsList(search.setRoleId(ShieldContextHolder.getRoleId()));
        } else {
            //共享角色+当前登录账号租户下角色
            list = roleMapper.selectVisibilityList(search.setTenantIds(Lists.newArrayList(ShieldContextHolder.getTenantId())));
        }

        List<RoleDTO> roleDTOList = getRoleListAssembled(list);

        PageInfo<RoleDTO> pageInfo = new PageInfo<>(roleDTOList);
        pageInfo.setPages(page.getPages());//总页数
        pageInfo.setTotal(page.getTotal());//总条数
        return pageInfo;
    }

    private List<RoleDTO> getRoleListAssembled(List<Role> list) {
        List<RoleDTO> roleDtoList = list.stream().map(role -> {
            RoleDTO roleDTO = new RoleDTO();
            BeanUtils.copyProperties(role, roleDTO);
            return roleDTO;
        }).collect(Collectors.toList());

        return roleDtoList;
    }

    public List<Role> getUserRoleList(Long userId) {
        UserRoleRelation userRoleRelation = new UserRoleRelation();
        userRoleRelation.setUserId(userId);
        List<UserRoleRelation> userRoleRelationList = userRoleRelationMapper.selectUserRoleRelationList(userRoleRelation);

        List<Long> roleIds = userRoleRelationList.stream().map(UserRoleRelation::getRoleId).collect(Collectors.toList());

        if (CollectionUtil.isEmpty(roleIds)) {
            return Lists.newArrayList();
        }

        List<Role> roleList = roleMapper.selectByIds(roleIds);

        return roleList;
    }

    public Role getRoleById(Long roleId) {
        return roleMapper.selectById(roleId);
    }

    public List<RoleBizDataPermission> getRoleBizDataPermission(Collection<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Lists.newArrayList();
        }
        return roleBizDataPermissionMapper.selectByRoleIds(roleIds);
    }

    /**
     * 根据角色权限范围获取角色列表
     *
     * @return
     */
    public List<SimpleRoleDTO> simpleList(Long userId) {

        List<SimpleRoleDTO> list;

        List<Role> roleList = Lists.newLinkedList();

        if (ShieldContextHolder.isSuperAdmin()) {
            //假设当前登陆的A账号（角色：系统管理员），则该账号创建账号的时候角色的选择范围可以覆盖系统所有角色
            roleList = roleMapper.selectList(new RoleSearch().setEnableStatus(EnableStatus.enable.getCode()));
        } else {
            //假设当前登陆的B账号（角色：租户B管理员），则该账号创建账号的时候角色的选择范围可以覆盖共享角色+对该租户开放的角色
            Integer roleVisibility = ShieldContextHolder.getRoleVisibility();
            if (superAdmin.getCode().equals(roleVisibility)) {
                roleList = roleMapper.selectList(new RoleSearch());
            } else if (share.getCode().equals(roleVisibility) || specifyTenants.getCode().equals(roleVisibility)) {

                List<Role> shareRoleList = roleMapper.selectByVisibilityType(share.getCode());

                roleList.addAll(shareRoleList);

                //指定租户可见角色
                if (specifyTenants.getCode().equals(roleVisibility)) {
                    List<Long> roleTenantIdList = roleTenantMapper.selectTenantIdsByRoleId(ShieldContextHolder.getRoleId());

                    List<Role> tenantRoleList = roleMapper.selectByTenantIds(roleTenantIdList);

                    roleList.addAll(tenantRoleList);
                }

                //跨租户冲突角色
                if (ObjectUtil.isNotNull(userId)) {
                    List<Long> roleIdList = roleList.stream().map(Role::getId).collect(Collectors.toList());

                    List<UserRoleRelation> urList = userRoleRelationMapper.selectByUserId(userId);

                    for (UserRoleRelation ur : urList) {
                        if (!roleIdList.contains(ur.getRoleId())) {
                            Role role = roleMapper.selectById(ur.getRoleId());
                            if (role != null) {
                                roleList.add(role);
                            }
                        }
                    }
                }
            }
        }

        list = roleList.stream().map(role -> new SimpleRoleDTO().setRoleId(role.getId()).setRoleName(role.getRoleName())).collect(Collectors.toList());

        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public Role addRole(RoleBO roleBO) {

        checkRole(roleBO);

        Role role = RoleConverter.INSTANCE.bo2do(roleBO);

        role.setContextInfo(true);

        role.setTenantId(ShieldContextHolder.getTenantId());

        roleMapper.insertUseGeneratedKeys(role);

        //角色可见控制
        if (specifyTenants.getCode().equals(roleBO.getRoleVisibility())) {
            if (StringUtils.isBlank(roleBO.getRoleTenants())) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "指定租户不能为空");
            }
            String[] tenantIds = roleBO.getRoleTenants().split(",");
            List<RoleTenant> roleTenantList = new ArrayList<>(tenantIds.length);
            Arrays.asList(tenantIds).forEach(item -> {
                roleTenantList.add(new RoleTenant().setRoleId(role.getId()).setTenantId(Long.valueOf(item)));
            });
            roleTenantMapper.insertList(roleTenantList);
        }

        //角色数据权限控制
        if (assignTenants.getCode().equals(roleBO.getRolePermissionType())) {
            if (StringUtils.isBlank(roleBO.getPermTenants())) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "指定数据权限不能为空");
            }

            String[] tenantIds = roleBO.getPermTenants().split(",");
            List<RoleDataPermission> permTenantList = new ArrayList<>(tenantIds.length);
            Arrays.asList(tenantIds).forEach(item -> {
                permTenantList.add(new RoleDataPermission().setRoleId(role.getId()).setRefId(Long.valueOf(item)).setType(0));
            });
            roleDataPermissionMapper.insertList(permTenantList);
        }

        //业务数据权限
        if (CollectionUtils.isNotEmpty(roleBO.getBizDataPermissions())) {
            List<RoleBizDataPermission> bizDataPermissionList = Lists.newArrayList();
            roleBO.getBizDataPermissions().forEach(bdp -> {
                RoleBizDataPermission bizDataPermission = new RoleBizDataPermission();
                bizDataPermission.setRoleId(role.getId());
                bizDataPermission.setType(bdp.getType());
                bizDataPermission.setVisibility(bdp.getVisibility());
                bizDataPermission.setCreatedBy(ShieldContextHolder.getUserId());
                bizDataPermission.setCreatedDate(new Date());
                bizDataPermissionList.add(bizDataPermission);
            });
            if (CollectionUtils.isNotEmpty(bizDataPermissionList)) {
                roleBizDataPermissionMapper.insertList(bizDataPermissionList);
            }
        }

        //角色菜单
        List<Long> menuIdList = getFixMenuIds(roleBO.getMenuIdList());

        List<RoleMenu> roleMenuList = menuIdList.stream().map(menuNo -> new RoleMenu(true)
                        .setMenuNo(menuNo)
                        .setRoleId(role.getId()))
                .collect(Collectors.toList());
        roleMenuMapper.insertList(roleMenuList);

        return role;
    }

    @Transactional(rollbackFor = Exception.class)
    public void editRole(RoleBO roleBO) {

        checkRole(roleBO);

        Role role = RoleConverter.INSTANCE.bo2do(roleBO);

        role.setContextInfo(false);

        roleMapper.updateByPrimaryKeySelective(role);

        //角色可见控制
        roleTenantMapper.logicDeleteByRoleId(role.getId());
        if (specifyTenants.getCode().equals(roleBO.getRoleVisibility())) {
            if (StringUtils.isBlank(roleBO.getRoleTenants())) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "指定租户不能为空");
            }

            String[] tenantIds = roleBO.getRoleTenants().split(",");
            List<RoleTenant> roleTenantList = new ArrayList<>(tenantIds.length);
            Arrays.asList(tenantIds).forEach(item -> {
                roleTenantList.add(new RoleTenant().setRoleId(role.getId()).setTenantId(Long.valueOf(item)));
            });
            roleTenantMapper.insertList(roleTenantList);
        }

        roleDataPermissionMapper.logicDeleteByRoleId(role.getId());
        //角色数据权限控制
        if (assignTenants.getCode().equals(roleBO.getRolePermissionType())) {
            if (StringUtils.isBlank(roleBO.getPermTenants())) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "指定数据权限不能为空");
            }

            String[] tenantIds = roleBO.getPermTenants().split(",");
            List<RoleDataPermission> permTenantList = new ArrayList<>(tenantIds.length);
            Arrays.asList(tenantIds).forEach(item -> {
                permTenantList.add(new RoleDataPermission().setRoleId(role.getId()).setRefId(Long.valueOf(item)).setType(0));
            });
            roleDataPermissionMapper.insertList(permTenantList);
        }

        roleBizDataPermissionMapper.logicDeleteByRoleId(role.getId());
        //业务数据权限
        if (CollectionUtils.isNotEmpty(roleBO.getBizDataPermissions())) {
            List<RoleBizDataPermission> bizDataPermissionList = Lists.newArrayList();
            roleBO.getBizDataPermissions().forEach(bdp -> {
                RoleBizDataPermission bizDataPermission = new RoleBizDataPermission();
                bizDataPermission.setRoleId(role.getId());
                bizDataPermission.setType(bdp.getType());
                bizDataPermission.setVisibility(bdp.getVisibility());
                bizDataPermission.setCreatedBy(ShieldContextHolder.getUserId());
                bizDataPermission.setCreatedDate(new Date());
                bizDataPermissionList.add(bizDataPermission);
            });
            if (CollectionUtils.isNotEmpty(bizDataPermissionList)) {
                roleBizDataPermissionMapper.insertList(bizDataPermissionList);
            }
        }

        //角色菜单
        roleMenuMapper.logicDelByRoleId(role.getId());

        List<Long> menuIdList = getFixMenuIds(roleBO.getMenuIdList());

        List<RoleMenu> roleMenuList = menuIdList.stream().map(menuNo -> new RoleMenu(true)
                .setMenuNo(menuNo)
                .setRoleId(role.getId())).collect(Collectors.toList());

        roleMenuMapper.insertList(roleMenuList);
    }

    /**
     * 将缺失的夫级id补全
     *
     * @param menuIdList 页面传递的id集合
     */
    private List<Long> getFixMenuIds(List<Long> menuIdList) {
        //把已有的子菜单id转为Set
        HashSet<Long> parentids = new HashSet<>(menuIdList);

        //Long转String,根据01判断一级父id,每多一级长度加2,截取下一级父id,添加set
        menuIdList.stream().map(String::valueOf).forEach(menuId -> {
            int length = menuId.length();
            int rootIndex = menuId.indexOf("01") + 2;
            if (rootIndex != -1) {
                for (int i = rootIndex; i < length; i += 2) {
                    String parentIdStr = menuId.substring(0, i);
                    parentids.add(Long.parseLong(parentIdStr));
                }
            }
        });
        return new ArrayList<>(parentids);
    }

    /**
     * 检查角色关键信息
     *
     * @param roleBO
     */
    private void checkRole(RoleBO roleBO) {
        if (roleBO.getRoleId() == null) {
            int cnt = roleMapper.selectCntByName(roleBO.getRoleName());
            if (cnt > 0) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "相同角色已存在");
            }
        } else {
            Role dbRole = roleMapper.selectByName(roleBO.getRoleName());
            if (dbRole != null && !dbRole.getId().equals(roleBO.getRoleId())) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "相同角色已存在");
            }
        }

        if (!ShieldContextHolder.isSuperAdmin()) {
            if (superAdmin.getCode().equals(roleBO.getRoleVisibility())) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "非管理员账号不能赋予管理员角色");
            }
            if (all.getCode().equals(roleBO.getRolePermissionType())) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "非管理员账号不能授权全部数据权限角色");
            }
        }
    }

    public RoleDTO roleInfo(Long id) {
        RoleDTO roleDTO = null;
        Role role = roleMapper.selectById(id);
        if (role != null) {
            roleDTO = new RoleDTO();
            BeanUtils.copyProperties(role, roleDTO);

            if (specifyTenants.getCode().equals(role.getRoleVisibility())) {
                List<Long> roleTenantIdList = roleTenantMapper.selectTenantIdsByRoleId(id);
                String roleTenants = StringUtils.join(roleTenantIdList, ',');
                roleDTO.setRoleTenants(roleTenants);
            }

            if (assignTenants.getCode().equals(role.getRolePermissionType())) {
                List<Long> permTenantIdList = roleDataPermissionMapper.selectRefIdsByRoleId(id);
                String roleTenants = StringUtils.join(permTenantIdList, ',');
                roleDTO.setPermTenants(roleTenants);
            }

            //业务数据权限
            List<RoleBizDataPermission> roleBizDataPermissionList = roleBizDataPermissionMapper.selectByRoleId(id);
            List<BizDataPermission> bizDataPermissionList = Lists.newArrayList();
            roleBizDataPermissionList.forEach(rbdp -> bizDataPermissionList.add(RoleBizDataPermissionConverter.INSTANCE.bo2simple(rbdp)));
            roleDTO.setBizDataPermissions(bizDataPermissionList);
        }
        return roleDTO;
    }

    public void deleteRole(Long id) {
        List<UserRoleRelation> relations = userRoleRelationMapper.selectUserRoleRelationList(new UserRoleRelation().setRoleId(id));
        if (CollectionUtils.isNotEmpty(relations)) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "请取消相关账号与该角色的关联后再删除");
        }
        roleTenantMapper.logicDeleteByRoleId(id);
        roleDataPermissionMapper.logicDeleteByRoleId(id);
        roleMapper.logicDeleteById(id);
    }

    public Role getRoleByName(String roleName) {
        return roleMapper.selectByName(roleName);
    }

}
