package cn.com.glsx.shield.modules.mapper;

import cn.com.glsx.shield.modules.entity.User;
import cn.com.glsx.shield.modules.model.param.UserBO;
import cn.com.glsx.shield.modules.model.param.UserSearch;
import cn.com.glsx.shield.modules.model.view.DepartmentCount;
import cn.com.glsx.usercenter.api.dto.request.FindUserRequest;
import cn.com.glsx.usercenter.api.dto.response.CUser;
import cn.com.glsx.usercenter.api.dto.tree.OrgModel;
import com.glsx.plat.mybatis.mapper.CommonBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface UserMapper extends CommonBaseMapper<User> {

    User selectById(@Param("id") Long id);

    List<User> selectAllNotDel();

    List<User> selectByIds(@Param("ids") Collection<Long> ids);

    List<User> selectByTenantId(@Param("tenantId") Long tenantId);

    List<User> selectByTenantIds(@Param("tenantIds") Collection<Long> tenantIds);

    List<User> selectByDepartmentId(@Param("departmentId") Long departmentId);

    List<OrgModel> selectUserOrgModelById(@Param("id") Long id);

    List<OrgModel> selectUserOrgModels(UserSearch search);

//    List<OrgModel> selectUserOrgModels(@Param("departmentId") Long departmentId, @Param("userId") Long userId);

    List<User> selectByDepartmentIds(@Param("departmentIds") Collection<Long> departmentIds);

    List<User> selectExcludeUserIdByDepartmentId(@Param("departmentId") Long departmentId, @Param("userId") Long userId);

    List<User> selectByAccount(@Param("account") String account);

    int countByAccount(@Param("account") String account);

    User selectByAccountAndTenantId(@Param("account") String account, @Param("tenantId") Long tenantId);

    int countByAccountAndTenantId(@Param("account") String account, @Param("tenantId") Long tenantId);

    List<User> selectByUsername(@Param("username") String username);

    List<User> search(UserSearch search);

    Integer countByCriterial(UserBO userBO);

    List<DepartmentCount> countDepartmentsUser(@Param("departmentIds") Collection<Long> departmentIds);

    List<User> selectDepartmentsSubordinate(UserSearch search);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    int logicDeleteById(@Param("id") Long id);

    List<User> selectUsersByCondition(FindUserRequest condition);

    User selectSuperiorUserById(@Param("id") Long id);

    List<cn.com.glsx.usercenter.api.dto.response.User> selectSuperiorUserByIds(@Param("ids") Collection<Long> ids);

    List<CUser> selectByRelUserIds(@Param("relUserIds") Collection<Long> relUserIds);

}