package cn.com.glsx.shield.modules.service;

import cn.com.glsx.admin.common.constant.Constants;
import cn.com.glsx.admin.common.enums.UserGlobalType;
import cn.com.glsx.auth.model.BizDataPermission;
import cn.com.glsx.auth.model.SyntheticUser;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.shield.common.exception.UserCenterException;
import cn.com.glsx.shield.modules.converter.*;
import cn.com.glsx.shield.modules.entity.*;
import cn.com.glsx.shield.modules.mapper.*;
import cn.com.glsx.shield.modules.model.export.UserExport;
import cn.com.glsx.shield.modules.model.param.ChangePasswordBO;
import cn.com.glsx.shield.modules.model.param.UserBO;
import cn.com.glsx.shield.modules.model.param.UserSearch;
import cn.com.glsx.shield.modules.model.view.SuperTreeModel;
import cn.com.glsx.shield.modules.model.view.UserDTO;
import cn.com.glsx.shield.modules.service.permissionStrategy.PermissionStrategy;
import cn.com.glsx.usercenter.api.dto.request.FindUserRequest;
import cn.com.glsx.usercenter.api.dto.request.SimpleAddUser;
import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.glsx.plat.common.utils.ObjectUtils;
import com.glsx.plat.common.utils.StringUtils;
import com.glsx.plat.core.enums.SysConstants;
import com.glsx.plat.exception.SystemMessage;
import com.glsx.plat.jwt.base.ComJwtUser;
import com.glsx.plat.jwt.util.JwtUtils;
import com.glsx.plat.web.utils.SessionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.com.glsx.admin.common.constant.UserConstants.RolePermitCastType.*;
import static cn.com.glsx.admin.common.constant.UserConstants.RoleVisibility.*;

/**
 * @author liuyf
 * @desc 用户信息
 * @date 2019年10月24日 下午2:37:40
 */
@Slf4j
@Service
public class UserService {

    @Lazy
    @Resource
    OrganizationService organizationService;
    @Resource
    private HashedCredentialsMatcher hcm;
    @Resource
    private JwtUtils<ComJwtUser> jwtUtils;
    @Resource
    private UserMapper userMapper;
    @Resource
    private TenantMapper tenantMapper;
    @Resource
    private DepartmentMapper departmentMapper;
    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private UserPathMapper userPathMapper;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private RoleMenuMapper roleMenuMapper;
    @Resource
    private RoleTenantMapper roleTenantMapper;
    @Resource
    private RoleDataPermissionMapper roleDataPermissionMapper;

    @Autowired
    private MenuService menuService;
    @Autowired
    private RoleService roleService;
    @Lazy
    @Resource
    private DepartmentService departmentService;

    private static SuperTreeModel applySuperTreeModel(Department dept) {
        SuperTreeModel model = new SuperTreeModel();
        model.setId(dept.getId());
        model.setLabel(dept.getDepartmentName());
        model.setOrder(dept.getOrderNum());
        model.setType(0);
        model.setRoot(dept.getIsRoot() == 1);
        model.setDisabled(dept.getIsRoot() == 1);
        return model;
    }

    private static SuperTreeModel applySuperTreeModel(User du) {
        SuperTreeModel model = new SuperTreeModel();
        model.setId(du.getId());
        model.setLabel(du.getUsername());
        model.setType(1);
        return model;
    }

    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    public User findByAccount(String account) {
        //todo 不同租户相同账号怎么处理？？？
        List<User> userList = userMapper.selectByAccount(account);
        if (CollectionUtils.isNotEmpty(userList)) {
            return userList.get(0);
        }
        return null;
    }

    public List<User> findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    public List<User> findUsers(FindUserRequest condition) {
        if (StringUtils.isEmpty(condition.getAccount()) && StringUtils.isEmpty(condition.getUsername())
                && CollectionUtils.isEmpty(condition.getOrgIds()) && CollectionUtils.isEmpty(condition.getUserIds())) {
            return Lists.newArrayList();
        }
        return userMapper.selectUsersByCondition(condition);
    }

    /**
     * 获取权限内全部用户
     *
     * @return
     */
    public List<User> getAllUserWithinAuthority() {
        // 2021/6/7 租户全部用户，就算账号权限是管理员或配置能看全部数据，也只返回当前用户自己所在租户用户，不同租户下面可能存在一模一样的账号
        // 2022/03/10 账号做全局控制，不存在同名账号

        PermissionStrategy permissionStrategy = organizationService.getPermissionStrategy();

        return permissionStrategy.permissionUsers();
    }

    public UserDTO userInfo(Long userId) {
        UserDTO userDTO = null;

        User user = userMapper.selectById(userId);
        if (user != null) {
            userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);

            List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectUserRoleRelationList(new UserRoleRelation().setUserId(userId));
            if (CollectionUtils.isNotEmpty(userRoleRelations)) {
                Long roleId = userRoleRelations.get(0).getRoleId();
                userDTO.setRoleId(roleId);

                Role role = roleMapper.selectById(roleId);
                userDTO.setRoleName(role != null ? role.getRoleName() : "");
            }

            Long departmentId = userDTO.getDepartmentId();
            Department department = departmentMapper.selectById(departmentId);
            if (department != null) {
                userDTO.setDepartmentName(department.getDepartmentName());
                userDTO.setDeptDepth(department.getIsRoot() == Constants.IS_ROOT_DEPARTMENT ? 0 : 1);
            }

            Long superiorId = userDTO.getSuperiorId();
            User superiorUser = userMapper.selectById(superiorId);
            userDTO.setSuperiorName(superiorUser != null ? superiorUser.getUsername() : "");
        }
        return userDTO;
    }

    @Transactional(rollbackFor = Exception.class)
    public int logicDeleteById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "该用户不存在，请重新刷新列表");
        }
        List<UserPath> userPaths = userPathMapper.selectSubordinateBySuperiorId(id);

        Set<Long> subUserIds = userPaths.stream().map(UserPath::getSubId).collect(Collectors.toSet());
        //超过包含自己的用户数
        if (CollectionUtils.isNotEmpty(subUserIds) && subUserIds.size() > 1) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "该用户存在下级用户，请移除这部分用户后再重试");
        }
        userPathMapper.deleteUserAllPath(id);

        return userMapper.logicDeleteById(id);
    }

    public PageInfo<UserDTO> search(UserSearch search) {

        Page page = null;

        Integer rolePermissionType = ShieldContextHolder.getRolePermissionType();

        Long userId = ShieldContextHolder.getUserId();

        Long userDeptId = ShieldContextHolder.getDepartment().getDeptId();

        List<Long> selectDeptIdList = Lists.newArrayList();
        if (search.getDepartmentId() != null) {
            //当前选中部门
            selectDeptIdList.add(search.getDepartmentId());
        }

        List<User> userList;

        if (subordinate.getCode().equals(rolePermissionType)) {
            //所有子部门的下属员工
            List<UserPath> userPathList = userPathMapper.selectSubordinateBySuperiorId(userId);

            List<Long> subUserIdList = userPathList.stream().map(UserPath::getSubId).collect(Collectors.toList());

            List<User> subUserList = userMapper.selectByIds(subUserIdList);

            List<Long> departmentIds = subUserList.stream().map(User::getDepartmentId).collect(Collectors.toList());

            search.setDepartmentIds(departmentIds);

            if (CollectionUtils.isNotEmpty(selectDeptIdList)) {
                //选中部门下属部门
                List<Organization> subList = organizationMapper.selectSubOrgList(selectDeptIdList, null);
                //当前账号下属部门
                List<Long> subDepartmentIds = subList.stream().map(Organization::getSubId).filter(departmentIds::contains).collect(Collectors.toList());

                search.setDepartmentIds(subDepartmentIds);
            }

            search.setUserId(userId);

            page = PageHelper.startPage(search.getPageNumber(), search.getPageSize());

            userList = userMapper.selectDepartmentsSubordinate(search);
        } else {
            if (oneself.getCode().equals(rolePermissionType)) {
                search.setUserId(ShieldContextHolder.getUserId());
            } else if (selfDepartment.getCode().equals(rolePermissionType)) {
                //本人部门同级全部用户
                search.setDepartmentIds(Lists.newArrayList(userDeptId));
            } else if (subDepartment.getCode().equals(rolePermissionType)) {
                //当前用户所有下级部门（包含自己部门）
                List<Organization> subDepartmentList = organizationMapper.selectAllSubBySuperiorId(userDeptId);

                List<Long> subDepartmentIdList = subDepartmentList.stream().map(Organization::getSubId).collect(Collectors.toList());

                List<Long> departmentIds = Lists.newArrayList();

                if (CollectionUtils.isNotEmpty(selectDeptIdList)) {
                    //选中部门下属部门
                    List<Organization> subList = organizationMapper.selectSubOrgList(selectDeptIdList, null);
                    //当前账号所属部门的下级部门
                    departmentIds = subList.stream().filter(org -> subDepartmentIdList.contains(org.getSubId())).map(Organization::getSubId).collect(Collectors.toList());
                } else {
                    departmentIds.addAll(subDepartmentIdList);
                }
                search.setDepartmentIds(departmentIds);
            } else if (all.getCode().equals(rolePermissionType)) {
                //部门id
                if (CollectionUtils.isNotEmpty(selectDeptIdList)) {
                    List<Organization> subList = organizationMapper.selectAllSubBySuperiorId(search.getDepartmentId());

                    List<Long> departmentIds = subList.stream().map(Organization::getSubId).collect(Collectors.toList());

                    departmentIds.addAll(selectDeptIdList);

                    search.setDepartmentIds(departmentIds);
                }
            } else if (assignTenants.getCode().equals(rolePermissionType)) {
                List<Tenant> tenantList = departmentService.getRolePermissionTenants();

                List<Long> tenantIdList = tenantList.stream().map(Tenant::getId).collect(Collectors.toList());

                search.setTenantIds(tenantIdList);

                if (CollectionUtils.isNotEmpty(selectDeptIdList)) {
                    List<Organization> subList = organizationMapper.selectAllSubBySuperiorId(search.getDepartmentId());

                    List<Long> departmentIds = subList.stream().map(Organization::getSubId).collect(Collectors.toList());

                    departmentIds.addAll(selectDeptIdList);

                    search.setDepartmentIds(departmentIds);
                }
            }
            page = PageHelper.startPage(search.getPageNumber(), search.getPageSize());
            userList = userMapper.search(search);
        }

        List<UserDTO> userDTOList = userListAssembled(userList);

        PageInfo<UserDTO> pageInfo = new PageInfo<>(userDTOList);
        pageInfo.setPages(page.getPages());//总页数
        pageInfo.setTotal(page.getTotal());//总条数
        return pageInfo;
    }

    public List<UserDTO> permissionUsers() {
        List<User> userList = organizationService.permissionUsers();
        return userListAssembled(userList);
    }

    public List<UserDTO> permissionUsersByDepartmentId(Long departmentId) {
        List<User> userList = organizationService.permissionUsersByDepartmentId(departmentId);
        return userListAssembled(userList);
    }

    public List<UserDTO> userListAssembled(List<User> userList) {
        List<UserDTO> userDTOList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(userList)) {
            return userDTOList;
        }

        List<Long> departmentIdList = userList.stream().map(User::getDepartmentId).collect(Collectors.toList());

        List<Department> departmentList = departmentMapper.selectByIds(departmentIdList);

        Map<Long, Department> departmentMap = departmentList.stream().collect(Collectors.toMap(Department::getId, d -> d));

        for (User user : userList) {
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            Department department = departmentMap.get(user.getDepartmentId());
            userDTO.setDepartmentName(Optional.ofNullable(department).map(d -> department.getDepartmentName()).orElse(""));
            userDTOList.add(userDTO);
        }
        return userDTOList;
    }

    public List<UserExport> export(UserSearch search) {
        return Lists.newArrayList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserBO userBO) {
        //校验部门范围
        checkUserDepartment(userBO);

        //检查用户关键信息
        checkUser(userBO);

        //校验上级范围
        checkUserSuperior(userBO);

        //校验角色
        checkUserRole(userBO);

        // 添加帐号设置创建人
        SyntheticUser user = ShieldContextHolder.getUser();
        addCommonUser(userBO, user.getUserId());

    }

    @Transactional(rollbackFor = Exception.class)
    public void addCommonUser(UserBO userBO, Long createBy) {
        User user = new User();
        user.setCreatedBy(createBy);
        user.setCreatedDate(new Date());
        BeanUtils.copyProperties(userBO, user);

        generateAndSetPassword(user, userBO.getPassword());
        userMapper.insertUseGeneratedKeys(user);

        Long userId = user.getId();

        if (userBO.getSuperiorId() == null) {
            UserPath userPath = new UserPath();
            userPath.setCreatedBy(createBy);
            userPath.setCreatedDate(new Date());
            userPath.setSuperiorId(userId);
            userPath.setSubId(userId);
            userPath.setTenantId(user.getTenantId());
            userPathMapper.insertRootPath(userPath);
            log.info("新增用户根关系{}", userPath);
        } else {
            UserPath userPath = new UserPath();
            userPath.setCreatedBy(createBy);
            userPath.setCreatedDate(new Date());
            userPath.setSuperiorId(userBO.getSuperiorId());
            userPath.setSubId(userId);
            userPath.setTenantId(user.getTenantId());
            int insertCnt = userPathMapper.insertUserPath(userPath);
            log.info("新增用户{}关系{}条", userId, insertCnt);
        }

        userRoleRelationMapper.insert(new UserRoleRelation(createBy).setUserId(userId).setRoleId(userBO.getRoleId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void editUser(UserBO userBO) {
        //校验部门范围
        checkUserDepartment(userBO);

        //检查用户关键信息
        checkUser(userBO);

        //校验上级范围
        checkUserSuperior(userBO);

        //校验角色
        checkUserRole(userBO);

        editCommonUser(userBO, ShieldContextHolder.getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void editCommonUser(UserBO userBO, Long updateBy) {

        User user = userMapper.selectById(userBO.getId());

        Long superiorId = user.getSuperiorId();

        String oldPassword = user.getPassword();

        BeanUtils.copyProperties(userBO, user);

        //如果填了密码，修改密码
        boolean changePwd = StringUtils.isNotBlank(userBO.getPassword());
        if (changePwd) {
            generateAndSetPassword(user, userBO.getPassword());
        } else {
            user.setPassword(oldPassword);
        }
        userMapper.updateByPrimaryKey(user);

        //重建用户上下级关系
        rebuildUserPath(superiorId, userBO);

        //更新角色关系
        List<UserRoleRelation> relationList = userRoleRelationMapper.selectUserRoleRelationList(new UserRoleRelation().setUserId(user.getId()));
        if (CollectionUtils.isNotEmpty(relationList)) {
            UserRoleRelation relation = relationList.get(0);
            if (!relation.getRoleId().equals(userBO.getRoleId())) {
                relation.setRoleId(userBO.getRoleId());
                relation.setUpdatedBy(updateBy);
                relation.setUpdatedDate(new Date());
                userRoleRelationMapper.updateByPrimaryKeySelective(relation);
            }
        }
    }

    /**
     * 重建用户上下级关系
     *
     * @param originSuperiorId
     * @param userBO
     */
    private void rebuildUserPath(Long originSuperiorId, UserBO userBO) {
        if (userBO.getSuperiorId() == null) {
            //原来存在上级，现在去掉上级
            if (originSuperiorId != null) {
                //删除账号到上级的关系
                userPathMapper.deleteUserSuperiorPath(userBO.getId());

                UserPath userPath = new UserPath(true);
                userPath.setSuperiorId(userBO.getId());
                userPath.setSubId(userBO.getId());
                userPath.setTenantId(userBO.getTenantId());
                userPathMapper.insertRootPath(userPath);
                log.info("编辑用户根关系{}", userPath);
            }
        } else {
            //上级改变，重新生成上级到自己的层级关系
            if (!userBO.getSuperiorId().equals(originSuperiorId)) {
                //删除账号到上级的关系
                userPathMapper.deleteUserSuperiorPath(userBO.getId());

                UserPath userPath = new UserPath(true);
                userPath.setSuperiorId(userBO.getSuperiorId());
                userPath.setSubId(userBO.getId());
                userPath.setTenantId(userBO.getTenantId());
                int insertCnt = userPathMapper.insertUserPath(userPath);
                log.info("编辑用户{}关系{}条", userBO.getId(), insertCnt);
            }
        }
    }

    /**
     * 检查用户关键信息
     *
     * @param userBO
     */
    private void checkUser(UserBO userBO) {
        if (userBO.getId() == null) {
            int cnt = userMapper.countByAccount(userBO.getAccount());
//            int cnt = userMapper.countByAccountAndTenantId(userBO.getAccount(), userBO.getTenantId());
            if (cnt > 0) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "相同账号已存在");
            }
        } else {
            User dbUser = this.findByAccount(userBO.getAccount());
//            User dbUser = userMapper.selectByAccountAndTenantId(userBO.getAccount(), userBO.getTenantId());
            if (dbUser != null && !dbUser.getId().equals(userBO.getId())) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "相同账号已存在");
            }
        }
    }

    /**
     * 检查用户部门权限
     *
     * @param userBO
     */
    private void checkUserDepartment(UserBO userBO) {
        Long departmentId = userBO.getDepartmentId();
        List<Long> departmentIdList = departmentService.getCurrentUserDepartmentIds();
        if (!departmentIdList.contains(departmentId)) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "当前用户不具备该部门权限");
        }
        //设置租户
        Department department = departmentMapper.selectById(userBO.getDepartmentId());
        if (department == null) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "部门不存在，请刷新页面重新选择部门");
        }
        userBO.setTenantId(department.getTenantId());
    }

    /**
     * 检查用户上级部门
     *
     * @param userBO
     */
    private void checkUserSuperior(UserBO userBO) {
        if (userBO.getId() != null && userBO.getId().equals(userBO.getSuperiorId())) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "不能选择当前编辑用户为其上级");
        }

        //上三级级组织
        List<Organization> superiorOrgList = organizationMapper.selectSuperiorOrgsByDepths(userBO.getDepartmentId(), Lists.newArrayList(0, 1, 2, 3));
        Set<Long> organizationIdSet = superiorOrgList.stream().map(Organization::getSuperiorId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(organizationIdSet)) {
            User superiorUser = userMapper.selectById(userBO.getSuperiorId());
            if (superiorUser != null) {
                if (!organizationIdSet.contains(superiorUser.getDepartmentId())) {
                    throw new UserCenterException(SystemMessage.FAILURE.getCode(), "上级用户范围不在本部门或上级部门");
                }
            }
        }
    }

    /**
     * 检查用户角色权限
     *
     * @param userBO
     */
    private void checkUserRole(UserBO userBO) {
        //如果是管理员或拥有全部数据权限
        if (ShieldContextHolder.isSuperAdmin() || all.getCode().equals(ShieldContextHolder.getRolePermissionType())) {
            return;
        }

        List<Long> tenantIdList = Lists.newArrayList();

        //2021/8/11 加上数据权限的指定租户（运营跨租户特殊处理）
        if (assignTenants.getCode().equals(ShieldContextHolder.getRolePermissionType())) {
            List<Long> permTenantIdList = roleDataPermissionMapper.selectRefIdsByRoleId(ShieldContextHolder.getRoleId());
            tenantIdList.addAll(permTenantIdList);
        }

        Long roleId = userBO.getRoleId();
        Role role = roleMapper.selectById(roleId);

        if (superAdmin.getCode().equals(role.getRoleVisibility())) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), "该角色不可选");
        } else if (share.getCode().equals(role.getRoleVisibility())) {
            //共享角色直接可以选择
        } else if (specifyTenants.getCode().equals(role.getRoleVisibility())) {
            List<Long> roleTenantIdList = roleTenantMapper.selectTenantIdsByRoleId(ShieldContextHolder.getRoleId());
            tenantIdList.addAll(roleTenantIdList);
            if (CollectionUtils.isEmpty(tenantIdList)) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "角色权限错误");
            }
            //指定租户可见角色
            List<Long> tenantRoleIdList = roleTenantMapper.selectRoleIdsByTenantIds(tenantIdList);
            if (!tenantRoleIdList.contains(roleId)) {
                throw new UserCenterException(SystemMessage.FAILURE.getCode(), "角色不可选");
            }
        }
    }

    /**
     * 生成密码
     *
     * @param user
     */
    private void generateAndSetPassword(User user, String newPassword) {
        String salt = StringUtils.generateRandomCode(false, 4);
        SimpleHash hash = new SimpleHash(hcm.getHashAlgorithmName(), newPassword, salt, hcm.getHashIterations());
        user.setSalt(salt);
        user.setPassword(hash.toString());
    }

    public void verify(User user, String inputPassword) {
        //是否存在
        if (user == null) {
            throw new UserCenterException("当前登录账号不存在");
        }

        //是否停用
        if (!SysConstants.EnableStatus.enable.getCode().equals(user.getEnableStatus())) {
            throw new UserCenterException("当前账号已被停用");
        }

        //密码是否ok
        boolean pwdFlag = this.verifyPassword(user, inputPassword);
        if (!pwdFlag) {
            throw new UserCenterException("账号或密码不正确");
        }
    }

    /**
     * 验证密码
     *
     * @param user
     * @param inputPassword
     * @return
     */
    public boolean verifyPassword(User user, String inputPassword) {
        String dbPassword = user.getPassword();
        String salt = user.getSalt();
        SimpleHash hash = new SimpleHash(hcm.getHashAlgorithmName(), inputPassword, salt, hcm.getHashIterations());
        return dbPassword.equals(hash.toString());
    }

    /**
     * 修改密码
     *
     * @param passwordBO
     */
    public void changePassword(ChangePasswordBO passwordBO) {
        User user = userMapper.selectById(passwordBO.getUserId());

        verify(user, passwordBO.getOldPassword());

        SimpleHash hash = new SimpleHash(hcm.getHashAlgorithmName(), passwordBO.getNewPassword(), user.getSalt(), hcm.getHashIterations());
        userMapper.updatePassword(passwordBO.getUserId(), hash.toString());
    }

    /**
     * 重置密码
     *
     * @param userId
     */
    public void resetPassword(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        String defaultPwd = "ABC123";
        SimpleHash hash = new SimpleHash(hcm.getHashAlgorithmName(), defaultPwd, user.getSalt(), hcm.getHashIterations());
        userMapper.updatePassword(userId, hash.toString());
    }

    /**
     * 生成带用户信息的token
     *
     * @param user
     * @return
     */
    public String createToken(User user) {
        String uuid = UUID.randomUUID().toString(); //JWT 随机ID,做为验证的key
        String jwtId = jwtUtils.getApplication() + ":" + uuid + "_" + jwtUtils.JWT_SESSION_PREFIX + user.getId();
        ComJwtUser jwtUser = new ComJwtUser();
        jwtUser.setApplication(jwtUtils.getApplication());
        jwtUser.setJwtId(jwtId);
        jwtUser.setUserId(String.valueOf(user.getId()));
        jwtUser.setAccount(user.getAccount());
        jwtUser.setTenant(String.valueOf(user.getTenantId()));
        jwtUser.setBelong(String.valueOf(user.getDepartmentId()));

        Map<String, Object> userMap = (Map<String, Object>) ObjectUtils.objectToMap(jwtUser);

        return jwtUtils.createToken(userMap);
    }

    /**
     * 根据Token获取Customer
     */
    public User getByToken() {
        String token = SessionUtils.request().getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isNullOrEmpty(token)) {
            throw new UserCenterException(SystemMessage.NOT_LOGIN.getCode(), SystemMessage.NOT_LOGIN.getMsg());
        }
        Map<String, Object> userMap = jwtUtils.parseClaim(token);
        Object userId = userMap.get("userId");
        if (Objects.isNull(userId)) {
            throw new UserCenterException(SystemMessage.NOT_LOGIN.getCode(), SystemMessage.NOT_LOGIN.getMsg());
        }
        User user = this.getById(Long.valueOf(userId.toString()));
        if (user == null) {
            throw new UserCenterException(SystemMessage.NOT_LOGIN.getCode(), SystemMessage.NOT_LOGIN.getMsg());
        }
        return user;
    }

    /**
     * 从redis删除token缓存
     */
    public void removeToken() {
        String token = SessionUtils.request().getHeader(HttpHeaders.AUTHORIZATION);
        jwtUtils.destroyToken(token);
    }

    /**
     * 获取综合用户信息by token
     *
     * @return
     */
    public SyntheticUser getSyntheticUserByToken() {
        User user = this.getByToken();
        return getSyntheticUser(user);
    }

    /**
     * 获取综合用户信息by userId
     *
     * @return
     */
    public SyntheticUser getSyntheticUserById(Long userId) {
        User user = userMapper.selectById(userId);
        return getSyntheticUser(user);
    }

    /**
     * 获取综合用户信息
     *
     * @return
     */
    public SyntheticUser getSyntheticUser(User user) {
        boolean isSupperAdmin = UserGlobalType.SUPER_ADMIN.getType().equals(user.getGlobalType());

        SyntheticUser authUser = new SyntheticUser();
        authUser.setUserId(user.getId());
        authUser.setAccount(user.getAccount());
        authUser.setUsername(user.getUsername());
        authUser.setIsSupperAdmin(isSupperAdmin);

        // TODO: 2020/9/24 缓存数据

        //租户
        Tenant tenant = tenantMapper.selectById(user.getTenantId());
        authUser.setTenant(AuthTenantConverter.INSTANCE.toAuthTenant(tenant));

        //部门
        Department department = departmentMapper.selectById(user.getDepartmentId());
        authUser.setDepartment(AuthDepartmentConverter.INSTANCE.toAuthDepartment(department));

        //角色
        List<Role> roleList = roleService.getUserRoleList(user.getId());
        if (CollectionUtils.isNotEmpty(roleList)) {
            List<cn.com.glsx.auth.model.Role> list = new ArrayList<>(roleList.size());

            List<Long> roleIds = roleList.stream().map(Role::getId).collect(Collectors.toList());

            List<RoleBizDataPermission> roleBizDataPermissionList = roleService.getRoleBizDataPermission(roleIds);

            List<BizDataPermission> bizDataPermissionList = Lists.newArrayList();

            roleBizDataPermissionList.forEach(rbdp -> bizDataPermissionList.add(RoleBizDataPermissionConverter.INSTANCE.bo2simple(rbdp)));

            Map<Long, List<BizDataPermission>> bizDataPermissionMap = bizDataPermissionList.stream().collect(Collectors.groupingBy(BizDataPermission::getRoleId));

            roleList.forEach(dbRole -> {
                cn.com.glsx.auth.model.Role role = AuthRoleConverter.INSTANCE.toAuthRole(dbRole);

                List<BizDataPermission> dataPermissionList = bizDataPermissionMap.get(dbRole.getId());

                role.setBizDataPermissions(dataPermissionList);

                list.add(role);
            });
            authUser.setRoles(list);
        }
        //可见租户
        Set<Long> tenantIds = this.getRelationAuthTenantIds(user.getId(), roleList.get(0).getId());
        //可见根部门（租户）
        authUser.setVisibleTenantIds(tenantIds);
        //可见部门
        Set<Long> deptIds = this.getRelationAuthDeptIds(user.getId(), roleList.get(0).getId());
        //可见组织（部门）
        authUser.setVisibleDeptIds(deptIds);

        //超级管理员无需查询更详细的部门、用户等信息
        if (!isSupperAdmin) {
            //可见用户
            Set<Long> userIds = this.getRelationAuthUserIds(user.getId(), roleList.get(0).getId());
            //可见用户
            authUser.setVisibleCreatorIds(userIds);
        }
        return authUser;
    }

    /**
     * 获取用户根据数据权限范围内的可见租户id
     *
     * @param userId
     * @param roleId
     * @return
     */
    public Set<Long> getRelationAuthTenantIds(Long userId, Long roleId) {

        List<Tenant> deptList = departmentService.getUserRoleTenants(userId, roleId);

        return deptList.stream().map(Tenant::getId).collect(Collectors.toSet());
    }

    /**
     * 获取用户根据数据权限范围内的可见部门id
     *
     * @param userId
     * @param roleId
     * @return
     */
    public Set<Long> getRelationAuthDeptIds(Long userId, Long roleId) {

        List<Department> deptList = departmentService.getUserRoleDepartments(userId, roleId);

        return deptList.stream().map(Department::getId).collect(Collectors.toSet());
    }

    /**
     * 获取用户根据数据权限范围内的可见用户id
     *
     * @param userId
     * @param roleId
     * @return
     */
    public Set<Long> getRelationAuthUserIds(Long userId, Long roleId) {
        List<User> userList = departmentService.getUserRoleUsers(userId, roleId);

        return userList.stream().map(User::getId).collect(Collectors.toSet());
    }

    /**
     * 获取用户角色分配的功能菜单标识
     *
     * @param authUser
     * @return
     */
    public List<cn.com.glsx.auth.model.MenuPermission> getPermissionMenus(SyntheticUser authUser) {
        //菜单
        if (CollectionUtils.isNotEmpty(authUser.getRoles())) {
            List<Long> roleIds = authUser.getRoles().stream().map(cn.com.glsx.auth.model.Role::getRoleId).collect(Collectors.toList());

//            List<Menu> menuList = menuService.getMenuList(roleIds);
//            List<cn.com.glsx.auth.model.Menu> list = new ArrayList<>(roleList.size());
//            menuList.forEach(menu -> {
//                list.add(AuthMenuConverter.INSTANCE.toAuthMentu(menu));
//            });

            List<Long> menuIdList = roleMenuMapper.selectMenuIdsByRoleIds(roleIds);
            List<MenuPermission> permissionList = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(menuIdList)) {
                permissionList = menuService.getMenuPermissions(menuIdList);
            }
            List<cn.com.glsx.auth.model.MenuPermission> list = new ArrayList<>(permissionList.size());
            permissionList.forEach(mp -> {
                list.add(AuthMenuPermissionConverter.INSTANCE.toAuthMenuPermission(mp));
            });
            return list;
        }
        return Lists.newArrayList();
    }

    public List<SuperTreeModel> suitableSuperUsers(Long departmentId) {
        //部门
        Department department = departmentMapper.selectById(departmentId);
        if (department == null) {
            throw new UserCenterException("部门不存在");
        }

        //当前部门
        SuperTreeModel selfTreeModel = UserService.applySuperTreeModel(department);
        selfTreeModel.setRoot(true);
        selfTreeModel.setDisabled(true);

        //当前部门用户
        List<User> departmentUserList = userMapper.select(new User().setDepartmentId(departmentId));

        List<SuperTreeModel> departmentUserModels = departmentUserList.stream().map(UserService::applySuperTreeModel).collect(Collectors.toList());

        selfTreeModel.setChildren(departmentUserModels);

        //直接三级
        List<Organization> superiorOrgList = organizationMapper.selectSuperiorOrgsByDepths(departmentId, Lists.newArrayList(1, 2, 3));
        if (CollectionUtils.isEmpty(superiorOrgList)) {
            //无上级，返回自己
            return Lists.newArrayList(selfTreeModel);
        }

        //上一级
        Organization superiorOrg = superiorOrgList.get(0);

        SuperTreeModel superTreeModel = buildSuperTreeNode(superiorOrg, selfTreeModel);

        if (superiorOrgList.size() > 1) {
            //上二级
            Organization superiorOrg2 = superiorOrgList.get(1);

            SuperTreeModel superTreeModel2 = buildSuperTreeNode(superiorOrg2, superTreeModel);

            if (superiorOrgList.size() > 2) {
                //上三级
                Organization superiorOrg3 = superiorOrgList.get(2);

                SuperTreeModel superTreeModel3 = buildSuperTreeNode(superiorOrg3, superTreeModel2);

                return Lists.newArrayList(superTreeModel3);
            } else {
                return Lists.newArrayList(superTreeModel2);
            }
        } else {
            return Lists.newArrayList(superTreeModel);
        }
    }

    /**
     * 构建上级节点
     *
     * @param superiorOrg
     * @param supTreeModel
     * @return
     */
    private SuperTreeModel buildSuperTreeNode(Organization superiorOrg, SuperTreeModel supTreeModel) {
        Department superiorDepartment = departmentMapper.selectById(superiorOrg.getSuperiorId());

        SuperTreeModel superTreeModel = UserService.applySuperTreeModel(superiorDepartment);

        List<User> superiorUserList = userMapper.select(new User().setDepartmentId(superiorDepartment.getId()));

        List<SuperTreeModel> userChildren = superiorUserList.stream().map(UserService::applySuperTreeModel).collect(Collectors.toList());

        List<SuperTreeModel> superiorChildren = Lists.newArrayList();
        superiorChildren.addAll(userChildren);
        superiorChildren.add(supTreeModel);

        superTreeModel.setChildren(superiorChildren);

        return superTreeModel;
    }

    /**
     * 获取直接下级部门及用户账号
     *
     * @param departmentId
     * @return
     */
    public SuperTreeModel suitableSubUsers(Long departmentId) {
        Department department = departmentMapper.selectById(departmentId);
        if (department == null) {
            throw new UserCenterException("部门不存在");
        }

        //直接下级
        List<Organization> subOrgList = organizationMapper.selectSubOrgList(Lists.newArrayList(departmentId), 1);
        //部门ID
        List<Long> departmentIdList = subOrgList.stream().map(Organization::getSubId).collect(Collectors.toList());
        //部门
        List<Department> departmentList = departmentMapper.selectByIds(departmentIdList);
        //部门用户
        List<User> departmentUserList = userMapper.select(new User().setDepartmentId(departmentId));

        SuperTreeModel superTreeModel = UserService.applySuperTreeModel(department);
        List<SuperTreeModel> deptChildren = departmentList.stream().map(UserService::applySuperTreeModel).collect(Collectors.toList());
        List<SuperTreeModel> userChildren = departmentUserList.stream().map(UserService::applySuperTreeModel).collect(Collectors.toList());
        List<SuperTreeModel> children = Lists.newArrayList();
        children.addAll(deptChildren);
        children.addAll(userChildren);
        superTreeModel.setChildren(children);
        return superTreeModel;
    }

    public List<User> findByOrg(FindUserRequest request) {

        List<User> userList = Lists.newArrayList();

//        if (departmentIdList.contains(request.getOrgId())) {

        List<User> permissionUserList = organizationService.permissionUsers();

        List<Long> permissionUserIdList = permissionUserList.stream().map(User::getId).collect(Collectors.toList());

        UserSearch userSearch = new UserSearch().setDepartmentIds(request.getOrgIds()).setUserIds(permissionUserIdList);

        userList = userMapper.search(userSearch);

//        }
        return userList;
    }

    public List<User> findAllSubByOrg(FindUserRequest request) {

        List<User> deptUserList = Lists.newArrayList();

        if (CollectionUtils.isNotEmpty(request.getOrgIds())) {
            request.getOrgIds().forEach(orgId -> {
                List<User> userList = organizationService.permissionUsersByDepartmentId(orgId);
                deptUserList.addAll(userList);
            });
        }

        return deptUserList;
    }

    public User getSuperiorUserById(Long id) {
        return userMapper.selectSuperiorUserById(id);
    }

    public Set<Long> getSubordinateById(Long userId) {

        List<Role> roleList = roleService.getUserRoleList(userId);
        if (CollectionUtils.isEmpty(roleList)) {
            return Sets.newHashSet();
        }

        return this.getRelationAuthUserIds(userId, roleList.get(0).getId());
    }

    public List<cn.com.glsx.usercenter.api.dto.response.User> getSuperiorUserByIds(Collection<Long> userIds) {
        return userMapper.selectSuperiorUserByIds(userIds);
    }

    /**
     * @description: 同步创建用户
     * @author: wangxw1
     * @date: 2022/4/12 10:50
     * @param: [simpleAddUser]
     * @return: cn.com.glsx.usercenter.api.dto.response.User
     **/
    public cn.com.glsx.usercenter.api.dto.response.User syncAddSimpleUser(SimpleAddUser simpleAddUser) throws UserCenterException {
        User user = findByAccount(simpleAddUser.getAccount());
        if (user != null && !user.getTenantId().equals(simpleAddUser.getTenantId())) {
            throw new UserCenterException("账户已经存在其他租户");
        }

        UserBO userBO = new UserBO().setAccount(simpleAddUser.getAccount()).setUsername(simpleAddUser.getUserName())
                .setDepartmentId(simpleAddUser.getDepartmentId())
                .setRoleId(simpleAddUser.getRoleId())
                .setPassword(simpleAddUser.getPassword())
                .setPhoneNumber(simpleAddUser.getPhoneNum())
                .setEnableStatus(SysConstants.EnableStatus.enable.getCode())
                .setTenantId(simpleAddUser.getTenantId());
        log.info("账号：{}", JSONUtil.toJsonStr(userBO));
        if (user != null) {
            userBO.setId(user.getId());
            editCommonUser(userBO, simpleAddUser.getCreateId());
        } else {
            addCommonUser(userBO, simpleAddUser.getCreateId());
        }

        cn.com.glsx.usercenter.api.dto.response.User userDto = null;
        User userResult = findByAccount(simpleAddUser.getAccount());
        if (userResult != null) {
            userDto = new cn.com.glsx.usercenter.api.dto.response.User();
            userDto.setId(userResult.getId());
            userDto.setAccount(userResult.getAccount());
            userDto.setUsername(userResult.getUsername());
            userDto.setOrgId(userResult.getDepartmentId());
        }
        return userDto;
    }
}
