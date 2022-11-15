package cn.com.glsx.shield.modules.model.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author: taoyr
 **/
@Data
@Accessors(chain = true)
public class MenuTreeSearch {

    private List<Long> roleIds;

    private List<Long> menuIds;

    private List<Long> menuNos;

    private List<Integer> menuTypes;

}
