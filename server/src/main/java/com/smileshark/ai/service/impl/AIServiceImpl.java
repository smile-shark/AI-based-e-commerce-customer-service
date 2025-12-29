package com.smileshark.ai.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.smileshark.ai.adviser.CustomizationMemoryAdviser;
import com.smileshark.ai.service.AIService;
import com.smileshark.entity.Role;
import com.smileshark.entity.Session;
import com.smileshark.entity.SessionLog;
import com.smileshark.service.RoleService;
import com.smileshark.websocket.endpoint.UserServiceEndpoint;
import com.smileshark.websocket.message.ChatMessage;
import jakarta.websocket.EncodeException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;

import static com.smileshark.code.DocumentCode.GOODS_ID;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
    private final ChatClient chatClient;
    private final RoleService roleService;
    private final CustomizationMemoryAdviser memoryAdviser;
    private final DashScopeChatModel dashscopeChatModel;
    private final VectorStore vectorStore;
    @Value("classpath:template/convert-to-manual-judgment-prompts.st")
    private Resource convertToManualJudgmentPromptsResource;
    @Value("classpath:template/customer-service-role.st")
    private Resource customerServiceRoleResource;

    @Override
    public void turnToManualJudgment(Session chatSession, ChatMessage message) {
        // 添加转人工判断逻辑
        String result = chatClient.prompt()
                .system(convertToManualJudgmentPromptsResource)
                .user(message.getMessage())
                .call()
                .content();
        System.out.println("转人工判断逻辑：" + result);
        // 将AI返回的字符串转换为布尔值
        boolean needTransfer = false;
        if (result != null) {
            needTransfer = Boolean.parseBoolean(result.trim());
        }
        if (needTransfer) {
            chatSession.setConversationStatus(Session.ConversationStatus.HUMAN);
        }
    }

    @Override
    public void chat(Session chatSession, ChatMessage message, UserServiceEndpoint userServiceEndpoint) throws IllegalAccessException, EncodeException, IOException {
        // RAG检索专业知识回答
        // 查询到商户设置的客服角色信息
        Role role = roleService.getRoleByCtId(chatSession.getCtId());
        // 进行模板的关键词插入
        PromptTemplate template = PromptTemplate.builder()
                // 文件中使用的是 <> 作为占位符，这样的配置更加的灵活
                .renderer(StTemplateRenderer.builder()
                        .startDelimiterToken('<')
                        .endDelimiterToken('>')
                        .build())
                .resource(customerServiceRoleResource)
                .build();
        HashMap<String, Object> pos = new HashMap<>();
        // 使用的反射的方式将需要填入的值提取出来
        Class<? extends Role> aClass = role.getClass();
        for (Field field : aClass.getDeclaredFields()) {
            // 判断属性的类型为String才使用
            if (field.getType() == String.class) {
                field.setAccessible(true);
                pos.put(field.getName(), field.get(role));
            }
        }

        Flux<ChatResponse> chatResponse = chatClient.prompt()
                .system(template.render(pos))
                .user(message.getMessage())
                // 传递会话ID作为记忆的唯一键
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatSession.getId()))
                .advisors(
                        memoryAdviser,
                        RetrievalAugmentationAdvisor.builder()
                                .order(1) // 确保在记忆存储之后
                                // 配置文档检索器
                                .documentRetriever(
                                        VectorStoreDocumentRetriever.builder()
                                                // 设置相似度阈值 大于0.5的才选用
                                                .similarityThreshold(.5)
                                                // 只选取最高的5条，减少token的使用
                                                .topK(5)
                                                // 配置过滤器，只选取商品ID为当前商品的数据
                                                .filterExpression(
                                                        new Filter.Expression(
                                                                Filter.ExpressionType.EQ,
                                                                new Filter.Key(GOODS_ID),
                                                                new Filter.Value(chatSession.getGoodsId())
                                                        )
                                                )
                                                .vectorStore(vectorStore)
                                                .build()
                                )
                                // 配置查询增强器
                                .queryAugmenter(
                                        // 不允许空上下文，如果检索不到内容，会触发空上下文处理逻辑，而不是AI出现幻觉回答（胡乱猜测）
                                        ContextualQueryAugmenter.builder()
                                                // emptyContextPromptTemplate 只在allowEmptyContext为false时生效
                                                .allowEmptyContext(false)
                                                .emptyContextPromptTemplate(
                                                        PromptTemplate.builder()
                                                                .template("用户查询的位于知识库之外，礼貌的告知用户无法回答")
                                                                .build()
                                                )
                                                .build()
                                )
                                .queryTransformers(
                                        // 使用RewriteQueryTransformer将用户查询重写为搜索引擎查询，防止语义模糊导致的检索精度下降
                                        RewriteQueryTransformer.builder()
                                                .chatClientBuilder(ChatClient.builder(dashscopeChatModel))
                                                .targetSearchSystem("商品客服小助手")
                                                .promptTemplate(
                                                        // 这里如果不设置自定义的模板，那么就会使用默认的模板，模板中要求 target,query 两个参数，所以必须查询
                                                        PromptTemplate.builder()
                                                                .template("""
                                                                        你是一个{target}的搜索查询专家。
                                                                        请将用户的模糊查询重写为一个清晰、简洁、独立的搜索查询，只输出重写后的查询，不要添加任何解释。
                                                                        用户查询：{query}
                                                                        重写后的搜索查询：
                                                                        """)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .stream()
                .chatResponse();
        for (ChatResponse response : chatResponse.toIterable()) {
            // 将AI回复的消息发送给客户
            userServiceEndpoint.sendMessage(ChatMessage.builder()
                    .sessionId(chatSession.getId())
                    .type(SessionLog.Type.ASSISTANT)
                    .message(response.getResult().getOutput().getText())
                    .build());
        }
        // 确认AI回复结束
        userServiceEndpoint.sendMessage(ChatMessage.builder().state(ChatMessage.State.END).build());
    }
}
