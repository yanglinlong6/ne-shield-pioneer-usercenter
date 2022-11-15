package cn.com.glsx.shield.modules.model.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * @author payu
 */
@Data
@Accessors(chain = true)
public class OrgTreeSearch {

    private Long tenantId;

    private Long orgId;

    private String orgName;

    private Collection<Long> tenantIds;

    private Collection<Long> orgIds;

    private Collection<Long> supOrgIds;

    private Collection<Long> subOrgIds;

    private Collection<Long> userIds;

    private Integer enableStatus;

    /**
     * 返回树形结构
     */
    private Boolean returnTrees = true;

    /**
     * 返回用户叶子节点
     */
    private Boolean hasUserLeaf = false;

}
