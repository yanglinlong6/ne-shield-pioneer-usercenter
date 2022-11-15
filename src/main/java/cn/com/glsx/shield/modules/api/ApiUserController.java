package cn.com.glsx.shield.modules.api;

import cn.com.glsx.shield.modules.converter.UserConverter;
import cn.com.glsx.shield.modules.service.UserService;
import cn.com.glsx.usercenter.api.dto.request.SimpleAddUser;
import cn.com.glsx.usercenter.api.dto.response.User;
import com.alibaba.fastjson.JSONObject;
import com.glsx.plat.core.web.R;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 对外开放的用户api接口，免登陆
 *
 * @author payu
 */
@Slf4j
@Api(value = "系统用户API", tags = {"系统用户API"})
@RestController
@RequestMapping(value = "/api/user")
public class ApiUserController implements cn.com.glsx.usercenter.api.feign.ApiUserService {

    @Autowired
    private UserService userService;

    @Override
    @GetMapping(value = "/findByAccount")
    public R<List<User>> findByAccount(@RequestParam("account") String account) {
        List<User> userList = Lists.newArrayList();
        cn.com.glsx.shield.modules.entity.User u = userService.findByAccount(account);
        userList.add(UserConverter.INSTANCE.do2foo(u));
        return R.ok().data(userList);
    }

    @Override
    @GetMapping(value = "/findByUsername")
    public R<List<User>> findByUsername(@RequestParam("username") String username) {
        List<User> userList = Lists.newArrayList();
        List<cn.com.glsx.shield.modules.entity.User> dbUserList = userService.findByUsername(username);
        dbUserList.forEach(u -> {
            userList.add(UserConverter.INSTANCE.do2foo(u));
        });
        return R.ok().data(userList);
    }

    @Override
    @PostMapping(value = "/simple/add")
    public R<User> addSimpleUser(@RequestBody SimpleAddUser simpleAddUser) {
        log.info("add simple user request:{}", JSONObject.toJSONString(simpleAddUser));
        Assert.hasLength(simpleAddUser.getAccount(), "帐号不可为空");
        Assert.hasLength(simpleAddUser.getUserName(), "用户名称不可为空");
        Assert.hasLength(simpleAddUser.getPassword(), "密码不可为空");
        Assert.notNull(simpleAddUser.getDepartmentId(), "部门不可为空");
        Assert.notNull(simpleAddUser.getRoleId(), "角色不可为空");

        User user = userService.syncAddSimpleUser(simpleAddUser);
        if (user != null) {
            return R.ok().data(user);
        }
        return R.error("用户同步创建失败");
    }

}
