package cn.com.glsx.shield.modules.api;

import cn.com.glsx.auth.api.AuthFeignClient;
import cn.com.glsx.auth.model.MenuPermission;
import cn.com.glsx.auth.model.SimpleUser;
import cn.com.glsx.auth.model.SyntheticUser;
import cn.com.glsx.shield.common.exception.UserCenterException;
import cn.com.glsx.shield.modules.entity.User;
import cn.com.glsx.shield.modules.service.UserService;
import com.glsx.plat.core.web.R;
import com.glsx.plat.jwt.base.ComJwtUser;
import com.glsx.plat.jwt.util.JwtUtils;
import com.glsx.plat.web.utils.SessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping(value = "/auth")
public class AuthApiController implements AuthFeignClient {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils<ComJwtUser> jwtUtils;

    @Override
    public R<SimpleUser> login(String account, String password) {

        User user = userService.findByAccount(account);

        userService.verify(user, password);

        SimpleUser simpleUser = new SimpleUser();
        simpleUser.setId(user.getId());
        simpleUser.setAccount(user.getAccount());
        simpleUser.setUsername(user.getUsername());
        return R.ok().data(simpleUser);
    }

    @Override
    public R<SimpleUser> getSimpleUserById(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new UserCenterException("用户不存在");
        }

        SimpleUser simpleUser = new SimpleUser();
        simpleUser.setId(user.getId());
        simpleUser.setAccount(user.getAccount());
        simpleUser.setUsername(user.getUsername());
        return R.ok().data(simpleUser);
    }

    @Override
    public R<Boolean> sniff() {
        String token = SessionUtils.request().getHeader(HttpHeaders.AUTHORIZATION);
        boolean verifyFlag = jwtUtils.verifyToken(token);
        return R.ok().data(verifyFlag);
    }

    @Override
    public R<SyntheticUser> getAuthUser() {
        SyntheticUser authUser = userService.getSyntheticUserByToken();
        return R.ok().data(authUser);
    }

    @Override
    public R<SyntheticUser> getAuthUserById(Long userId) {
        SyntheticUser authUser = userService.getSyntheticUserById(userId);
        return R.ok().data(authUser);
    }

    @Override
    public R<Set<Long>> getAuthDeptIds() {
        // TODO: 2021/6/10 单独查询
        SyntheticUser authUser = userService.getSyntheticUserByToken();
        return R.ok().data(authUser.getVisibleDeptIds());
    }

    @Override
    public R<Set<Long>> getAuthUserIds() {
        // TODO: 2021/6/10 单独查询
        SyntheticUser authUser = userService.getSyntheticUserByToken();
        return R.ok().data(authUser.getVisibleCreatorIds());
    }

    @Override
    public R<List<MenuPermission>> getPermMenus() {
        // TODO: 2021/6/10 单独查询
        SyntheticUser authUser = userService.getSyntheticUserByToken();
        List<MenuPermission> list = userService.getPermissionMenus(authUser);
        return R.ok().data(list);
    }

}
