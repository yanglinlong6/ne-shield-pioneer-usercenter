package cn.com.glsx.shield.modules.service;

import cn.com.glsx.shield.modules.mapper.CustUserLocalAccountMapper;
import cn.com.glsx.shield.modules.mapper.CustUserWechatAccountMapper;
import cn.com.glsx.shield.modules.mapper.UserMapper;
import cn.com.glsx.usercenter.api.dto.response.CUser;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CUserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private CustUserLocalAccountMapper custUserLocalAccountMapper;

    @Resource
    private CustUserWechatAccountMapper custUserWechatAccountMapper;

    /**
     * 获取本地C端账号用户信息
     *
     * @param cuserIds
     * @return
     */
    public List<CUser> getLocalUsers(Collection<Long> cuserIds) {
        if (CollectionUtils.isEmpty(cuserIds)) {
            return Lists.newArrayList();
        }
        return custUserLocalAccountMapper.selectByUserIds(cuserIds);
    }

    public Map<Long, CUser> getLocalUserMap(Collection<Long> userIds) {
        List<CUser> userModelList = this.getLocalUsers(userIds);

        return userModelList.stream().collect(Collectors.toMap(CUser::getUserId, Function.identity()));
    }

    /**
     * 获取后台账号用户信息
     *
     * @param relUserIds
     * @return
     */
    public List<CUser> getLocalRelUsers(Collection<Long> relUserIds) {
        if (CollectionUtils.isEmpty(relUserIds)) {
            return Lists.newArrayList();
        }
        return userMapper.selectByRelUserIds(relUserIds);
    }

    public Map<Long, CUser> getLocalRelUserMap(Collection<Long> relUserIds) {
        List<CUser> userModelList = this.getLocalRelUsers(relUserIds);

        return userModelList.stream().filter(um -> um.getRelUserId() != null).collect(Collectors.toMap(CUser::getRelUserId, Function.identity()));
    }

    /**
     * 获取微信账号用户信息
     *
     * @param cuserIds
     * @return
     */
    public List<CUser> getWechatUsers(Collection<Long> cuserIds) {
        if (CollectionUtils.isEmpty(cuserIds)) {
            return Lists.newArrayList();
        }
        return custUserWechatAccountMapper.selectByUserIds(cuserIds);
    }

    public Map<Long, CUser> getWechatUserMap(Collection<Long> cuserIds) {
        List<CUser> userModelList = this.getWechatUsers(cuserIds);

        return userModelList.stream().collect(Collectors.toMap(CUser::getUserId, Function.identity()));
    }

}
