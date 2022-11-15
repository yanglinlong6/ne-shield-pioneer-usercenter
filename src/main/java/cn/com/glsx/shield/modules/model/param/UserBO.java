package cn.com.glsx.shield.modules.model.param;

import cn.com.glsx.admin.common.util.RegexUtil;
import com.glsx.plat.context.utils.validator.group.AddGroup;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author payu
 */
@Data
@Accessors(chain = true)
public class UserBO {

    private Long id;

    @Size(min = 3, max = 30)
    @NotBlank
    private String account;

    /**
     * 部门id
     */
    @NotNull(message = "部门不能为空")
    private Long departmentId;

    /**
     * 上级id
     */
    private Long superiorId;

    @Size(min = 2, max = 50)
    @NotBlank
    private String username;

    @NotBlank(message = "密码不能为空", groups = {AddGroup.class})
    private String password;

    @NotNull(message = "角色不能为空")
    private Long roleId;

    @NotBlank
    @Pattern(regexp = RegexUtil.mobileRegex, message = "手机号码格式有误")
    private String phoneNumber;

    private String email;

    private Long gender;

    /**
     * 岗位
     */
    private String position;

    @NotNull
    private Integer enableStatus;

    private String remark;

    private Long tenantId;

    private List<Long> departmentIds;

}
