package cn.com.glsx.shield.modules.service;

import cn.com.glsx.Application;
import cn.com.glsx.shield.modules.entity.User;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void userInfo() {
        User user = userService.userInfo(1L);
        System.out.println("user = " + JSONUtil.toJsonStr(user));
    }
}