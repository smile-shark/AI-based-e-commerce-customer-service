package com.smileshark.config;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import io.milvus.client.MilvusServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VectorStoreConfig {
    private final MilvusServiceClient milvusClient;
    private final DashScopeEmbeddingModel dashScopeEmbeddingModel;

    /**
     * 对向量数据库进行配置
     * <p>
     *     其实这里的配置是不必要的，再SpringBoot中，对于VectorStore是已经自动注入的了，
     *     这里主要演示多向量模型是会遇到的无法自动注入的问题。
     *     当有多个embeddingModel时（比如dashScopeEmbeddingModel和ollamaEmbeddingModel同时存在）
     *     程序就无法确定我们需要使用的是哪个模型，那么需要像这样手动配置
     * </p>
     * @return VectorStore
     */
    @Bean
    public VectorStore vectorStore() {
        return MilvusVectorStore.builder(milvusClient, dashScopeEmbeddingModel).build();
    }
}
