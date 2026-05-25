package com.blogsystem.log.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.blogsystem.log.annotation.OpLog;
import com.blogsystem.log.entity.OperationLog;
import com.blogsystem.log.mapper.OperationLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final OperationLogMapper operationLogMapper;
    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;

    @Around("@annotation(com.blogsystem.log.annotation.OpLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        OpLog annotation = signature.getMethod().getAnnotation(OpLog.class);

        OperationLog log = new OperationLog();
        log.setModule(annotation.module());
        log.setAction(annotation.action());
        log.setContent(annotation.module() + "-" + annotation.action());
        log.setIp(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setCreatedAt(LocalDateTime.now());

        try {
            log.setUserId(StpUtil.getLoginIdAsLong());
        } catch (Exception ignored) {
        }

        try {
            String args = objectMapper.writeValueAsString(point.getArgs());
            if (args.length() > 500) args = args.substring(0, 500);
            log.setRequestData(args);
        } catch (Exception ignored) {
        }

        Object result = point.proceed();

        try {
            operationLogMapper.insert(log);
        } catch (Exception ignored) {
        }

        return result;
    }
}
