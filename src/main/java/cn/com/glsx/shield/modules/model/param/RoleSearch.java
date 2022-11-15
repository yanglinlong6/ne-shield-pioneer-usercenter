package cn.com.glsx.shield.modules.model.param;

import cn.hutool.db.Page;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * @author payu
 */
@Data
@Accessors(chain = true)
public class RoleSearch extends Page {

    private Long roleId;

    private String roleName;

    private Integer enableStatus;

    private Integer roleVisibility;

    private Collection<Long> tenantIds;

}
