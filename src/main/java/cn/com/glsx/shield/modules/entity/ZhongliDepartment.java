package cn.com.glsx.shield.modules.entity;

import com.glsx.plat.mybatis.base.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

@Data
@Table(name = "t_zhongli_department")
public class ZhongliDepartment extends BaseEntity {

    /**
     * 分组id
     */
    @Column(name = "group_id")
    private Integer groupId;

    /**
     * 分组
     */
    @Column(name = "group_name")
    private String groupName;

    /**
     * 分类id
     */
    @Column(name = "class_id")
    private Integer classId;

    /**
     * 分类
     */
    @Column(name = "class_name")
    private String className;

    /**
     * 1级
     */
    @Column(name = "one_level")
    private String oneLevel;

    /**
     * 2级
     */
    @Column(name = "two_level")
    private String twoLevel;

    /**
     * 3级
     */
    @Column(name = "three_level")
    private String threeLevel;

    /**
     * 4级
     */
    @Column(name = "four_level")
    private String fourLevel;

    /**
     * 类型
     */
    private Integer type;

}