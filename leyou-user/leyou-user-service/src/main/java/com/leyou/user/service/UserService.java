package com.leyou.user.service;


import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate template;

    private static final String USER_CODE_PREFIX = "leyou:user:code:";

    public Boolean checkUser(String data, Integer type) {

        User record = new User();
        switch (type) {
            case 1:
                record.setUsername(data); break;
            case 2:
                record.setPhone(data); break;
            default:
                return null; // 参数不合法
        }
        // 响应用户名或者手机号是否可用
        return this.userMapper.selectCount(record) == 0;
    }

    public void sendCode(String phone) {

        // 生成验证码
        String code = NumberUtils.generateCode(6);

        // 保存到redis中
        this.redisTemplate.opsForValue().set(USER_CODE_PREFIX + phone, code, 5, TimeUnit.MINUTES);

        // 发送消息给sms服务
        Map<String, String> map = new HashMap<>();
        map.put("phone", phone);
        map.put("code", code);
        this.template.convertAndSend("leyou.sms.exchange", "sms.register", map);
    }

    public Boolean register(User user, String code) {

        // 从redis中获取本地验证码
        String redisCode = this.redisTemplate.opsForValue().get(USER_CODE_PREFIX + user.getPhone());
        // 和用户提交的验证码，比较
        if (!StringUtils.equals(code, redisCode)) {
            return null;
        }
        // 校验通过，对密码加密
        String salt = CodecUtils.generateSalt();
        String password = CodecUtils.md5Hex(user.getPassword(), salt);

        // 保存用户信息
        user.setPassword(password);
        user.setSalt(salt);
        user.setCreated(new Date());
        return this.userMapper.insertSelective(user) == 1;
    }

    public User queryUser(String username, String password) {
        // 根据用户名查询用户
        User record = new User();
        record.setUsername(username);
        User user = this.userMapper.selectOne(record);

        // 如果用户为null
        if (user == null) {
            return  null;
        }

        // 比较密码是否正确
        password = CodecUtils.md5Hex(password, user.getSalt());
        if (!StringUtils.equals(password, user.getPassword())) {
            return null;
        }

        // 比较成功
        return user;
    }
}
