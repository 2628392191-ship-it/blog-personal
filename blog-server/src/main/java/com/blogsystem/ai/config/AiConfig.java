package com.blogsystem.ai.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blogsystem.ai.memory.RedisChatMemory;
import com.blogsystem.content.entity.Article;
import com.blogsystem.content.mapper.ArticleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class AiConfig {

    @Bean
    public ChatMemory chatMemory(StringRedisTemplate redisTemplate) {
        return new RedisChatMemory(redisTemplate);
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel, ArticleMapper articleMapper) {
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().eq(Article::getDeleted, 0)
                        .eq(Article::getStatus, 1));
        List<Document> allChunks = new ArrayList<>();
        for (Article a : articles) {
            if (a.getContentMd() == null || a.getContentMd().length() < 100) continue;
            Document doc = new Document(a.getContentMd(),
                    Map.of("articleId", String.valueOf(a.getId()),
                           "title", a.getTitle() != null ? a.getTitle() : ""));
            TokenTextSplitter splitter = new TokenTextSplitter(200, 200, 50, 1, true);
            allChunks.addAll(splitter.apply(List.of(doc)));
        }
        if (!allChunks.isEmpty()) {
            store.add(allChunks);
            log.info("Loaded {} document chunks from {} articles into VectorStore",
                    allChunks.size(), articles.size());
        }
        return store;
    }
}
