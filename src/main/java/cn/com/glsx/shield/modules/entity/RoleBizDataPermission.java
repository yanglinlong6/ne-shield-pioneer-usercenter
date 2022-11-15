package cn.com.glsx.shield.modules.entity;

import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

@Data
@Table(name = "t_role_biz_data_permission")
public class RoleBizDataPermission extends BaseEntity {

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
     * 权限类型：0预留，1结清数据
     */
    private Integer type;

    /**
     * 可见度:1可见，2默认不可见
     */
    private Integer visibility;

}