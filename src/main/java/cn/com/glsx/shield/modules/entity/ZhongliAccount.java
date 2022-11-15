package cn.com.glsx.shield.modules.entity;

import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

@Data
@Table(name = "t_zhongli_account")
public class ZhongliAccount extends BaseEntity {

    /**
     * e盾角色
     */
    @Column(name = "edun_role")
    private String edunRole;

    /**
     * 用户账号
     */
    private String account;

    /**
     * 用户名称
     */
    private String username;

    /**
     * 分组
     */
    @Column(name = "group_name")
    private String groupName;

    /**
     * 分类
     */
    @Column(name = "class_name")
    private String className;

    /**
     * 级别
     */
    private String level;

    /**
     * 部门
     */
    private String department;

    /**
     * 新角色
     */
    @Column(name = "role_name")
    private String roleName;

    /**
     * 类型
     */
    private Integer type;

}