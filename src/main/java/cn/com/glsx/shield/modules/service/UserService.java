package cn.com.glsx.shield.modules.service;

import cn.com.glsx.shield.modules.entity.User;
import cn.com.glsx.shield.modules.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author liuyf
 * @desc 用户信息
 * @date 2019年10月24日 下午2:37:40
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User userInfo(Long id) {
        return userMapper.selectById(id);
    }

    public User getByToken() {
        return userMapper.selectById(1L);
    }
}
