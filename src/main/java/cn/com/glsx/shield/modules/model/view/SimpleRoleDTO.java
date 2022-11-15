package cn.com.glsx.shield.modules.model.view;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author payu
 */
@Setter
@Getter
@Accessors(chain = true)
public class SimpleRoleDTO {

    private Long roleId;
    private String roleName;

}
