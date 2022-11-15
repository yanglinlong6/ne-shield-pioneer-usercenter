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
public class UserSearch extends Page {

    private String searchField;

    private Long userId;

    private Collection<Long> userIds;

    private Integer userStatus;

    private Collection<Long> tenantIds;

    private Long departmentId;

    private Collection<Long> departmentIds;

}
