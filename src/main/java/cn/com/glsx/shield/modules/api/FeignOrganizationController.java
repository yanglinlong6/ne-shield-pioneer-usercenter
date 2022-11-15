package cn.com.glsx.shield.modules.api;

import cn.com.glsx.shield.modules.model.UserOrgModel;
import cn.com.glsx.shield.modules.model.UserSuperiorOrgModel;
import cn.com.glsx.shield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.shield.modules.model.view.DepartmentDTO;
import cn.com.glsx.shield.modules.service.DepartmentService;
import cn.com.glsx.shield.modules.service.OrganizationService;
import cn.com.glsx.usercenter.api.dto.request.FindOrgRequest;
import cn.com.glsx.usercenter.api.dto.response.Organization;
import com.glsx.plat.common.model.TreeModel;
import com.glsx.plat.core.web.R;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * feign组织机构服务接口
 */
@Slf4j
@Api(value = "用户组织微服务API", tags = {"用户组织微服务API"})
@RestController
@RequestMapping(value = "/forg")
public class FeignOrganizationController implements cn.com.glsx.usercenter.api.feign.OrganizationService {

    @Autowired
    private DepartmentService departmentService;

    @Resource
    private OrganizationService organizationService;

    @Override
    public R<List<Organization>> findOrgs(@RequestBody FindOrgRequest request) {
        List<Organization> list = Lists.newArrayList();
        List<cn.com.glsx.shield.modules.entity.Department> dbDeptList = departmentService.findByOrg(request);
        dbDeptList.forEach(dept -> {
            Organization org = new Organization();
            org.setId(dept.getId());
            org.setOrgName(dept.getDepartmentName());
            list.add(org);
        });
        return R.ok().data(list);
    }

    @Override
    public R<List<Organization>> getTenantPathsByUserId(@RequestParam("userId") Long userId) {
        List<Organization> list = Lists.newArrayList();
        List<cn.com.glsx.shield.modules.entity.Department> dbDeptList = departmentService.getTenantPathsByUserId(userId);
        dbDeptList.forEach(dept -> {
            Organization org = new Organization();
            org.setId(dept.getId());
            org.setOrgName(dept.getDepartmentName());
            list.add(org);
        });
        return R.ok().data(list);
    }

    @Override
    public R<List<Organization>> getTenantPathsByUserIds(@RequestBody Collection<Long> userIds) {
        List<Organization> list = Lists.newArrayList();
        List<UserSuperiorOrgModel> modelList = departmentService.getTenantByUserIds(userIds);
        modelList.forEach(model -> {
            Organization org = new Organization();
            org.setId(model.getOrgId());
            org.setOrgName(model.getSuperiorNames());
            org.setUserId(model.getUserId());
            list.add(org);
        });
        return R.ok().data(list);
    }

    @Override
    public R<List<Organization>> getRootOrganizations() {
        List<Organization> list = Lists.newArrayList();
        List<DepartmentDTO> subOrgList = organizationService.simpleList(null);
        subOrgList.forEach(dept -> {
            Organization org = new Organization();
            org.setId(dept.getId());
            org.setOrgName(dept.getDepartmentName());
            list.add(org);
        });
        return R.ok().data(list);
    }

    @Override
    public R<List<Organization>> getSubOrganizations(@RequestParam("orgId") Long orgId) {
        List<Organization> list = Lists.newArrayList();
        List<DepartmentDTO> subOrgList = organizationService.simpleList(orgId);
        subOrgList.forEach(dept -> {
            Organization org = new Organization();
            org.setId(dept.getId());
            org.setOrgName(dept.getDepartmentName());
            list.add(org);
        });
        return R.ok().data(list);
    }

    @Override
    public R<List<Organization>> getOrgsByUserIds(@RequestParam("userIds") Collection<Long> userIds) {
        List<Organization> list = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(userIds)) {
            List<cn.com.glsx.shield.modules.entity.Department> localDeptList = organizationService.reverseOrgsByUserIds(userIds);
            localDeptList.forEach(dept -> {
                Organization org = new Organization();
                org.setId(dept.getId());
                org.setOrgName(dept.getDepartmentName());
                list.add(org);
            });
        }
        return R.ok().data(list);
    }

    @Override
    public R<List<Organization>> getAllUserOrgsByUserIds(@RequestParam("userIds") Collection<Long> userIds) {
        List<Organization> list = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(userIds)) {
            List<UserOrgModel> modelList = organizationService.getAllUserOrgs(userIds);
            modelList.forEach(uom -> {
                Organization org = new Organization();
                org.setId(uom.getOrgId());
                org.setOrgName(uom.getOrgName());
                org.setUserId(uom.getUserId());
                list.add(org);
            });
        }
        return R.ok().data(list);
    }

    @Override
    public R<List<Organization>> getSpecialLevelOrganizations(Integer level) {
        List<Organization> list = Lists.newArrayList();
        List<cn.com.glsx.shield.modules.entity.Department> levelDeptList = organizationService.getSpecialLevelDepartments(level);
        levelDeptList.forEach(dept -> {
            Organization org = new Organization();
            org.setId(dept.getId());
            org.setOrgName(dept.getDepartmentName());
            list.add(org);
        });
        return R.ok().data(list);
    }

    @Override
    public R<Set<Long>> getAllSubOrgIds() {
        Set<Long> levelDeptList = organizationService.getAllSubOrgIds();
        return R.ok().data(levelDeptList);
    }

    @Override
    public R orgTree(@RequestBody FindOrgRequest request) {
        OrgTreeSearch search = new OrgTreeSearch();
        search.setOrgName(request.getOrgName());
        search.setTenantIds(request.getTenantIds());
        search.setOrgIds(request.getOrgIds());
        search.setSupOrgIds(request.getSupOrgIds());
        search.setSubOrgIds(request.getSubOrgIds());
        search.setUserIds(request.getUserIds());
        search.setReturnTrees(false);
        search.setHasUserLeaf(request.getHasUserLeaf());
        List<? extends TreeModel> list = organizationService.orgTree(search);
        log.info("组织数{}", list.size());
        return R.ok().data(list);
    }

}
