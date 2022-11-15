package cn.com.glsx.shield.modules.entity;

import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Table;

@Data
@Accessors(chain = true)
@Table(name = "t_role_data_permission")
public class RoleDataPermission extends BaseEntity {

    /**
     * 角色id
     */
    @Column(name = "role_id")
    private Long roleId;

    /**
     * 关联id
     */
    @Column(name = "ref_id")
    private Long refId;

    /**
     * 权限类型：0租户，1部门，2用户
     */
    private Integer type;

}