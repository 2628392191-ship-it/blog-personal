package com.blogsystem.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import com.blogsystem.security.TokenRedisInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${blog.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/uploads/**");
        registry.addInterceptor(new TokenRedisInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/sms-code");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path p = Path.of(uploadDir);
        if (!p.isAbsolute()) {
            p = Path.of(System.getProperty("user.dir")).resolve(uploadDir);
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(p.toAbsolutePath().toUri().toString());
    }
}
