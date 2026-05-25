package com.blogsystem.file.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.blogsystem.common.ApiResponse;
import com.blogsystem.file.service.FileService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @SaCheckLogin
    @PostMapping("/upload")
    public ApiResponse<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                                    @RequestParam(required = false) String subDir) throws IOException {
        return ApiResponse.ok(fileService.upload(file, subDir));
    }

    @SaCheckLogin
    @DeleteMapping("/delete")
    public ApiResponse<Void> delete(@RequestParam @NotBlank String url) {
        fileService.deleteByUrl(url);
        return ApiResponse.ok();
    }
}
