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
@Table(name = "t_tenant")
public class Tenant extends BaseEntity {

    /**
     * 租户名称
     */
    @Column(name = "tenant_name")
    private String tenantName;

    public Tenant() {
        super();
    }

    public Tenant(boolean isAdd) {
        if (isAdd) {
            SyntheticUser user = ShieldContextHolder.getUser();
            this.setCreatedBy(user.getUserId());
            this.setCreatedDate(new Date());
        }
    }

}