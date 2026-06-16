package com.blogsystem.common;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public ApiResponse<Void> handleNotLogin(NotLoginException e) {
        return ApiResponse.fail(401, "请先登录");
    }

    @ExceptionHandler(NotPermissionException.class)
    public ApiResponse<Void> handleNotPermission(NotPermissionException e) {
        return ApiResponse.fail(403, "权限不足");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException e) {
        return ApiResponse.fail(400, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError() == null
                ? "参数校验失败"
                : e.getBindingResult().getFieldError().getDefaultMessage();
        return ApiResponse.fail(400, msg);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResponse<Void> handleNoResource(NoResourceFoundException e) {
        return ApiResponse.fail(404, "Not Found");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e, HttpServletRequest request) {
        String uri = request.getRequestURI();
        // 忽略扫描器探测路径，不打印堆栈
        if (isScannerPath(uri)) {
            return ApiResponse.fail(404, "Not Found");
        }
        log.error("Unhandled error on {}", uri, e);
        return ApiResponse.fail(500, "系统繁忙");
    }

    private boolean isScannerPath(String uri) {
        if (uri == null) return false;
        String u = uri.toLowerCase();
        return u.contains(".cgi") || u.contains(".php") || u.contains(".asp")
            || u.contains("wp-admin") || u.contains("wp-login")
            || u.contains("phpmyadmin") || u.contains("adminer")
            || u.contains(".env") || u.contains("actuator")
            || u.contains("geoserver") || u.contains("solr")
            || u.contains("/vendor/") || u.contains("/.git/");
    }
}
