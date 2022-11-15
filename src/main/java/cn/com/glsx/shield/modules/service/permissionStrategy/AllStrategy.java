package cn.com.glsx.shield.modules.service.permissionStrategy;

import cn.com.glsx.admin.common.constant.Constants;
import cn.com.glsx.shield.modules.entity.Department;
import cn.com.glsx.shield.modules.entity.Organization;
import cn.com.glsx.shield.modules.entity.User;
import cn.com.glsx.shield.modules.model.param.DepartmentSearch;
import cn.com.glsx.shield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.shield.modules.model.param.OrganizationSearch;
import cn.com.glsx.shield.modules.model.param.UserSearch;
import cn.com.glsx.shield.modules.model.view.DepartmentDTO;
import cn.com.glsx.shield.modules.service.DepartmentService;
import cn.com.glsx.usercenter.api.dto.tree.OrgModel;
import cn.com.glsx.usercenter.api.dto.tree.OrgTreeModel;
import com.glsx.plat.common.model.TreeModel;
import com.glsx.plat.common.utils.StringUtils;
import com.glsx.plat.common.utils.TreeModelUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author taoyr
 */
@Slf4j
@Component
public class AllStrategy extends PermissionStrategy {

    @Resource
    private DepartmentService departmentService;

    public AllStrategy() {
        super();
    }

    @Override
    public List<Long> permissionDepartmentIds() {
        List<Department> departmentList = this.permissionDepartments();
        return departmentList.stream().map(Department::getId).collect(Collectors.toList());
    }

    @Override
    public List<Department> permissionDepartments() {
        //超管或全部数据权限
        return departmentMapper.selectAllNotDel();
    }

    @Override
    public List<User> permissionUsers() {
        //超管或全部数据权限
        return userMapper.selectAllNotDel();
    }

    @Override
    public List<User> permissionUsersByDepartmentId(Long departmentId) {
        //假设本人所在组织链：R-A-B-C-本人组织-X-Y-Z

        //departmentId全部下级组织节点（本人组织-X-Y-Z）
        List<Organization> subOrgList = organizationMapper.selectAllSubBySuperiorId(departmentId);

        List<Long> subOrgIdList = subOrgList.stream().map(Organization::getSubId).collect(Collectors.toList());

        //返回departmentId及其下级用户
        List<User> userList = userMapper.selectByDepartmentIds(subOrgIdList);

        return userList;
    }

    /**
     * 1 all
     * * 1.1 root 找所有根部门
     * * 1.2 非root 根据rootId找子部门list
     * * 封装-不过滤-返回
     *
     * @param rootId
     * @return
     */
    @Override
    public List<DepartmentDTO> orgSimpleList(Long rootId) {
        List<Department> departmentParamList;
        if (rootId == null) {
            departmentParamList = departmentMapper.search(new DepartmentSearch().setIsRoot(Constants.IS_ROOT_DEPARTMENT));
        } else {
            departmentParamList = organizationMapper.selectChildrenList(new OrganizationSearch().setRootId(rootId));
        }
        return departmentService.getDepartmentAssembled(departmentParamList, true, true);
    }

    /**
     * 1.查询符合条件的部门nameDepartmentList
     * 2.找出idList所有向上的路径经过的所有部门allDepartmentIdList
     * 3.找出所有组织链organizationList
     * 4.找出根节点rootList
     * 5.封装-调用TreeModelUtil组装树
     *
     * @param search
     * @return
     */
    @Override
    public List<? extends TreeModel> orgTree(OrgTreeSearch search) {

        Collection<Long> orgIds = Sets.newHashSet();

        //模糊搜索组织到根组织的(多个)组织链 eg. A-B-C X-Y-Z
        if (StringUtils.isNotEmpty(search.getOrgName())) {
            orgIds = this.getSuperiorIds(search);
        }

        //查询组织到根组织、最后一级的完整组织链 eg. A-B-C,由B得A-B-C
        if (CollectionUtils.isNotEmpty(search.getOrgIds())) {
            Collection<Long> orgIds1 = this.getSuperiorIds(search.getOrgIds());

            Collection<Long> orgIds2 = this.getSubIds(search.getOrgIds());

            orgIds.addAll(orgIds1);
            orgIds.addAll(orgIds2);
        }

        //查询组织到根组织的组织链 eg. A-B-C,由B得A-B
        if (CollectionUtils.isNotEmpty(search.getSupOrgIds())) {
            orgIds = this.getSuperiorIds(search.getSupOrgIds());
        }

        //查询组织到最后一级组织的组织链 eg. A-B-C,由B得B-C，这种会丢失根组织，只能以线性结构返回
        if (CollectionUtils.isNotEmpty(search.getSubOrgIds())) {
            Map<Long, Set<Long>> subOrgIdListMap = getSubIdsMap(search.getSubOrgIds());

            //选中的节点及下级节点
            Set<Long> selectSubOrgIds = Sets.newHashSet();

            for (Map.Entry<Long, Set<Long>> entry : subOrgIdListMap.entrySet()) {
                selectSubOrgIds.addAll(entry.getValue());
            }

            orgIds = selectSubOrgIds;
        }

        //查询用户组织到根组织的完整上级链 eg. u得组织为C，A-B-C,由u得A-B-C
        if (CollectionUtils.isNotEmpty(search.getUserIds())) {
            orgIds = this.getUserOrgSuperiorIds(search.getUserIds());
        }

        //如果有查询条件，如果没有过滤到需要查询的组织链，返回空数据
        if ((StringUtils.isNotEmpty(search.getOrgName())
                || CollectionUtils.isNotEmpty(search.getOrgIds())
                || CollectionUtils.isNotEmpty(search.getSupOrgIds())
                || CollectionUtils.isNotEmpty(search.getSubOrgIds())
                || CollectionUtils.isNotEmpty(search.getUserIds())) && CollectionUtils.isEmpty(orgIds)) {
            return Lists.newArrayList();
        }

        search.setOrgIds(orgIds);

        StopWatch sw = new StopWatch();
        sw.start("task1");
        List<OrgModel> modelList = organizationMapper.selectOrgList(search);

        List<Long> departmentIdList = modelList.stream().map(OrgModel::getOrgId).collect(Collectors.toList());
        sw.stop();

        sw.start("task2");
        //计算用户数
        Map<Long, Integer> recursiveDepartmentUserMap = departmentService.countRecursiveDepartmentUser(departmentIdList);

        List<OrgTreeModel> orgTreeModelList = modelList.stream().map(OrgTreeModel::new).collect(Collectors.toList());

        List<OrgTreeModel> allTreeModelList = Lists.newArrayList(orgTreeModelList);
        sw.stop();

        //查询需要的用户,然后放到对应的组织节点的child,如果需要返回用户子节点的话
        Map<Long, List<OrgModel>> groupByOrgIdUserMap = Maps.newHashMap();
        if (search.getHasUserLeaf() && CollectionUtils.isNotEmpty(departmentIdList)) {
            List<OrgModel> allUserList = userMapper.selectUserOrgModels(new UserSearch().setDepartmentIds(departmentIdList).setUserIds(search.getUserIds()));
            groupByOrgIdUserMap = allUserList.stream().collect(Collectors.groupingBy(OrgModel::getParentId));
        }

        sw.start("task3");
//        final Set<Long> subRootOrgIds = subOrgIdListMap.keySet();
        Map<Long, List<OrgModel>> finalGroupByOrgIdUserMap = groupByOrgIdUserMap;
        orgTreeModelList.forEach(otm -> {
            Long orgId = Long.valueOf(otm.getId());
            Integer userNumber = recursiveDepartmentUserMap.get(orgId);
            otm.setUserNumber(userNumber == null ? 0 : userNumber);

            //查询多组织节点下组织的树形结构（没有太大意义）,eg. A-B-C-D,U-V-W-X-Y-Z 返回B-C-D,X-Y-Z
//            if (search.getReturnTrees() && subRootOrgIds.contains(orgId)) {
//                otm.getOrigin().setIsRoot(1);
//            }
            //用户子树
            if (search.getHasUserLeaf() && otm.getUserNumber() > 0) {
                List<OrgModel> userList = finalGroupByOrgIdUserMap.getOrDefault(orgId, Lists.newArrayList());
                List<OrgTreeModel> userTreeList = userList.stream().map(OrgTreeModel::new).collect(Collectors.toList());
                allTreeModelList.addAll(userTreeList);
            }
        });
        sw.stop();

        sw.start("task4");
        if (search.getReturnTrees()) {
            List<? extends TreeModel> orgTree = TreeModelUtil.fastConvertByRootMark(allTreeModelList, 1);
            return orgTree;
        }
        sw.stop();

        log.info("任务耗时概览：{}，总耗时：{}秒", sw.prettyPrint(), sw.getTotalTimeSeconds());
        return allTreeModelList;
    }

    @Override
    public Map<Long, Integer> calculateNumberOfOrgUsers(Collection<Long> orgIds) {
        return null;
    }

}
