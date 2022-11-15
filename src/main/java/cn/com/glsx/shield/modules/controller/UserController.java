package cn.com.glsx.shield.modules.controller;

import cn.com.glsx.auth.model.Department;
import cn.com.glsx.auth.model.SyntheticUser;
import cn.com.glsx.auth.model.Tenant;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.shield.modules.model.export.UserExport;
import cn.com.glsx.shield.modules.model.param.ChangePasswordBO;
import cn.com.glsx.shield.modules.model.param.UserBO;
import cn.com.glsx.shield.modules.model.param.UserSearch;
import cn.com.glsx.shield.modules.model.view.SuperTreeModel;
import cn.com.glsx.shield.modules.model.view.UserDTO;
import cn.com.glsx.shield.modules.service.TransDataService;
import cn.com.glsx.shield.modules.service.UserService;
import cn.com.glsx.shield.modules.service.ZhongliAccountService;
import com.github.pagehelper.PageInfo;
import com.glsx.plat.common.annotation.SysLog;
import com.glsx.plat.common.enums.OperateType;
import com.glsx.plat.common.utils.DateUtils;
import com.glsx.plat.context.utils.validator.AssertUtils;
import com.glsx.plat.context.utils.validator.group.AddGroup;
import com.glsx.plat.core.web.R;
import com.glsx.plat.office.excel.EasyExcelUtils;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 单体应用Controller处理统一返回体
 *
 * @author payu
 */
@Slf4j
@RestController
@RequestMapping(value = "/user")
@Api(value = "用户管理", tags = {"用户管理"})
public class UserController {

    private final static String MODULE = "用户管理";

    @Resource
    private UserService userService;

    @Resource
    private TransDataService transDataService;

    @Resource
    private ZhongliAccountService zhongliAccountService;

    /**
     * 广本账号
     *
     * @return
     */
    @GetMapping("/creatGbAccount")
    public R creatGbAccount() {
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
        //根组织
        //建立组织关系（总部-大区-省-市-店）
        transDataService.AddOrgAll();
        //建立总部账号

        //广汽本田管理员角色id

        Long tenant_id = transDataService.testAddAccount1();
        log.info("tenant_id=======:" + tenant_id);
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
        return R.ok();
    }

    /**
     * 仲利账号
     *
     * @return
     */
    @GetMapping("/creatZhongliAccount")
    public R creatZhongliAccount(@RequestParam String tenantName) {
        zhongliAccountService.buildZhongliDepartment();
        zhongliAccountService.buildZhongliAccount(tenantName);
        return R.ok();
    }

    @GetMapping("/search")
    public R search(UserSearch search) {
        PageInfo<UserDTO> pageInfo = userService.search(search);
        return R.ok().putPageData(pageInfo);
    }

    @GetMapping(value = "/export")
    public void export(HttpServletResponse response, UserSearch search) throws Exception {
        List<UserExport> list = userService.export(search);
        EasyExcelUtils.writeExcel(response, list, "用户_" + DateUtils.formatSerial(new Date()), "Sheet1", UserExport.class);
    }

    @SysLog(module = MODULE, action = OperateType.ADD)
    @PostMapping(value = "/add")
    public R add(@RequestBody @Validated(AddGroup.class) UserBO userBO) {
//        User user = UserConverter.INSTANCE.bo2do(userBO);
//        String password = userBO.getPassword();
//        if (StringUtils.isBlank(password) || !RegexUtil.regexPwd(password)) {
//            return R.error(SystemMessage.ARGS_ERROR.getCode(), "密码格式错误");
//        }
        //AssertUtils.isNull(userBO.getPassword(), "密码不能为空");
        userService.addUser(userBO);
        return R.ok();
    }

    @SysLog(module = MODULE, action = OperateType.EDIT)
    @PostMapping(value = "/edit")
    public R edit(@RequestBody @Validated UserBO userBO) {
        AssertUtils.isNull(userBO.getId(), "ID不能为空");
//        User user = UserConverter.INSTANCE.bo2do(userBO);
        userService.editUser(userBO);
        return R.ok();
    }

    @GetMapping(value = "/info")
    public R info(@RequestParam("id") Long id) {
        UserDTO user = userService.userInfo(id);
        return R.ok().data(user);
    }

    //@RequireFunctionPermissions(permissionType = FunctionPermissionType.USER_DELETE)
    @SysLog(module = MODULE, action = OperateType.DELETE)
    @GetMapping(value = "/delete")
    public R delete(@RequestParam("id") Long id) {
        userService.logicDeleteById(id);
        return R.ok();
    }

    @SysLog(module = MODULE, value = "重置密码", action = OperateType.EDIT)
    @GetMapping(value = "/resetPassword")
    public R resetPassword(@RequestParam("id") Long id) {
        userService.resetPassword(id);
        return R.ok();
    }

    @SysLog(module = MODULE, value = "重置密码", action = OperateType.EDIT)
    @PostMapping(value = "/batchResetPassword")
    public R batchResetPassword(@RequestBody Long[] ids) {
        Arrays.asList(ids).forEach(id -> {
            userService.resetPassword(id);
        });
        return R.ok();
    }

    @SysLog(module = MODULE, value = "修改密码", action = OperateType.EDIT)
    @PostMapping(value = "/changePassword")
    public R changePassword(@RequestBody @Validated ChangePasswordBO passwordBO) {
        userService.changePassword(passwordBO);
        return R.ok();
    }

    @GetMapping("/suitableSuperUsers")
    public R suitableSuperUsers(@RequestParam("departmentId") Long departmentId) {
        List<SuperTreeModel> superTreeModels = userService.suitableSuperUsers(departmentId);
        return R.ok().data(superTreeModels);
    }

    @GetMapping("/suitableSubUsers")
    public R suitableSubUsers(@RequestParam("departmentId") Long departmentId) {
        SuperTreeModel superTreeModels = userService.suitableSubUsers(departmentId);
        return R.ok().data(superTreeModels);
    }

    @GetMapping("/permAllUsers")
    public R permissionUsers() {
        List<UserDTO> userDTOList = userService.permissionUsers();
        return R.ok().data(userDTOList);
    }

    @GetMapping("/permDeptUsers")
    public R permissionUsers(@RequestParam("departmentId") Long departmentId) {
        List<UserDTO> userDTOList = userService.permissionUsersByDepartmentId(departmentId);
        return R.ok().data(userDTOList);
    }

}
