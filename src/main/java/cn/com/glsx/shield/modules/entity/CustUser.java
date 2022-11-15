package cn.com.glsx.shield.modules.entity;

import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

@Data
@Table(name = "t_cust_user")
public class CustUser extends BaseEntity {

    /**
     * 登录token
     */
    private String token;

    /**
     * token过期时间
     */
    @Column(name = "expire_in")
    private Long expireIn;

    /**
     * 登录失败次数
     */
    @Column(name = "try_times")
    private Integer tryTimes;

    /**
     * 关联人（账户）id
     */
    @Column(name = "rel_user_id")
    private Long relUserId;

    /**
     * 黑名单状态：1白名单，2临时封禁，3永久封禁
     */
    private Integer blacklist;

}