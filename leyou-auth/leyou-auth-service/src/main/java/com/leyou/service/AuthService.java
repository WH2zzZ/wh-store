package com.leyou.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.client.UserClient;
import com.leyou.config.JwtProperties;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties properties;

    public String accredit(String username, String password) {

        // 调用用户中心的查询接口，校验用户信息
        User user = this.userClient.queryUser(username, password);

        // 判断用户
        if (user == null) {
            return null;
        }

        // 创建userInfo对象
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());

        try {
            // 生成jwt
            return JwtUtils.generateToken(userInfo, this.properties.getPrivateKey(), this.properties.getExpire());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
