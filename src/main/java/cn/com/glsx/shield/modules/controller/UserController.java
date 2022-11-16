package cn.com.glsx.shield.modules.controller;

import cn.com.glsx.shield.modules.entity.User;
import cn.com.glsx.shield.modules.service.UserService;
import com.glsx.plat.core.web.R;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * 单体应用Controller处理统一返回体
 *
 * @author payu
 */
@Slf4j
@RestController
@RequestMapping(value = "/user")
@Api(value = "用户管理", tags = {"用户管理"})
public class UserController {

    private final static String MODULE = "用户管理";

    @Autowired
    private UserService userService;


    @GetMapping(value = "/info")
    public R info(@RequestParam("id") Long id) {
        User user = userService.userInfo(id);
        return R.ok().data(user);
    }

}
