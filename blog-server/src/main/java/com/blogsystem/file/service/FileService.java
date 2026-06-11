package com.blogsystem.file.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blogsystem.file.entity.FileRecord;
import com.blogsystem.file.mapper.FileRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRecordMapper fileRecordMapper;

    @Value("${blog.upload.dir:uploads}")
    private String uploadDir;

    @Value("${blog.upload.public-url:}")
    private String publicUrl;

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final long MAX_SIZE = 2 * 1024 * 1024;

    private Path resolveUploadDir() {
        Path p = Path.of(uploadDir);
        if (!p.isAbsolute()) {
            p = Path.of(System.getProperty("user.dir")).resolve(uploadDir);
        }
        return p;
    }

    public Map<String, String> upload(MultipartFile file) throws IOException {
        return upload(file, "avatars");
    }

    public Map<String, String> upload(MultipartFile file, String subDir) throws IOException {
        if (subDir == null || subDir.isBlank()) subDir = "avatars";
        if (file.isEmpty()) throw new IllegalArgumentException("文件为空");

        String original = file.getOriginalFilename();
        if (original == null || !original.contains(".")) throw new IllegalArgumentException("文件名非法");

        String ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXT.contains(ext)) throw new IllegalArgumentException("仅支持 jpg/png/webp/gif");

        if (file.getSize() > MAX_SIZE) throw new IllegalArgumentException("文件不能超过 2MB");

        Long userId = StpUtil.getLoginIdAsLong();
        String filename = userId + "_" + System.currentTimeMillis() + "." + ext;
        // 根据给定的路径创建路径
        Path dir = resolveUploadDir().resolve(subDir);
        // 根据路径创建目录
        Files.createDirectories(dir);
        // 指定目标文件
        Path target = dir.resolve(filename);

        String md5;
        try (InputStream is = file.getInputStream()) {
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
        }
        try (InputStream is = Files.newInputStream(target)) {
            md5 = DigestUtils.md5DigestAsHex(is);
        }

        String url = (publicUrl != null && !publicUrl.isBlank() ? publicUrl : "")
                + "/uploads/" + subDir + "/" + filename;

        FileRecord record = new FileRecord();
        record.setFileName(original);
        record.setFileUrl(url);
        record.setFilePath(target.toString());
        record.setFileType(ext);
        record.setFileSize(file.getSize());
        record.setStorageType("local");
        record.setMd5(md5);
        record.setUploaderId(userId);
        record.setCreatedAt(LocalDateTime.now());
        fileRecordMapper.insert(record);

        return Map.of("url", url);
    }

    public void deleteByUrl(String url) {
        if (url == null || !url.contains("/uploads/")) return;

        FileRecord record = fileRecordMapper.selectOne(
                new LambdaQueryWrapper<FileRecord>().eq(FileRecord::getFileUrl, url));
        if (record == null) return;

        try {
            Files.deleteIfExists(Path.of(record.getFilePath()));
        } catch (IOException e) {
            log.warn("删除文件失败: {}", record.getFilePath(), e);
        }
        fileRecordMapper.deleteById(record.getId());
    }
}
