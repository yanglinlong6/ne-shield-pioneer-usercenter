package cn.com.glsx.usercenter.modul;

import cn.com.glsx.Application;
import cn.com.glsx.shield.modules.api.ApiUserController;
import cn.com.glsx.usercenter.api.dto.request.SimpleAddUser;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiUserControllerTest {

    @Autowired
    private ApiUserController apiUserController;

    @Before
    public void runBeforeTestMethod() {
        /*SyntheticUser user = new SyntheticUser();
        user.setUserId(8160L);
        user.setUsername("仲利国际租赁有限公司");
        user.setIsSupperAdmin(false);
        user.setAccount("zlgj");

        // 山东顺骋租户数据
        Tenant tenant = new Tenant();
        tenant.setTenantId(86L);
        tenant.setTenantName("仲利国际");
        user.setTenant(tenant);

        Department department = new Department();
        department.setDeptId(4602L);
        department.setDepartmentName("仲利国际");
        user.setDepartment(department);
        List<Role> roleList = new ArrayList<>();
        Role role = new Role();
        role.setRoleId(74L);
        role.setRoleName("仲利国际租赁有限公司");
        roleList.add(role);
        user.setRoles(roleList);
        ShieldContextHolder.setUser(user);*/
    }

    @Test
    public void test() {
        SimpleAddUser simpleAddUser = new SimpleAddUser();
        simpleAddUser.setAccount("wangxw10000003");
        simpleAddUser.setUserName("xinwei10003");
        simpleAddUser.setDepartmentId(4808L);
        simpleAddUser.setRoleId(81L);
        simpleAddUser.setPassword("ABC123");
        simpleAddUser.setPhoneNum("13800001001");
        simpleAddUser.setTenantId(86L);
        simpleAddUser.setCreateId(8160L);
        apiUserController.addSimpleUser(simpleAddUser);
    }

}
