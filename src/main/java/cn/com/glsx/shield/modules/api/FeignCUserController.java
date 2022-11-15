package cn.com.glsx.shield.modules.api;

import cn.com.glsx.shield.modules.service.CUserService;
import cn.com.glsx.usercenter.api.dto.response.CUser;
import com.glsx.plat.core.web.R;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

/**
 * feign用户服务接口
 *
 * @author payu
 */
@Slf4j
@Api(value = "C端用户微服务API", tags = {"C端用户微服务API"})
@RestController
@RequestMapping(value = "/fcuser")
public class FeignCUserController implements cn.com.glsx.usercenter.api.feign.CUserService {

    @Autowired
    private CUserService cUserService;

    @Override
    public R<List<CUser>> getLocalCUsers(Collection<Long> cuserIds) {
        List<CUser> cUserList = cUserService.getLocalUsers(cuserIds);
        return R.ok().data(cUserList);
    }

    @Override
    public R<List<CUser>> getLocalRelCUsers(Collection<Long> relUserIds) {
        List<CUser> cUserList = cUserService.getLocalRelUsers(relUserIds);
        return R.ok().data(cUserList);
    }

    @Override
    public R<List<CUser>> getWechatUsers(Collection<Long> cuserIds) {
        List<CUser> cUserList = cUserService.getWechatUsers(cuserIds);
        return R.ok().data(cUserList);
    }

}
