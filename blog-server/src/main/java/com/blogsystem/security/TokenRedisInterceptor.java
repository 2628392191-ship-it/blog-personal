package com.blogsystem.security;

import cn.dev33.satoken.exception.NotLoginException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT + Redis 二级校验拦截器。
 * Sa-Token JWT 模式只校验签名和过期，不查 Redis。
 * 此拦截器在 SaInterceptor 之后运行，额外检查 Redis 中是否仍存有该 token。
 * 管理员从 Redis 删除会话后，下一次请求立即被拦截返回 401。
 */
@Slf4j
public class TokenRedisInterceptor implements HandlerInterceptor {

    private static final String REDIS_KEY_PREFIX = "Authorization:login:token:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) return true;

        try {
            WebApplicationContext ctx = WebApplicationContextUtils
                    .getRequiredWebApplicationContext(request.getServletContext());
            StringRedisTemplate redis = ctx.getBean(StringRedisTemplate.class);
            if (Boolean.FALSE.equals(redis.hasKey(REDIS_KEY_PREFIX + token))) {
                throw new NotLoginException("admin", "token", "凭证已失效，请重新登录");
            }
        } catch (NotLoginException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis 令牌校验异常，降级放行", e);
        }
        return true;
    }
}
