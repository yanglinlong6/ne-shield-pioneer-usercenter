package cn.com.glsx.shield.modules.service.permissionStrategy;

import cn.com.glsx.shield.modules.entity.Department;
import cn.com.glsx.shield.modules.entity.User;
import cn.com.glsx.shield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.shield.modules.model.view.DepartmentDTO;
import com.glsx.plat.common.model.TreeModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author taoyr
 */
@Slf4j
@Component
public class AssignDepartmentStrategy extends PermissionStrategy {

    public AssignDepartmentStrategy() {
        super();
    }

    @Override
    public List<Long> permissionDepartmentIds() {
        return null;
    }

    @Override
    public List<Department> permissionDepartments() {
        return null;
    }

    @Override
    public List<User> permissionUsers() {
        return null;
    }

    @Override
    public List<User> permissionUsersByDepartmentId(Long departmentId) {
        return null;
    }

    @Override
    public List<DepartmentDTO> orgSimpleList(Long rootId) {
        return null;
    }

    @Override
    public List<? extends TreeModel> orgTree(OrgTreeSearch search) {
        return null;
    }

    @Override
    public Map<Long, Integer> calculateNumberOfOrgUsers(Collection<Long> orgIds) {
        return null;
    }

}
