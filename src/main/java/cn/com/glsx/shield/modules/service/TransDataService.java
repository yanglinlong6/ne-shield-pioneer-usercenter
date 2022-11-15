package cn.com.glsx.shield.modules.service;

import cn.com.glsx.admin.common.constant.Constants;
import cn.com.glsx.shield.modules.entity.EshieldTransdata;
import cn.com.glsx.shield.modules.entity.User;
import cn.com.glsx.shield.modules.mapper.DepartmentMapper;
import cn.com.glsx.shield.modules.mapper.EshieldTransdataMapper;
import cn.com.glsx.shield.modules.model.param.DepartmentSearch;
import cn.com.glsx.shield.modules.model.param.OrganizationBO;
import cn.com.glsx.shield.modules.model.param.UserBO;
import com.alibaba.druid.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * （临时类）
 * 数据导入添加账号
 *
 * @author fengzhi
 * 针对7级架构，对应不同角色：
 * A）全国-对应超管，运营人员
 * B）大区-对应系长，一期平台暂无对应角色分配
 * C）省-灵活处理，暂无角色分配，方便分配系长管辖范围
 * D）市-对应巡回员角色
 * E）区（县）-对应特约店角色
 * F）特殊情况：门店之下有分店，则门店可以看到名下所有分店，对应“特约店级别”中的“一/二级”
 */
@Slf4j
@Service
public class TransDataService {
    @Value("${transdata.role.root}")
    private Long roleRoot;

    @Value("${transdata.role.area}")
    private Long roleArea;

    @Value("${transdata.role.province}")
    private Long roleProvince;

    @Value("${transdata.role.city}")
    private Long roleCity;

    @Value("${transdata.role.store}")
    private Long roleStore;

    @Value("${transdata.org.root.name}")
    private String rootName;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private EshieldTransdataMapper transdataMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    public void AddOrgAll() {
        log.info("role参数：" + roleRoot + "==" + roleArea + "==" + roleProvince + "==" + roleCity + "==" + roleStore + "===" + rootName);
        AtomicInteger count = new AtomicInteger();

        List<EshieldTransdata> list = transdataMapper.selectByShopType("大区");
        //全部有完整组织的特约店
        List<Integer> storelist = new ArrayList<>();

        for (EshieldTransdata data : list) {
            String typeVals = data.getTypeval();
            if (StringUtils.isEmpty(typeVals)) {
                continue;
            }

            String[] split = typeVals.split(",");

            for (String provinceID : split) {
                //省
                EshieldTransdata provinceData = transdataMapper.selectByUserId(Long.valueOf(provinceID)).get(0);
                String typeValsClass = provinceData.getTypeval();
                if (StringUtils.isEmpty(typeValsClass)) {
                    continue;
                }
                String[] splitClass = typeValsClass.split(",");

                for (String cityId : splitClass) {
                    //巡回员
                    EshieldTransdata cityData = transdataMapper.selectByUserId(Long.valueOf(cityId)).get(0);
                    String storeValsClass = cityData.getTypeval();
                    if (StringUtils.isEmpty(storeValsClass)) {
                        continue;
                    }
                    String[] storeClass = storeValsClass.split(",");
                    for (String classid : storeClass) {
                        //特约店
                        EshieldTransdata storeData = transdataMapper.selectByClassId(Integer.valueOf(classid));
                        storelist.add(storeData.getId());
                        count.getAndIncrement();
                    }
                }

            }
        }

        log.info("AddOrgAll完整组织特约店数量：" + count.get());

        //所有有上级的特约店，建立完整组织
        storelist.stream().forEach(dataid -> {
            EshieldTransdata storeTransdata = transdataMapper.selectByPrimaryKey(dataid);
            String rolename = storeTransdata.getRolename();

            if (!StringUtils.isEmpty(rolename)) {
                String[] orgs = rolename.split("/");
                cn.com.glsx.shield.modules.entity.Department org0dept = addDepartment(rootName, null, null);
                Long rootdepartmentID0 = org0dept.getId();
                Long telnentID = org0dept.getTenantId();

                String org1 = orgs[1] + "大区";
                cn.com.glsx.shield.modules.entity.Department org1dept = addDepartment(org1, rootdepartmentID0, telnentID);
                Long rootdepartmentID1 = org1dept.getId();

                String org2 = orgs[2] + "省";
                cn.com.glsx.shield.modules.entity.Department org2dept = addDepartment(org2, rootdepartmentID1, telnentID);
                Long rootdepartmentID2 = org2dept.getId();

                String org3 = orgs[3] + "市";
                cn.com.glsx.shield.modules.entity.Department org3dept = addDepartment(org3, rootdepartmentID2, telnentID);
                Long rootdepartmentID3 = org3dept.getId();

                String org5 = storeTransdata.getUserremark();
                cn.com.glsx.shield.modules.entity.Department org5dept = addDepartment(org5, rootdepartmentID3, telnentID);
            }
        });
    }

    /**
     * 广汽本田-release
     */
    public Long testAddAccount1() {
        AtomicInteger count = new AtomicInteger();
        List<EshieldTransdata> list = transdataMapper.selectByShopType("广汽本田-release");
        //查询建立组织
        cn.com.glsx.shield.modules.entity.Department dept = addDepartment(rootName, null, null);

        Long rootdepartmentID = dept.getId();
        Long telnentID = dept.getTenantId();

        list.stream().forEach(data -> {
            User user = userService.findByAccount(data.getUsername());
            if (null == user) {
                log.info("data0:" + data + "====DepartmentID:" + rootdepartmentID);
                addUser(rootdepartmentID, -1L, roleRoot, data, telnentID);
            }
            count.getAndIncrement();
        });
        log.info("数量：" + count.get() + "====telnentID:" + telnentID);
        return telnentID;
    }

    /**
     * 大区
     */

    public void testAddAccount2(String rootNmae, Long tenant_id) {
        AtomicInteger count = new AtomicInteger();
        List<EshieldTransdata> list = transdataMapper.selectByShopType(rootNmae);
        for (EshieldTransdata data : list) {
            String[] areas = data.getRolename().split("-");
            String departmentName = areas[0].substring(4);
            //添加大区组织
            DepartmentSearch deptSearch = new DepartmentSearch()
                    .setTenantId(tenant_id)
                    .setEnableStatus(1)
                    .setDepartmentName(departmentName)
                    .setIsRoot(Constants.IS_NOT_ROOT_DEPARTMENT);

            List<cn.com.glsx.shield.modules.entity.Department> departments = departmentMapper.search(deptSearch);
            if (CollectionUtils.isEmpty(departments)) {
                continue;
            }
            //该组织部门id
            Long departmentAreaID = departments.get(0).getId();
            String departmentName2 = departments.get(0).getDepartmentName();
            User user = userService.findByAccount(data.getUsername());
            //添加没有上级的用户
            if (null == user) {
                log.info("大区user:" + data + "组织====:" + departmentName2 + "====组织id====:" + departmentAreaID);
                addUser(departmentAreaID, -1L, roleArea, data, tenant_id);
            }
            count.getAndIncrement();
        }

        log.info("大区数量：" + count.get());
    }


    /**
     * 省（销售系长）
     */

    public void testAddAccount3(String rootNmae, Long tenant_id) {
        AtomicInteger count = new AtomicInteger();
        List<EshieldTransdata> list = transdataMapper.selectByShopType(rootNmae);
        for (EshieldTransdata data : list) {
            String[] areas = data.getRolename().split("-");
            String departmentName = areas[0].substring(4);
            //添加系
            DepartmentSearch deptSearch = new DepartmentSearch()
                    .setTenantId(tenant_id)
                    .setEnableStatus(1)
                    .setDepartmentName(departmentName)
                    .setIsRoot(Constants.IS_NOT_ROOT_DEPARTMENT);

            List<cn.com.glsx.shield.modules.entity.Department> departments = departmentMapper.search(deptSearch);
            if (CollectionUtils.isEmpty(departments)) {
                continue;
            }
            String typeVals = data.getTypeval();
            if (StringUtils.isEmpty(typeVals)) {
                continue;
            }
            String[] split = typeVals.split(",");
            //大区user
            User finalUser = userService.findByAccount(data.getUsername());
            if (null == finalUser) {
                continue;
            }

            for (String userid : split) {
                EshieldTransdata store_data = transdataMapper.selectByUserId(Long.valueOf(userid)).get(0);
                log.info("data:" + data.getLoginuserid() + "==accountid:" + finalUser.getId() + "====child:" + store_data);
                addUser(finalUser.getDepartmentId(), finalUser.getId(), roleProvince, store_data, tenant_id);
                count.getAndIncrement();
            }
        }
        log.info("省数量：" + count.get());
    }


    /**
     * 巡回员
     */

    public void testAddAccount4(String rootNmae, Long tenant_id) {
        AtomicInteger count = new AtomicInteger();

        List<EshieldTransdata> list = transdataMapper.selectByShopType(rootNmae);
        for (EshieldTransdata data : list) {
            User finalUser = userService.findByAccount(data.getUsername());
            if (null == finalUser) {
                continue;
            }
            //上级部门id
            String typeVals = data.getTypeval();
            if (StringUtils.isEmpty(typeVals)) {
                continue;
            }

            String[] split = typeVals.split(",");
            //巡回员userid
            for (String userid : split) {
                Long departmentId = null;

                HashSet<String> provinceSet = new HashSet<>();
                HashSet<String> citySet = new HashSet<>();

                EshieldTransdata city_data = transdataMapper.selectByUserId(Long.valueOf(userid)).get(0);
                String classType = city_data.getTypeval();
                if (StringUtils.isEmpty(classType)) {
                    continue;
                }
                String[] classIDs = classType.split(",");

                for (String classID : classIDs) {
                    EshieldTransdata store_data = transdataMapper.selectByClassId(Integer.valueOf(classID));
                    if (null == store_data) {
                        continue;
                    }
                    String rolename = store_data.getRolename();
                    String[] orgs = rolename.split("/");
                    String org0 = orgs[0];
                    String org1 = orgs[1] + "大区";
                    String org2 = orgs[2] + "省";
                    String org3 = orgs[3] + "市";

                    provinceSet.add(org0 + "/" + org1 + "/" + org2);
                    citySet.add(org0 + "/" + org1 + "/" + org2 + "/" + org3);

                    DepartmentSearch deptSearch = new DepartmentSearch()
                            .setTenantId(tenant_id)
                            .setEnableStatus(1)
                            .setDepartmentName(org3)
                            .setIsRoot(Constants.IS_NOT_ROOT_DEPARTMENT);
                    List<cn.com.glsx.shield.modules.entity.Department> departments = departmentMapper.search(deptSearch);
                    if (!org.apache.shiro.util.CollectionUtils.isEmpty(departments)) {
                        cn.com.glsx.shield.modules.entity.Department org3dept = departments.get(0);
                        //市区id
                        departmentId = org3dept.getId();
                    }
                }

                if (provinceSet.size() > 1) {
                    for (String s : provinceSet) {
                        log.info("跨省：" + s);
                    }
                    Iterator<String> iterator = provinceSet.iterator();
                    if (iterator.hasNext()) {
                        String str = iterator.next();
                        String[] orgs = str.split("/");

                        DepartmentSearch deptSearch = new DepartmentSearch()
                                .setTenantId(tenant_id)
                                .setEnableStatus(1)
                                .setDepartmentName(orgs[1])
                                .setIsRoot(Constants.IS_NOT_ROOT_DEPARTMENT);

                        List<cn.com.glsx.shield.modules.entity.Department> departments = departmentMapper.search(deptSearch);
                        if (!org.apache.shiro.util.CollectionUtils.isEmpty(departments)) {
                            //提升至大区id
                            departmentId = departments.get(0).getId();
                        }
                        log.info("跨省后处理的id：" + departmentId);
                    }
                } else if (citySet.size() > 1) {
                    for (String s : provinceSet) {
                        log.info("跨市：" + s);
                    }
                    Iterator<String> iterator = provinceSet.iterator();
                    if (iterator.hasNext()) {
                        String str = iterator.next();
                        String[] orgs = str.split("/");

                        DepartmentSearch deptSearch = new DepartmentSearch()
                                .setTenantId(tenant_id)
                                .setEnableStatus(1)
                                .setDepartmentName(orgs[2])
                                .setIsRoot(Constants.IS_NOT_ROOT_DEPARTMENT);

                        List<cn.com.glsx.shield.modules.entity.Department> departments = departmentMapper.search(deptSearch);
                        if (!org.apache.shiro.util.CollectionUtils.isEmpty(departments)) {
                            //提示至省id
                            departmentId = departments.get(0).getId();
                        }
                        log.info("跨市后处理的id：" + departmentId);
                    }
                }

                if (null != departmentId) {
                    count.getAndIncrement();
                    log.info("departmentId：" + departmentId + "=====data:" + city_data);
                    addUser(departmentId, finalUser.getId(), roleCity, city_data, tenant_id);
                }
            }
        }
        log.info("巡回员数量：" + count.get());
    }

    /**
     * 特约店
     */

    public void testAddAccount5(String rootNmae, Long tenant_id) {
        AtomicInteger count = new AtomicInteger();
        List<EshieldTransdata> list = transdataMapper.selectByShopType(rootNmae);
        //全部有完整组织的特约店
        List<Integer> storelist = new ArrayList<>();

        List<EshieldTransdata> missstorelist = new ArrayList<>();
        for (EshieldTransdata data : list) {
            String typeVals = data.getTypeval();
            if (StringUtils.isEmpty(typeVals)) {
                continue;
            }
            String[] split = typeVals.split(",");
            for (String provinceID : split) {
                //省
                EshieldTransdata provinceData = transdataMapper.selectByUserId(Long.valueOf(provinceID)).get(0);
                String typeValsClass = provinceData.getTypeval();
                if (StringUtils.isEmpty(typeValsClass)) {
                    continue;
                }
                String[] splitClass = typeValsClass.split(",");
                for (String cityId : splitClass) {
                    //巡回员
                    EshieldTransdata cityData = transdataMapper.selectByUserId(Long.valueOf(cityId)).get(0);
                    User cityUser = userService.findByAccount(cityData.getUsername());
                    if (null == cityUser) {
                        continue;
                    }
                    String storeValsClass = cityData.getTypeval();
                    if (StringUtils.isEmpty(storeValsClass)) {
                        continue;
                    }
                    String[] storeClass = storeValsClass.split(",");
                    for (String classid : storeClass) {
                        //特约店
                        EshieldTransdata storeData = transdataMapper.selectByClassId(Integer.valueOf(classid));

                        DepartmentSearch deptSearch = new DepartmentSearch()
                                .setTenantId(tenant_id)
                                .setEnableStatus(1)
                                .setDepartmentName(storeData.getUserremark())
                                .setIsRoot(Constants.IS_NOT_ROOT_DEPARTMENT);

                        List<cn.com.glsx.shield.modules.entity.Department> departments = departmentMapper.search(deptSearch);
                        if (CollectionUtils.isEmpty(departments)) {
                            log.info("特约店不存在=======：" + storeData.getUserremark());
                            continue;
                        }
                        Long departmentId = departments.get(0).getId();

                        User storeUser = userService.findByAccount(storeData.getUsername());
                        if (null == storeUser) {
                            log.info("特约店添加用户=======：" + storeData);
                            addUser(departmentId, cityUser.getId(), roleStore, storeData, tenant_id);
                        }

                        log.info(departments.get(0).getDepartmentName() + "=======特约店添加用户：" + storeData);
                        count.getAndIncrement();
                        storelist.add(storeData.getId());
                    }
                }
            }
        }

        log.info("数量1：" + count.get());

        List<EshieldTransdata> storeALLlist = transdataMapper.selectByShopType("特约店");
        storeALLlist.stream().forEach(data -> {
            if (!storelist.contains(data.getId())) {
                missstorelist.add(data);
            }
        });

        log.info("数量2：" + missstorelist.size());
        String sql = "select * from t_eshield_transdata where id in (";
        StringBuilder builder = new StringBuilder();
        builder.append(sql);
        for (int i = 0; i < missstorelist.size(); i++) {
            EshieldTransdata eshieldTransdata = missstorelist.get(i);
            if (eshieldTransdata.getUserremark().contains("分店")) {
                builder.append(eshieldTransdata.getId());
                if (i != missstorelist.size() - 1) {
                    builder.append(",");
                } else {
                    builder.append(")");
                }
            }
        }
        log.info("misssql====：" + builder.toString());

        //所有分店特约店，建立用户
        for (EshieldTransdata secstoreData : missstorelist) {
            //DLN008S001
            String username = secstoreData.getUsername();
            //DLN008
            String parentUserName = username.substring(0, 6);
            User parentUser = userService.findByAccount(parentUserName);
            if (null == parentUser) {
                log.info("分店上级不存在：" + parentUserName);
                continue;
            }

            Long departmentId = parentUser.getDepartmentId();
            if (null == departmentId) {
                log.info("分店上级departmentId不存在：" + parentUserName);
                continue;
            }
            String secondStoreName = secstoreData.getUserremark();
            cn.com.glsx.shield.modules.entity.Department orgsecondStore = addDepartment(secondStoreName, departmentId, tenant_id);
            Long seconddepartmentID = orgsecondStore.getId();

            User storeUser = userService.findByAccount(secstoreData.getUsername());
            if (null == storeUser) {
                addUser(seconddepartmentID, parentUser.getId(), roleStore, secstoreData, tenant_id);
            }
            count.getAndIncrement();
        }
        log.info("特约店+分店数量：" + count.get());
    }


    /**
     * 添加用户
     *
     * @param departmentId
     * @param superiorId
     * @param roleid
     * @param data
     * @param telnentID
     */
    private void addUser(Long departmentId, Long superiorId, Long roleid, EshieldTransdata data, Long telnentID) {
        UserBO userBO = new UserBO();
        //账户名
        userBO.setAccount(data.getUsername());
        //用户名
        userBO.setUsername(data.getUserremark());

        userBO.setDepartmentId(departmentId);
        //直接上级id
        if (-1L != superiorId) {
            userBO.setSuperiorId(superiorId);
        }
        //密码
        userBO.setPassword("ABC123");
        //角色id
        userBO.setRoleId(roleid);
        //手机
        userBO.setPhoneNumber("13800138000");
        //性别
        userBO.setGender(1L);
        //启用
        userBO.setEnableStatus(1);
        userBO.setTenantId(telnentID);
        try {
            userService.addUser(userBO);
        } catch (Exception e) {
            log.info("addUserException==========:" + e.getMessage());
            log.info("addUserExceptionBo=====" + userBO);
        }
    }

    private cn.com.glsx.shield.modules.entity.Department addDepartment(String rootNmae, Long supid, Long tenantId) {
        DepartmentSearch deptSearch = new DepartmentSearch()
                .setEnableStatus(1)
                .setDepartmentName(rootNmae)
                .setIsRoot(null == supid ? Constants.IS_ROOT_DEPARTMENT : Constants.IS_NOT_ROOT_DEPARTMENT);
        if (null != supid) {
            deptSearch.setTenantId(tenantId);
        }

        List<cn.com.glsx.shield.modules.entity.Department> departments = departmentMapper.search(deptSearch);
        if (CollectionUtils.isEmpty(departments)) {
            //添加组织
            OrganizationBO Org = new OrganizationBO();
            Org.setEnableStatus(1);
            Org.setDepartmentName(rootNmae);
            Org.setOrderNum(1);
            if (null == supid) {
                organizationService.addRootOrganization(Org);
                log.info("addDepartment根组织：" + Org);
            } else {
                //挂靠组织
                Org.setSuperiorId(supid);
                organizationService.addNodeToOrganization(Org);
                log.info("addDepartment节点组织：" + Org);
            }
        }
        List<cn.com.glsx.shield.modules.entity.Department> departmentsEnd = departmentMapper.search(deptSearch);
        if (CollectionUtils.isEmpty(departmentsEnd)) {
            log.info("addDepartment组织异常：" + rootNmae);
        }
        return CollectionUtils.isEmpty(departmentsEnd) ? null : departmentsEnd.get(0);
    }

}
