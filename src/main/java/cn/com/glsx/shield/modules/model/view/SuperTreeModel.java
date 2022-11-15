package cn.com.glsx.shield.modules.model.view;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author taoyr
 */
@Accessors(chain = true)
@Data
public class SuperTreeModel implements Serializable {

    private Long id;
    private String label;
    private Integer order = 0;
    /**
     * 类型：0部门，1人
     */
    private Integer type;
    private boolean isRoot;
    private List<SuperTreeModel> children;
    private boolean disabled;

}
