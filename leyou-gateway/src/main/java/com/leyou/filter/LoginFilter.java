package com.leyou.filter;

import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.config.JwtProperties;
import com.leyou.config.PathProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@EnableConfigurationProperties({JwtProperties.class, PathProperties.class})
public class LoginFilter extends ZuulFilter {

    @Autowired
    private JwtProperties properties;

    @Autowired
    private PathProperties pathProperties;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 5;
    }

    @Override
    public boolean shouldFilter() {

        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        // 获取请求中路径
        StringBuffer requestURL = request.getRequestURL();

        // 判断当前的请求路径是否在白名单中
        for (String url : this.pathProperties.getAllowPaths()) {

            if (StringUtils.contains(requestURL.toString(), url)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Object run() throws ZuulException {

        // 获取zuul上下文
        RequestContext context = RequestContext.getCurrentContext();
        try {
            // 获取请求对象
            HttpServletRequest request = context.getRequest();
            // 获取token信息
            String token = CookieUtils.getCookieValue(request, this.properties.getCookieName());

            // 解析token信息
            JwtUtils.getInfoFromToken(token, this.properties.getPublicKey());

        } catch (Exception e) {
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
            e.printStackTrace();
        }
        return null;
    }
}
