package cn.com.glsx.shield.modules.entity;

import cn.com.glsx.auth.model.SyntheticUser;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

@Accessors(chain = true)
@Data
@Table(name = "t_user_path")
public class UserPath extends BaseEntity {

    public UserPath() {
        super();
    }

    public UserPath(boolean isAdd) {
        if (isAdd) {
            SyntheticUser user = ShieldContextHolder.getUser();
            this.setCreatedBy(user.getUserId());
            this.setCreatedDate(new Date());
        }
    }

    /**
     * 上级用户
     */
    @Column(name = "superior_id")
    private Long superiorId;

    /**
     * 下级用户id
     */
    @Column(name = "sub_id")
    private Long subId;

    /**
     * 深度(下级节点-上级节点层数)
     */
    private Integer depth;

    /**
     * 租户id
     */
    @Column(name = "tenant_id")
    private Long tenantId;


}