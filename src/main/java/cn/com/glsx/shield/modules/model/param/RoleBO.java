package cn.com.glsx.shield.modules.model.param;

import cn.com.glsx.auth.model.BizDataPermission;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @author taoyr
 */
@Data
@Accessors(chain = true)
public class RoleBO implements Serializable {

    private Long roleId;

    @NotBlank
    @Size(max = 50)
    private String roleName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 角色租户可见度（0=共享，1=系统管理员，2=指定租户）
     */
    @NotNull
    private Integer roleVisibility;

    /**
     * 角色可见租户id列表
     */
    private String roleTenants;

    /**
     * 数据权限（0=本人 1=本人及下属 2=本部门 3=本部门及下级部门 4=全部 5指定组织 6指定租户）
     */
    @NotNull
    private Integer rolePermissionType;

    /**
     * 业务数据权限
     */
    private List<BizDataPermission> bizDataPermissions;

    /**
     * 可见数据权限租户id列表
     */
    private String permTenants;

    /**
     * 状态（1正常 ，2停用）
     */
    @NotNull
    private Integer enableStatus;

    /**
     * 选中菜单列表
     */
    private List<Long> menuIdList;
}
