package cn.com.glsx.shield.modules.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "t_eshield_transdata")
public class EshieldTransdata implements Serializable {
    /**
     * 主键
     */
    @Id
    private Integer id;

    /**
     * 帐号类型（角色)
     */
    @Column(name = "account_type")
    private String accountType;

    /**
     * 帐号类型
     */
    @Column(name = "shop_type")
    private String shopType;

    /**
     * 角色名称
     */
    @Column(name = "roleName")
    private String rolename;

    /**
     * e盾账号id
     */
    @Column(name = "loginUserId")
    private Integer loginuserid;

    /**
     * e盾账号名称
     */
    private String username;

    /**
     * e盾账号说明
     */
    @Column(name = "userRemark")
    private String userremark;

    /**
     * （0:无关系;1:分类ID;2:用户ID）
     */
    private Byte type;

    /**
     * 分组/分类/车辆id
     */
    @Column(name = "typeVal")
    private String typeval;

    /**
     * 分类id
     */
    @Column(name = "class_id")
    private Integer classId;

    /**
     * 是否删除 0=正常 -1=删除
     */
    @Column(name = "del_flag")
    private Byte delFlag;

    /**
     * 创建时间
     */
    @Column(name = "created_date")
    private Date createdDate;

    /**
     * 创建人用户id
     */
    @Column(name = "created_by")
    private Integer createdBy;

    /**
     * 更新时间
     */
    @Column(name = "updated_date")
    private Date updatedDate;

    /**
     * 更新人用户id
     */
    @Column(name = "updated_by")
    private Integer updatedBy;

    private static final long serialVersionUID = 1L;

    /**
     * 获取主键
     *
     * @return id - 主键
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置主键
     *
     * @param id 主键
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取帐号类型（角色)
     *
     * @return account_type - 帐号类型（角色)
     */
    public String getAccountType() {
        return accountType;
    }

    /**
     * 设置帐号类型（角色)
     *
     * @param accountType 帐号类型（角色)
     */
    public void setAccountType(String accountType) {
        this.accountType = accountType == null ? null : accountType.trim();
    }

    /**
     * 获取帐号类型
     *
     * @return shop_type - 帐号类型
     */
    public String getShopType() {
        return shopType;
    }

    /**
     * 设置帐号类型
     *
     * @param shopType 帐号类型
     */
    public void setShopType(String shopType) {
        this.shopType = shopType == null ? null : shopType.trim();
    }

    /**
     * 获取角色名称
     *
     * @return roleName - 角色名称
     */
    public String getRolename() {
        return rolename;
    }

    /**
     * 设置角色名称
     *
     * @param rolename 角色名称
     */
    public void setRolename(String rolename) {
        this.rolename = rolename == null ? null : rolename.trim();
    }

    /**
     * 获取e盾账号id
     *
     * @return loginUserId - e盾账号id
     */
    public Integer getLoginuserid() {
        return loginuserid;
    }

    /**
     * 设置e盾账号id
     *
     * @param loginuserid e盾账号id
     */
    public void setLoginuserid(Integer loginuserid) {
        this.loginuserid = loginuserid;
    }

    /**
     * 获取e盾账号名称
     *
     * @return username - e盾账号名称
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置e盾账号名称
     *
     * @param username e盾账号名称
     */
    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    /**
     * 获取e盾账号说明
     *
     * @return userRemark - e盾账号说明
     */
    public String getUserremark() {
        return userremark;
    }

    /**
     * 设置e盾账号说明
     *
     * @param userremark e盾账号说明
     */
    public void setUserremark(String userremark) {
        this.userremark = userremark == null ? null : userremark.trim();
    }

    /**
     * 获取（0:无关系;1:分类ID;2:用户ID）
     *
     * @return type - （0:无关系;1:分类ID;2:用户ID）
     */
    public Byte getType() {
        return type;
    }

    /**
     * 设置（0:无关系;1:分类ID;2:用户ID）
     *
     * @param type （0:无关系;1:分类ID;2:用户ID）
     */
    public void setType(Byte type) {
        this.type = type;
    }

    /**
     * 获取分组/分类/车辆id
     *
     * @return typeVal - 分组/分类/车辆id
     */
    public String getTypeval() {
        return typeval;
    }

    /**
     * 设置分组/分类/车辆id
     *
     * @param typeval 分组/分类/车辆id
     */
    public void setTypeval(String typeval) {
        this.typeval = typeval == null ? null : typeval.trim();
    }

    /**
     * 获取分类id
     *
     * @return class_id - 分类id
     */
    public Integer getClassId() {
        return classId;
    }

    /**
     * 设置分类id
     *
     * @param classId 分类id
     */
    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    /**
     * 获取是否删除 0=正常 -1=删除
     *
     * @return del_flag - 是否删除 0=正常 -1=删除
     */
    public Byte getDelFlag() {
        return delFlag;
    }

    /**
     * 设置是否删除 0=正常 -1=删除
     *
     * @param delFlag 是否删除 0=正常 -1=删除
     */
    public void setDelFlag(Byte delFlag) {
        this.delFlag = delFlag;
    }

    /**
     * 获取创建时间
     *
     * @return created_date - 创建时间
     */
    public Date getCreatedDate() {
        return createdDate;
    }

    /**
     * 设置创建时间
     *
     * @param createdDate 创建时间
     */
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * 获取创建人用户id
     *
     * @return created_by - 创建人用户id
     */
    public Integer getCreatedBy() {
        return createdBy;
    }

    /**
     * 设置创建人用户id
     *
     * @param createdBy 创建人用户id
     */
    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * 获取更新时间
     *
     * @return updated_date - 更新时间
     */
    public Date getUpdatedDate() {
        return updatedDate;
    }

    /**
     * 设置更新时间
     *
     * @param updatedDate 更新时间
     */
    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    /**
     * 获取更新人用户id
     *
     * @return updated_by - 更新人用户id
     */
    public Integer getUpdatedBy() {
        return updatedBy;
    }

    /**
     * 设置更新人用户id
     *
     * @param updatedBy 更新人用户id
     */
    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", accountType=").append(accountType);
        sb.append(", shopType=").append(shopType);
        sb.append(", rolename=").append(rolename);
        sb.append(", loginuserid=").append(loginuserid);
        sb.append(", username=").append(username);
        sb.append(", userremark=").append(userremark);
        sb.append(", type=").append(type);
        sb.append(", typeval=").append(typeval);
        sb.append(", classId=").append(classId);
        sb.append(", delFlag=").append(delFlag);
        sb.append(", createdDate=").append(createdDate);
        sb.append(", createdBy=").append(createdBy);
        sb.append(", updatedDate=").append(updatedDate);
        sb.append(", updatedBy=").append(updatedBy);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}