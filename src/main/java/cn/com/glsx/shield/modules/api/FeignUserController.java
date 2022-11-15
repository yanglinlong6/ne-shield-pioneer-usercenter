package cn.com.glsx.shield.modules.api;

import cn.com.glsx.shield.modules.converter.UserConverter;
import cn.com.glsx.shield.modules.service.UserService;
import cn.com.glsx.usercenter.api.dto.request.FindUserRequest;
import cn.com.glsx.usercenter.api.dto.response.User;
import com.glsx.plat.core.web.R;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * feign用户服务接口
 *
 * @author payu
 */
@Slf4j
@Api(value = "系统用户微服务API", tags = {"系统用户微服务API"})
@RestController
@RequestMapping(value = "/fuser")
public class FeignUserController implements cn.com.glsx.usercenter.api.feign.UserService {

    @Autowired
    private UserService userService;

    @Override
    @ApiOperation(value = "全部用户查询")
    @GetMapping(value = "/findAll", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<List<User>> findAll() {
        List<User> userList = Lists.newArrayList();
        List<cn.com.glsx.shield.modules.entity.User> dbUserList = userService.getAllUserWithinAuthority();
        dbUserList.forEach(u -> userList.add(UserConverter.INSTANCE.do2foo(u)));
        return R.ok().data(userList);
    }

    @Override
    @ApiOperation(value = "系统用户查询")
    @GetMapping(value = "/getById")
    public R<User> getById(@RequestParam("id") Long id) {
        cn.com.glsx.shield.modules.entity.User u = userService.getById(id);
        User user = null;
        if (u != null) {
            user = UserConverter.INSTANCE.do2foo(u);
        }
        return R.ok().data(user);
    }

    @Override
    @GetMapping(value = "/getSuperiorById")
    public R<User> getSuperiorById(@RequestParam("id") Long userId) {
        cn.com.glsx.shield.modules.entity.User u = userService.getSuperiorUserById(userId);
        User user = null;
        if (u != null) {
            user = UserConverter.INSTANCE.do2foo(u);
            user.setMid(userId);
        }
        return R.ok().data(user);
    }

    @Override
    @PostMapping(value = "/getSuperiorByIds")
    public R<List<User>> getSuperiorByIds(@RequestBody Collection<Long> userIds) {
        List<User> userList = userService.getSuperiorUserByIds(userIds);
        return R.ok().data(userList);
    }

    @Override
    @GetMapping(value = "/getSubordinateById")
    public R<Set<Long>> getSubordinateById(@RequestParam("id") Long userId) {
        Set<Long> userIdSet = userService.getSubordinateById(userId);
        return R.ok().data(userIdSet);
    }

    @Override
    @ApiOperation(value = "系统用户列表查询")
    @GetMapping("/findByUser")
    public R<List<User>> findByUser(@RequestBody FindUserRequest request) {
        List<User> userList = Lists.newArrayList();
        List<cn.com.glsx.shield.modules.entity.User> dbUserList = userService.findUsers(request);
        dbUserList.forEach(u -> userList.add(UserConverter.INSTANCE.do2foo(u)));
        return R.ok().data(userList);
    }

    @Override
    @ApiOperation(value = "组织下系统用户查询")
    @GetMapping(value = "/findByOrg")
    public R<List<User>> findByOrg(@RequestBody FindUserRequest request) {
        List<User> userList = Lists.newArrayList();
        List<cn.com.glsx.shield.modules.entity.User> dbUserList = userService.findByOrg(request);
        dbUserList.forEach(u -> userList.add(UserConverter.INSTANCE.do2foo(u)));
        return R.ok().data(userList);
    }

    @Override
    @ApiOperation(value = "组织下系统所有下级用户查询")
    @GetMapping(value = "/findAllByOrg")
    public R<List<User>> findAllByOrg(@RequestBody FindUserRequest request) {
        List<User> userList = Lists.newArrayList();
        List<cn.com.glsx.shield.modules.entity.User> dbUserList = userService.findAllSubByOrg(request);
        dbUserList.forEach(u -> userList.add(UserConverter.INSTANCE.do2foo(u)));
        return R.ok().data(userList);
    }

}
