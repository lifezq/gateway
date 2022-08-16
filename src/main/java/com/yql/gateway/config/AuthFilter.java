package com.yql.gateway.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * @Author Ryan
 * @Date 2022/8/1
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String TOKEN_HEADER_KEY = "auth_token";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        /*
        // 1.获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        // 2.获取token
        String token = getToken(request);
        ServerHttpResponse response = exchange.getResponse();
        if (StringUtils.isBlank(token)) {
            // 3.token为空 返回401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        // 4.验证token是否有效
        String userId = getUserIdByToken(token);
        if (StringUtils.isBlank(userId)) {
            // 5.token无效 返回401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        // token有效，后续业务处理
        // 从写请求头，方便业务系统从请求头获取用户id进行权限相关处理
        ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
        request = builder.header("user_id", userId).build();
        // 延长缓存过期时间-token缓存用户如果一直在操作就会一直重置过期
        // 这样避免用户操作过程中突然过期影响业务操作及体验，只有用户操作间隔时间大于缓存过期时间才会过期
        resetTokenExpirationTime(token, userId);
         */
        // 完成验证
        return chain.filter(exchange);
    }


    @Override
    public int getOrder() {
        // 优先级 越小越优先
        return 0;
    }

    /**
     * 从redis中获取用户id
     * 在登录操作时候 登陆成功会生成一个token, redis得key为auth_token:token 值为用户id
     *
     * @param token
     * @return
     */
    private String getUserIdByToken(String token) {
        String redisKey = String.join(":", "auth_token", token);
        return redisTemplate.opsForValue().get(redisKey);
    }

    /**
     * 重置token过期时间
     *
     * @param token
     * @param userId
     */
    private void resetTokenExpirationTime(String token, String userId) {
        String redisKey = String.join(":", "auth_token", token);
        redisTemplate.opsForValue().set(redisKey, userId, 2, TimeUnit.HOURS);
    }


    /**
     * 获取token
     *
     * @param request
     * @return
     */
    private static String getToken(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        // 从请求头获取token
        String token = headers.getFirst(TOKEN_HEADER_KEY);
        if (StringUtils.isBlank(token)) {
            // 请求头无token则从url获取token
            token = request.getQueryParams().getFirst(TOKEN_HEADER_KEY);
        }
        if (StringUtils.isBlank(token)) {
            // 请求头和url都没有token则从cookies获取
            HttpCookie cookie = request.getCookies().getFirst(TOKEN_HEADER_KEY);
            if (cookie != null) {
                token = cookie.getValue();
            }
        }
        return token;
    }
}