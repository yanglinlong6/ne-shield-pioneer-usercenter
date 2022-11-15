package cn.com.glsx.shield.modules.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: taoyr
 **/
@Data
public class MenuModel implements Serializable {

    private Long menuNo;
    private String menuName;
    private Long parentId;
    private Integer type;
    private String icon;
    private String frontRoute;
    private String permissionTag;
    private Integer orderNum;
    private boolean hasChildren;
    private boolean checked;

}
