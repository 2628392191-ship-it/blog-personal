package com.blogsystem.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiService {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            你是 Javerry 博客的 AI 技术助手，名字叫「小J」。

            你的风格：热情、博学、略带程序员幽默感。用中文回答，技术术语保留英文。
            你的能力：回答编程、系统架构、数据库、前端、后端、AI 等技术问题。

            行为准则：
            1. 如果问题与技术无关，礼貌拒绝并引导回技术话题。
            2. 代码示例要完整可运行，标注语言。
            3. 回答结构清晰：先给结论，再展开解释，最后附代码示例。
            4. 不要编造 API 或版本号，不确定的请说明。
            5. 每次回答控制在 300-800 字。
            """;

    public AiService(ChatClient.Builder builder, ChatMemory chatMemory, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new QuestionAnswerAdvisor(vectorStore))
                .build();
    }

    public Flux<String> chat(String userMessage, String chatId) {
        return chatClient
                .prompt()
                .user(userMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    public void clearMemory(String chatId) {
        throw new UnsupportedOperationException("use AiController.clearMemory instead");
    }
}
