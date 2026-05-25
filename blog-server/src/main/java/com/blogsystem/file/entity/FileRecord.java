package com.blogsystem.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_record")
public class FileRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileName;
    private String fileUrl;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String storageType;
    private String md5;
    private Long uploaderId;
    private LocalDateTime createdAt;
}
