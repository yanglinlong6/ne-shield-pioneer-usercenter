package cn.com.glsx.shield.modules.entity;

import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

@Data
@Table(name = "t_cust_user_local_account")
public class CustUserLocalAccount extends BaseEntity {

    /**
     * 认证id
     */
    @Column(name = "auth_id")
    private Long authId;

    /**
     * 平台id
     */
    @Column(name = "union_id")
    private String unionId;

    /**
     * 用户名/昵称
     */
    private String username;

    /**
     * 头像
     */
    private String avatar;

}