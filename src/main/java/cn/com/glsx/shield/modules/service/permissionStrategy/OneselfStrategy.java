package cn.com.glsx.shield.modules.service.permissionStrategy;

import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.shield.modules.entity.Department;
import cn.com.glsx.shield.modules.entity.Organization;
import cn.com.glsx.shield.modules.entity.User;
import cn.com.glsx.shield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.shield.modules.model.view.DepartmentDTO;
import cn.com.glsx.shield.modules.service.DepartmentService;
import cn.com.glsx.usercenter.api.dto.tree.OrgModel;
import cn.com.glsx.usercenter.api.dto.tree.OrgTreeModel;
import com.glsx.plat.common.model.TreeModel;
import com.glsx.plat.common.utils.TreeModelUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author taoyr
 */
@Slf4j
@Component
public class OneselfStrategy extends PermissionStrategy {

    @Resource
    private DepartmentService departmentService;

    public OneselfStrategy() {
        super();
    }

    @Override
    public List<Long> permissionDepartmentIds() {
        return Lists.newArrayList(ShieldContextHolder.getDepartmentId());
    }

    @Override
    public List<Department> permissionDepartments() {
        Department department = departmentMapper.selectById(ShieldContextHolder.getDepartmentId());
        return Lists.newArrayList(department);
    }

    @Override
    public List<User> permissionUsers() {
        List<User> list = Lists.newArrayList();
        User user = userMapper.selectById(ShieldContextHolder.getUserId());
        CollectionUtils.addAll(list, user);
        return list;
    }

    @Override
    public List<User> permissionUsersByDepartmentId(Long departmentId) {
        //假设本人所在组织链：R-A-B-C-本人组织-X-Y-Z

        //departmentId全部上级组织节点（R-A-B-C-本人组织）
        List<Organization> superiorOrgList = organizationMapper.selectAllSuperiorBySubId(ShieldContextHolder.getDepartmentId());

        List<Long> superiorOrgIds = superiorOrgList.stream().map(Organization::getSuperiorId).collect(Collectors.toList());

        //departmentId只要在所在上级组织链上（R-A-B-C-本人组织），返回本人即可
        if (superiorOrgIds.contains(departmentId)) {
            return permissionUsers();
        }
        return Lists.newArrayList();
    }

    /**
     * 2 self 或 selfDepartment
     * * 2.1 root 找自己根部门
     * * 2.2 非root
     * * 先用rootId找到与自己部门的深度，看是上级还是下级（包括用户本部门）
     * * 2.2.1 上级
     * * 找确定深度的上级部门 先用rootId找到与自己部门的深度，-1得到确定深度，从t_org得到department_id（单个）
     * * 2.2.2 下级（本部门）
     * * 返回空
     * * 封装（false，false）-设置userNum为1（self）或设置userNum为自己部门人数（selfDepartment）-如果是本部门设置hasChild为false，非本部门设置hasChild为true
     *
     * @param rootId
     * @return
     */
    @Override
    public List<DepartmentDTO> orgSimpleList(Long rootId) {

        Long userDeptId = ShieldContextHolder.getDepartmentId();

        List<Department> departmentParamList = Lists.newArrayList();

        Department department = departmentMapper.selectById(userDeptId);

        //只能看他本人的部门
        departmentParamList.add(department);

        List<DepartmentDTO> departmentDTOList = departmentService.getDepartmentAssembled(departmentParamList, false, false);

        departmentDTOList.forEach(dep -> dep.setUserNumber(1));

        departmentDTOList.forEach(dep -> {
            if (!dep.getId().equals(userDeptId)) {
                dep.setHasChildren(true);
            }
        });
        return departmentDTOList;
    }

    /**
     * 只能看到个人所在部门得组织链，并且到个人部门终止
     *
     * @param search
     * @return
     */
    @Override
    public List<? extends TreeModel> orgTree(OrgTreeSearch search) {

        Long tenantId = ShieldContextHolder.getTenantId();

        Long deptId = ShieldContextHolder.getDepartmentId();

        Long userId = ShieldContextHolder.getUserId();

        search.setTenantId(tenantId);

        search.setOrgId(deptId);

        Collection<Long> superiorIds = this.getSuperiorIds(search);

        search.setOrgIds(superiorIds);

        List<OrgModel> modelList = organizationMapper.selectOrgList(search);

        List<OrgTreeModel> orgTreeModelList = modelList.stream().map(OrgTreeModel::new).collect(Collectors.toList());

        List<OrgTreeModel> allTreeModelList = Lists.newArrayList(orgTreeModelList);

        orgTreeModelList.stream().filter(otm -> Long.valueOf(otm.getId()).equals(deptId)).forEach(otm -> {
            otm.setUserNumber(1);
            //用户子树
            if (search.getHasUserLeaf() && otm.getUserNumber() > 0) {
                List<OrgModel> userList = userMapper.selectUserOrgModelById(userId);
                List<OrgTreeModel> userTreeList = userList.stream().map(OrgTreeModel::new).collect(Collectors.toList());
                allTreeModelList.addAll(userTreeList);
            }
        });

        if (search.getReturnTrees()) {
            List<? extends TreeModel> orgTree = TreeModelUtil.fastConvertByRootMark(allTreeModelList, 1);
            return orgTree;
        }
        return allTreeModelList;
    }

    @Override
    public Map<Long, Integer> calculateNumberOfOrgUsers(Collection<Long> orgIds) {
        return null;
    }

}
