package cn.com.glsx.shield.modules.controller;

import cn.com.glsx.shield.modules.converter.TenantConverter;
import cn.com.glsx.shield.modules.entity.Tenant;
import cn.com.glsx.shield.modules.model.param.OrganizationSearch;
import cn.com.glsx.shield.modules.model.view.DepartmentDTO;
import cn.com.glsx.shield.modules.model.view.TenantDTO;
import cn.com.glsx.shield.modules.service.DepartmentService;
import com.github.pagehelper.PageInfo;
import com.glsx.plat.core.web.R;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author payu
 */
@Slf4j
@RestController
@RequestMapping(value = "/tenant")
@Api(value = "租户管理", tags = {"租户管理"})
public class TenantController {

    @Resource
    private DepartmentService departmentService;

    @GetMapping("/simplelist")
    public R simpleList(@RequestParam(required = false) Long roleId) {
        OrganizationSearch search = new OrganizationSearch()
                .setForPage(false)
                .setHasChild(false)
                .setHasUserNumber(false)
                .setRoleId(roleId);
        PageInfo<DepartmentDTO> pageInfo = departmentService.rootDepartmentList(search);
        return R.ok().data(pageInfo.getList());
    }

    @GetMapping("/rolepermission")
    public R rolePermission(@RequestParam(required = false) Long roleId) {
        List<TenantDTO> tenantDTOList = Lists.newArrayList();
        List<Tenant> tenantList = departmentService.getRolePermissionTenants(roleId);
        tenantList.forEach(tt -> {
            tenantDTOList.add(TenantConverter.INSTANCE.do2dto(tt));
        });
        return R.ok().data(tenantDTOList);
    }

}
