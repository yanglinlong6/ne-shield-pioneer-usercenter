package cn.com.glsx.shield.modules;

import cn.com.glsx.shield.common.exception.UserCenterException;
import cn.com.glsx.shield.modules.entity.User;
import cn.com.glsx.shield.modules.model.LoginBO;
import cn.com.glsx.shield.modules.model.view.UserDTO;
import cn.com.glsx.shield.modules.service.UserService;
import com.glsx.plat.common.annotation.SysLog;
import com.glsx.plat.common.enums.OperateType;
import com.glsx.plat.common.utils.SnowFlake;
import com.glsx.plat.core.constant.BasicConstants;
import com.glsx.plat.core.web.R;
import com.glsx.plat.redis.utils.RedisUtils;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author payu
 */
@Slf4j
@RefreshScope
@RestController
public class LoginController {

    @Value("${captcha.open}")
    private Boolean captchaOpen;

    @Value("${captcha.text:1111}")
    private String captchaText;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private Producer producer;

    @Autowired
    private UserService userService;

    @GetMapping("captcha")
    public R captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        response.setHeader("Cache-Control", "no-store, no-cache");
//        response.setContentType("image/jpeg");

        String captchaSerialNumber = SnowFlake.nextSerialNumber();
        response.setHeader(BasicConstants.REQUEST_HEADERS_CAPTCHA, captchaSerialNumber);

        //生成文字验证码
        String text;
        if (captchaOpen) {
            text = producer.createText();

            redisUtils.setex(Constants.KAPTCHA_SESSION_KEY + "_" + captchaSerialNumber, text, 60);
        } else {
            text = captchaText;
        }

        //生成图片验证码
        BufferedImage image = producer.createImage(text);

        //保存到 session,单机模式，集群会有问题
//        request.getSession().setAttribute(Constants.KAPTCHA_SESSION_KEY, text);
//        ServletOutputStream out = response.getOutputStream();
//        ImageIO.write(image, "jpg", out);

        //输出流
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", stream);
        String imgBase64 = Base64.getEncoder().encodeToString(stream.toByteArray());
        Map<String, String> captchaMap = new HashMap<>();
        captchaMap.put("text", text);
        captchaMap.put("captcha", imgBase64);
        captchaMap.put(BasicConstants.REQUEST_HEADERS_CAPTCHA, captchaSerialNumber);
        return R.ok().data(captchaMap);
    }

    /**
     * 登录
     */
    @SysLog(module = "系统登录", action = OperateType.LOGIN, saveRequest = false)
    @PostMapping(value = "/login")
    public R login(@RequestBody @Valid LoginBO loginBO) {

        verifyCaptcha(loginBO.getCaptcha(), loginBO.getCaptchaId());

        User user = userService.findByAccount(loginBO.getAccount());

        userService.verify(user, loginBO.getPassword());

        Map<String, Object> rtnMap = new HashMap<>();
        String token = userService.createToken(user);
        rtnMap.put("token", token);

        UserDTO userInfo = userService.userInfo(user.getId());
        rtnMap.put("user", userInfo);
        return R.ok().data(rtnMap);
    }

    /**
     * 验证码校验
     *
     * @param captcha
     * @param captchaId
     */
    protected void verifyCaptcha(String captcha, String captchaId) {
        if (captchaOpen) {
//            HttpServletRequest request = SessionUtils.request();
//            // 从session中获取图形吗字符串
//            Object kaptcha = request.getSession().getAttribute(com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY);
//            //删除缓存验证码
//            request.getSession().removeAttribute(com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY);

            String key = Constants.KAPTCHA_SESSION_KEY + "_" + captchaId;

            String kaptcha = (String) redisUtils.get(key);

            redisUtils.del(key);
            // 校验
            if (kaptcha == null || !kaptcha.equals(captcha)) {
                throw new UserCenterException("验证码错误");
            }
        }
    }

    /**
     * 登出
     */
    @SysLog(module = "系统登出", action = OperateType.LOGOUT)
    @GetMapping(value = "/logout")
    public R logout() {
        userService.removeToken();
        return R.ok();
    }

}
