package cn.com.glsx.test;

import cn.com.glsx.Application;
import cn.com.glsx.auth.model.Department;
import cn.com.glsx.auth.model.SyntheticUser;
import cn.com.glsx.auth.model.Tenant;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.shield.modules.entity.Menu;
import cn.com.glsx.shield.modules.entity.Role;
import cn.com.glsx.shield.modules.entity.User;
import cn.com.glsx.shield.modules.model.LoginBO;
import cn.com.glsx.shield.modules.model.param.ChangePasswordBO;
import cn.com.glsx.shield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.shield.modules.model.param.UserBO;
import cn.com.glsx.shield.modules.model.param.UserSearch;
import cn.com.glsx.shield.modules.model.view.DepartmentDTO;
import cn.com.glsx.shield.modules.model.view.SuperTreeModel;
import cn.com.glsx.shield.modules.model.view.UserDTO;
import cn.com.glsx.shield.modules.service.*;
import cn.com.glsx.shield.modules.service.permissionStrategy.PermissionStrategy;
import cn.com.glsx.usercenter.api.dto.request.FindOrgRequest;
import cn.com.glsx.usercenter.api.dto.response.Organization;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.glsx.plat.common.utils.StringUtils;
import com.glsx.plat.jwt.base.ComJwtUser;
import com.glsx.plat.jwt.util.JwtUtils;
import com.glsx.plat.redis.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserApplicationTests {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MenuService menuService;

    @Resource
    private HashedCredentialsMatcher hcm;

    @Autowired
    private Map<String, PermissionStrategy> permissionStrategyMap;

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private TransDataService transDataService;
    @Autowired
    private DepartmentService departmentService;
    @Resource
    private JwtUtils<ComJwtUser> jwtUtils;

    @Autowired
    private ZhongliAccountService zhongliAccountService;

    // Should rename to @BeforeTestMethod
    // e.g. Creating an similar object and share for all @Test
    @Before
    public void runBeforeTestMethod() {
        System.out.println("@Before - runBeforeTestMethod");

        SyntheticUser user = new SyntheticUser();
        user.setUserId(1L);
        user.setIsSupperAdmin(true);
        user.setAccount("admin");

        Tenant tenant = new Tenant();
        tenant.setTenantId(1L);
        user.setTenant(tenant);

        Department dept = new Department();
        dept.setDeptId(1L);
        user.setDepartment(dept);

        List<cn.com.glsx.auth.model.Role> roles = Lists.newArrayList();
        cn.com.glsx.auth.model.Role role = new cn.com.glsx.auth.model.Role();
        role.setRoleId(1L);
        role.setRolePermissionType(4);
        role.setRoleVisibility(1);
        roles.add(role);
        user.setRoles(roles);
        ShieldContextHolder.setUser(user);
    }

    // Should rename to @AfterTestMethod
    @After
    public void runAfterTestMethod() {
        System.out.println("@After - runAfterTestMethod");
    }

    @Test
    public void testOrg() {
        OrgTreeSearch search = new OrgTreeSearch();
//        search.setOrgName("部");
//        List list1 = organizationService.fullOrgTree(search);
//        System.out.println(JSON.toJSONString(list1));
//
        List list2 = organizationService.orgTree(search);
        System.out.println(JSON.toJSONString(list2));

        List<DepartmentDTO> subOrgList = organizationService.simpleList(null);
        System.out.println(JSON.toJSONString(subOrgList));
//
        List<DepartmentDTO> subOrgList1 = organizationService.simpleList(157L);
        System.out.println(JSON.toJSONString(subOrgList1));
//
//        List<DepartmentDTO> subOrgList2 = organizationService.simpleList(158L);
//        System.out.println(JSON.toJSONString(subOrgList2));
//
//        List<DepartmentDTO> subOrgList3 = organizationService.simpleList(161L);
//        System.out.println(JSON.toJSONString(subOrgList3));
//
//        List<DepartmentDTO> cityDeptList = Lists.newArrayList();
//
//        List<Long> departmentIdList = subOrgList3.stream().map(DepartmentDTO::getId).collect(Collectors.toList());
//        departmentIdList.forEach(id -> {
//            List<DepartmentDTO> subOrgList4 = organizationService.simpleList(id);
//            cityDeptList.addAll(subOrgList4);
//        });
//
//        System.out.println(JSON.toJSONString(cityDeptList));
//
//        List<SuperTreeModel> superTreeModels = userService.suitableSuperUsers(162L);
//        System.out.println(JSON.toJSONString(superTreeModels));
//
//        SuperTreeModel superTreeModel = userService.suitableSubUsers(162L);
//        System.out.println(JSON.toJSONString(superTreeModel));
//
//
//        List<cn.com.glsx.shield.modules.entity.Department> departmentList = organizationService.permissionDepartments();
//        System.out.println(JSON.toJSONString(departmentList));
//
//        List<cn.com.glsx.shield.modules.entity.User> userList = organizationService.permissionUsers();
//        System.out.println(JSON.toJSONString(userList));
//
//        List<UserDTO> userDTOList = userService.permissionUsers();
//        System.out.println(JSON.toJSONString(userDTOList));
    }

    @Test
    public void testRole() {
        List<Role> list = roleService.getUserRoleList(1L);
        System.out.println(JSON.toJSONString(list));
    }

    @Test
    public void testMenu() {
        List<Long> ids = new ArrayList<>();
        ids.add(1L);
        List list = menuService.getMenuTree(ids);
        System.out.println(JSON.toJSONString(list));
    }

    @Test
    public void testMenuNo() {
        Long no = menuService.generateMenuNo(null);
        System.out.println(no);
    }

    @Test
    public void testUser() {
        SyntheticUser user = userService.getSyntheticUserById(1L);
        System.out.println(JSON.toJSONString(user));
        userService.addUser(new UserBO().setAccount("rrew").setUsername("桃桃桃").setDepartmentId(1L).setRoleId(1L).setPassword("123456"));

        UserSearch search = new UserSearch();
        search.setPageNumber(1);
        search.setPageSize(10);
        PageInfo<UserDTO> pageInfo = userService.search(search);
        System.out.println(JSON.toJSONString(pageInfo));
    }

    @Test
    public void testSuitableSuperUsers() {
        List<SuperTreeModel> superTreeModels = userService.suitableSuperUsers(2162L);
        System.out.println(JSON.toJSONString(superTreeModels));
    }

    @Test
    public void testPassword() {
        String password = "123456";
        String salt = StringUtils.generateRandomCode(false, 4);
        SimpleHash hash = new SimpleHash(hcm.getHashAlgorithmName(), password, salt, hcm.getHashIterations());

        //加密入库的密码
        String epassword = hash.toString();

        System.out.println(salt);
        System.out.println(epassword);

        String account = "admin";

        UsernamePasswordToken token = new UsernamePasswordToken(account, password);

        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(account, epassword, ByteSource.Util.bytes(salt), account);

        boolean matchFlag = hcm.doCredentialsMatch(token, authenticationInfo);

        System.out.println(matchFlag);
    }

    @Test
    public void verifyPassword() {
        LoginBO loginBO = new LoginBO();
        loginBO.setAccount("admin");
        loginBO.setPassword("123456");

        User user = userService.findByAccount(loginBO.getAccount());

        userService.verify(user, loginBO.getPassword());
    }

    @Test
    public void changePassword() {
        ChangePasswordBO passwordBO = new ChangePasswordBO();
        passwordBO.setUserId(1L);
        passwordBO.setOldPassword("admin");
        passwordBO.setNewPassword("123456");
        userService.changePassword(passwordBO);
    }

    @Test
    public void testMenuDept() {
        Integer depth = menuService.getMenuDepth(new Menu().setParentId(701010201L), 0);
        System.out.println(depth);
    }

    @Test
    public void testCheckedMenus() {
        Set<Long> list = menuService.getMenuCheckedIds(19L);
        log.info(list.toString());
    }

    @Test
    public void testRedisGet() {
        Object baseCity = redisUtils.get("baseCity");
        System.out.println(baseCity);
    }

    @Test
    public void testAddOrg() {
        //建立组织关系（总部-大区-省-市-店）
        transDataService.AddOrgAll();
        //建立总部账号

        //广汽本田管理员角色id
        Long tenant_id = transDataService.testAddAccount1();
        System.out.println("tenant_id==========" + tenant_id);
        //建立大区账号
        String AreaNmae = "大区";
        transDataService.testAddAccount2(AreaNmae, tenant_id);
        //建立省（系）账号
        String provinceNmae = "大区";
        transDataService.testAddAccount3(provinceNmae, tenant_id);
        //建立巡回员账号
        String cityNmae = "省";
        transDataService.testAddAccount4(cityNmae, tenant_id);
        //建立特约店账号
        String StoreName = "大区";
        transDataService.testAddAccount5(StoreName, tenant_id);
    }

    @Test
    public void testFindDept() {
        List<cn.com.glsx.shield.modules.entity.Department> dbDeptList = departmentService.findByOrg(new FindOrgRequest().setOrgName("广联赛讯"));
        dbDeptList.forEach(dept -> {
            Organization org = new Organization();
            org.setId(dept.getId());
            org.setOrgName(dept.getDepartmentName());
        });
    }

    @Test
    public void verifyToken() {
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJwYXlsb2FkTWFwIjp7ImJlbG9uZyI6IuW5v-iBlOi1m-iuryIsImFwcGxpY2F0aW9uIjoiZ2xzeC1uZS1zaGllbGQtdXNlcmNlbnRlciIsImNsYXp6IjoiY29tLmdsc3gucGxhdC5qd3QuYmFzZS5Db21Kd3RVc2VyIiwidXNlcklkIjoiMSIsImFjY291bnQiOiJhZG1pbiIsInRlbmFudCI6IjEiLCJqd3RJZCI6Imdsc3gtbmUtc2hpZWxkLXVzZXJjZW50ZXI6MzM0MDZiZWEtNGU3Ny00OTA0LWFlOGEtZTc2YWVmZjdlOTY0X0pXVC1TRVNTSU9OLTEifSwic3ViIjoiYWRtaW4iLCJleHAiOjE2MjU4NDA2ODcsImlhdCI6MTYyNTgxMTg4NywianRpIjoiZ2xzeC1uZS1zaGllbGQtdXNlcmNlbnRlcjozMzQwNmJlYS00ZTc3LTQ5MDQtYWU4YS1lNzZhZWZmN2U5NjRfSldULVNFU1NJT04tMSJ9.gbWNf86PzrJOvepjRb3aeTJM5ViYoDVaBIjsDdJbSUk";
        boolean flag = jwtUtils.verifyToken(token);
        System.out.println(flag);
    }

    @Test
    public void testZhongliAccountSystem() {
        zhongliAccountService.buildZhongliDepartment();
        zhongliAccountService.buildZhongliAccount("仲利国际");
    }
}