package com.smileshark.ai.service.impl;

import com.smileshark.ai.adviser.CustomizationMemoryAdviser;
import com.smileshark.ai.service.AIService;
import com.smileshark.entity.Role;
import com.smileshark.entity.Session;
import com.smileshark.service.RoleService;
import com.smileshark.websocket.endpoint.UserServiceEndpoint;
import com.smileshark.websocket.message.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
    private final ChatClient chatClient;
    private final RoleService roleService;
    private final CustomizationMemoryAdviser memoryAdviser;
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
    public void chat(Session chatSession, ChatMessage message, UserServiceEndpoint userServiceEndpoint) throws IllegalAccessException {
        // RAG检索专业知识回答
        // 查询到商户设置的客服角色信息
        Role role = roleService.getRoleByCtId(chatSession.getCtId());
        // 进行模板的关键词插入
        PromptTemplate template = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().build())
                .resource(customerServiceRoleResource)
                .build();
        HashMap<String, Object> pos = new HashMap<>();
        // 使用的反射的方式将需要填入的值提取出来
        Class<? extends Role> aClass = role.getClass();
        for (Field field : aClass.getFields()) {
            // 判断属性的类型为String才使用
            if (field.getType() == String.class) {
                field.setAccessible(true);
                pos.put(field.getName(), field.get(role));
            }
        }
        // 使用ChatMemoryIdAdvisor传递会话ID，确保线程安全
        chatClient.prompt()
                .system(template.render(pos))
                .user(message.getMessage())
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatSession.getId()))
                .advisors(memoryAdviser)
                .call();


    }
}
