package cn.com.glsx.shield.modules.converter;

import cn.com.glsx.shield.modules.entity.User;
import cn.com.glsx.shield.modules.model.param.UserBO;
import cn.com.glsx.shield.modules.model.view.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 解耦dao与api的实体映射
 * 字段名不一样，用@Mapping来处理
 * https://github.com/mapstruct/mapstruct-examples
 *
 * @author payu
 */
@Mapper
public interface UserConverter {

    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    UserDTO do2dto(User user);

    User dto2do(UserDTO userDTO);

    UserBO do2bo(User user);

    User bo2do(UserBO userBO);

    /**
     * do转Fegin Object-Oriented
     *
     * @param user
     * @return
     */
    @Mapping(source = "departmentId", target = "orgId")
    cn.com.glsx.usercenter.api.dto.response.User do2foo(User user);

}
