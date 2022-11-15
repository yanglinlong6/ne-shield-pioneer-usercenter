package cn.com.glsx.shield.modules.model.view;

import cn.com.glsx.auth.model.BizDataPermission;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class RoleDTO {

    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色可见度（0=共享，1=系统管理员，2=指定租户）
     */
    private Integer roleVisibility;

    /**
     * 角色可见租户
     */
    private String roleTenants;

    /**
     * 角色权限类型 0=本人 1=本人及下属 2=本部门 3=本部门及下级部门 4=全部 5指定部门，6指定租户
     */
    private Integer rolePermissionType;

    /**
     * 业务数据权限
     */
    private List<BizDataPermission> bizDataPermissions;

    /**
     * 指定租户权限
     */
    private String permTenants;

    /**
     * 角色最大拥有用户数
     */
    private Long maxUser;

    /**
     * 备注
     */
    private String remark;

    private Integer enableStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;

}
