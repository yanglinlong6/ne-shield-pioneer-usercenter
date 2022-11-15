package cn.com.glsx.shield.modules.entity;

import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;

import javax.persistence.Table;

@Data
@Table(name = "t_cust_user_local_auth")
public class CustUserLocalAuth extends BaseEntity {

    /**
     * 用户账号
     */
    private String account;

    /**
     * 密码
     */
    private String password;

}