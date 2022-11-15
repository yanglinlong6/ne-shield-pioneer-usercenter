package cn.com.glsx.shield.modules.model.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * @author: taoyr
 **/
@Data
@Accessors(chain = true)
public class OrganizationBO {

    private Long id;

    private String departmentName;

    private Long superiorId;

    private Integer orderNum;

    private Integer enableStatus;

    private Collection<Long> subIdList;

    private Collection<Long> superiorIdList;

    private Integer biggerDepth;

}
