package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptors.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GoodsClient goodsClient;

    private static final String LEYOU_CART_PREFIX = "leyou:cart:";

    public void saveCart(Cart cart) {
        // 获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key = LEYOU_CART_PREFIX + userInfo.getId();
        // 获取该用户的购物车信息
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(key);

        String skuId = cart.getSkuId().toString();
        Integer num = cart.getNum();
        // 查询该购物车是否包含当前记录
        if(hashOperations.hasKey(skuId)){
            // 获取购物车中的该条记录
            String cartJson = hashOperations.get(skuId).toString();
            // 反序列化
            cart = JsonUtils.parse(cartJson, Cart.class);
            cart.setNum(cart.getNum() + num);
        } else {
            cart.setUserId(userInfo.getId());
            Sku sku = this.goodsClient.querySkuById(cart.getSkuId());
            cart.setPrice(sku.getPrice());
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setTitle(sku.getTitle());
            cart.setImage(StringUtils.isBlank(sku.getImages())? "" : StringUtils.split(sku.getImages(), ",")[0]);
        }
        hashOperations.put(skuId, JsonUtils.serialize(cart));
    }

    public List<Cart> queryCarts() {
        // 获取当前用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key = LEYOU_CART_PREFIX + userInfo.getId();
        // 获取该用户的购物车信息
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(key);
        List<Object> cartsJson = hashOperations.values();
        // 把json类型字符串集合 转化为List<Cart>
        return cartsJson.stream().map(cart -> JsonUtils.parse(cart.toString(), Cart.class)).collect(Collectors.toList());
    }
}
