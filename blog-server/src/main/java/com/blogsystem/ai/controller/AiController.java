package com.blogsystem.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.blogsystem.ai.service.AiService;
import com.blogsystem.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final ChatMemory chatMemory;

    @SaCheckLogin
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestBody Map<String, String> body) {
        return aiService.chat(body.get("message"), body.get("sessionId"))
                .map(chunk -> ServerSentEvent.<String>builder().data(chunk).build());
    }

    @SaCheckLogin
    @DeleteMapping("/memory/{sessionId}")
    public ApiResponse<Void> clearMemory(@PathVariable String sessionId) {
        chatMemory.clear(sessionId);
        return ApiResponse.ok();
    }
}
