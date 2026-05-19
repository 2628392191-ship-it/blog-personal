package com.blogsystem.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sms_code_log")
public class SmsCodeLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String phone;
    private String bizType;
    private String codeHash;
    private LocalDateTime expiresAt;
    private LocalDateTime sentAt;
    private Integer status;
    private String requestIp;
}
