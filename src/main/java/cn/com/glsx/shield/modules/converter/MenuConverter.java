package cn.com.glsx.shield.modules.converter;

import cn.com.glsx.shield.modules.entity.Menu;
import cn.com.glsx.shield.modules.model.param.MenuBO;
import cn.com.glsx.shield.modules.model.view.MenuDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MenuConverter {

    MenuConverter INSTANCE = Mappers.getMapper(MenuConverter.class);

    MenuDTO do2dto(Menu menu);

    Menu bo2do(MenuBO menuBO);

}
