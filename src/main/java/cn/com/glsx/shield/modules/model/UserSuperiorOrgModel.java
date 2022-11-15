package cn.com.glsx.shield.modules.model;

import lombok.Data;

@Data
public class UserSuperiorOrgModel {

    private String superiorIds;
    private String superiorNames;
    private Long orgId;
    private String orgName;

    private Long userId;
    private String username;
    private String account;

}
