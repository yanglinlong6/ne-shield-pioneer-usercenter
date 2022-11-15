package cn.com.glsx.shield.modules.service;

import cn.com.glsx.shield.common.exception.UserCenterException;
import cn.com.glsx.shield.modules.entity.*;
import cn.com.glsx.shield.modules.mapper.*;
import cn.com.glsx.shield.modules.model.param.OrganizationBO;
import cn.com.glsx.shield.modules.model.param.UserBO;
import cn.com.glsx.shield.modules.model.zhongli.ZhongliAccountModel;
import cn.com.glsx.shield.modules.model.zhongli.ZhongliDepartmentModel;
import cn.hutool.json.JSONUtil;
import com.glsx.plat.core.enums.SysConstants;
import com.glsx.plat.exception.SystemMessage;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ZhongliAccountService {

    @Resource
    private TenantMapper tenantMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private OrganizationMapper organizationMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private OrganizationService organizationService;

    @Resource
    private ZhongliAccountMapper zhongliAccountMapper;

    @Resource
    private ZhongliDepartmentMapper zhongliDepartmentMapper;


    public void buildZhongliDepartment() {

        List<ZhongliDepartmentModel> deptModelList = zhongliDepartmentMapper.selectAllLevel();

        Map<String, List<ZhongliDepartmentModel>> oneLevelMap = deptModelList.stream().collect(Collectors.groupingBy(ZhongliDepartmentModel::getOneLevel));
        Map<String, List<ZhongliDepartmentModel>> twoLevelMap = deptModelList.stream().collect(Collectors.groupingBy(ZhongliDepartmentModel::getTwoLevel));
        Map<String, Map<String, List<ZhongliDepartmentModel>>> threeFourLevelMap = deptModelList.stream()//.filter(o -> o.getThreeLevel() != null)
                .collect(Collectors.groupingBy(ZhongliDepartmentModel::getTwoLevel, Collectors.groupingBy(ZhongliDepartmentModel::getThreeLevel)));

        //一级
        oneLevelMap.keySet().forEach(oneName -> {
            OrganizationBO rootNode = new OrganizationBO();
            rootNode.setDepartmentName(oneName);
            rootNode.setEnableStatus(SysConstants.EnableStatus.enable.getCode());
            rootNode.setOrderNum(8);
            Organization one = organizationService.addRootOrganization(rootNode);

            String oneLevel = "一级";
            log.info("{}：{}，入库ID:{}", oneLevel, oneName, one.getSubId());

            //二级
            twoLevelMap.keySet().forEach(twoName -> {
                Organization two = addSubNode(twoName, one.getSubId());
                String twoLevel = "二级";
                log.info("\t{}：{}，入库ID:{}", twoLevel, twoName, two.getSubId());

                Map<String, List<ZhongliDepartmentModel>> threeLevelMap = threeFourLevelMap.get(twoName);
                threeLevelMap.forEach((threeName, v) -> {
                    if (threeName.contains("仲利已结清")) {
                        return;
                    }

                    Organization three = addSubNode(threeName, two.getSubId());
                    log.info("\t\t三级：{}，入库ID:{}", threeName, three.getSubId());
                    v.forEach(model -> {
                        if (model.getFourLevel().contains("仲利已结清")) {
                            return;
                        }
                        Organization four = addSubNode(model.getFourLevel(), three.getSubId());
                        log.info("\t\t\t四级：{}，入库ID:{}", model.getFourLevel(), four.getSubId());
                    });
                });
            });
        });
    }

    public Organization addSubNode(String nodeName, Long superiorId) {
        OrganizationBO node = new OrganizationBO();
        node.setDepartmentName(nodeName);
        node.setSuperiorId(superiorId);
        node.setEnableStatus(SysConstants.EnableStatus.enable.getCode());
        node.setOrderNum(1);
        Organization org = organizationService.addNodeToOrganization(node);
        return org;
    }

    public void buildZhongliAccount(String tenantName) {

        Tenant tenant = tenantMapper.selectOne(new Tenant().setTenantName(tenantName));

        if (tenant == null) {
            throw new UserCenterException(SystemMessage.FAILURE.getCode(), tenantName + " 租户不存在");
        }

        Department department = departmentMapper.selectRootDepartmentByTenantId(tenant.getId());

        List<ZhongliAccountModel> accountModelList = zhongliAccountMapper.selectByLevel(null);
        Map<String, List<ZhongliAccountModel>> accountMap = accountModelList.stream().collect(Collectors.groupingBy(ZhongliAccountModel::getLevel));

        String oneLevel = "一级";
        Map<String, Department> departmentMap0 = getDepartmentMap(Lists.newArrayList(department.getId()), 0);
        buildAccount(oneLevel, departmentMap0, accountMap);

        String twoLevel = "二级";
        Map<String, Department> departmentMap1 = getDepartmentMap(Lists.newArrayList(department.getId()), 1);
        buildAccount(twoLevel, departmentMap1, accountMap);

        String threeLevel = "三级";
        Map<String, Department> departmentMap2 = getDepartmentMap(Lists.newArrayList(department.getId()), 2);
        buildAccount(threeLevel, departmentMap2, accountMap);

        String fourLevel = "四级";
        //Map<String, Department> departmentMap3 = getDepartmentMap(Lists.newArrayList(department.getId()), 3);
        //buildAccount(fourLevel, departmentMap3, accountMap);
    }

    public Map<String, Department> getDepartmentMap(List<Long> superiorIds, Integer depth) {
        List<Organization> organizations = organizationMapper.selectSubOrgList(superiorIds, depth);
        List<Long> organizationIdList = organizations.stream().map(Organization::getSubId).collect(Collectors.toList());
        List<Department> levelDept = departmentMapper.selectByIds(organizationIdList);
        Map<String, Department> departmentMap = levelDept.stream().collect(Collectors.toMap(Department::getDepartmentName, d -> d));
        return departmentMap;
    }

    public void buildAccount(String level, Map<String, Department> departmentMap, Map<String, List<ZhongliAccountModel>> accountMap) {
        List<ZhongliAccountModel> levelAccount = accountMap.get(level);

        log.info(JSONUtil.toJsonStr(departmentMap));
        log.info(JSONUtil.toJsonStr(accountMap));

        if (CollectionUtils.isNotEmpty(levelAccount)) {
            levelAccount.forEach(account -> {
                Role role = roleService.getRoleByName(account.getRoleName());
                if (role == null) {
                    throw new UserCenterException(SystemMessage.FAILURE.getCode(), "需手动创建角色 " + account.getRoleName());
                }

                User user = userService.findByAccount(account.getAccount());

                UserBO userBO = new UserBO().setAccount(account.getAccount()).setUsername(account.getUsername())
                        .setDepartmentId(departmentMap.get(account.getDepartment()).getId())
                        .setRoleId(role.getId())
                        .setPassword("ABC123")
                        .setPhoneNumber("13800138000")
                        .setEnableStatus(SysConstants.EnableStatus.enable.getCode());
                log.info("账号：{}", JSONUtil.toJsonStr(userBO));
                if (user != null) {
                    userService.editUser(userBO);
                } else {
                    userService.addUser(userBO);
                }
            });
        } else {
            log.info("{}没有账号", level);
        }
    }

}
