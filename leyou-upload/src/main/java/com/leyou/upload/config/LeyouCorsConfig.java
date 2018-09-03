package com.leyou.upload.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 统一解决cors跨域
 */
@Configuration
public class LeyouCorsConfig {

    @Bean
    public CorsFilter corsFilter(){

        // 允许请求的头信息
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://manage.leyou.com");
        configuration.setAllowCredentials(true);
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");

        // 允许请求过滤的路径
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", configuration);

        // 要注入的cors过滤器
        return new CorsFilter(configurationSource);
    }
}
