package com.leyou.controller;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.config.JwtProperties;
import com.leyou.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties properties;

    @PostMapping("accredit")
    public ResponseEntity<Void> accredit(
            @RequestParam("username") String username, @RequestParam("password") String password,
            HttpServletRequest request, HttpServletResponse response
    ) {
        // 调用service检验用户合法性
        String token = this.authService.accredit(username, password);

        // token为null，则说明校验失败
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 校验成功，把token信息放入cookie中
        CookieUtils.setCookie(request, response, this.properties.getCookieName(), token,
                this.properties.getExpire() * 60);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verifyUser(@CookieValue("LY_TOKEN") String token,
                                               HttpServletRequest request, HttpServletResponse response) {

        try {
            // 通过公钥解析token信息
            UserInfo user = JwtUtils.getInfoFromToken(token, this.properties.getPublicKey());

            // 重新生成jwt
            token = JwtUtils.generateToken(user, this.properties.getPrivateKey(), this.properties.getExpire());

            // 把重新生成的token，写入到cookie中
            CookieUtils.setCookie(request, response, this.properties.getCookieName(), token, this.properties.getExpire()*60);

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
