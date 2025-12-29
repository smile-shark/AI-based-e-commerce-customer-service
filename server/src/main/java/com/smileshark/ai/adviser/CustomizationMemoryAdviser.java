package com.smileshark.ai.adviser;

import com.smileshark.ai.memory.CustomizationMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomizationMemoryAdviser implements BaseChatMemoryAdvisor {
    private final CustomizationMemory customizationMemory;
    private final PromptTemplate systemPromptTemplate = new PromptTemplate("{instructions}\n\nUse the conversation memory from the MEMORY section to provide accurate answers.\n\n---------------------\nMEMORY:\n{memory}\n---------------------\n\n");
    private final static String defaultConversationId = "default";


    /**
     * 发送前的操作：
     * <p>
     * 此方法在发送消息前执行，主要功能包括：
     * </p>
     * <ol>
     *     <li>从聊天中提取发送的消息</li>
     *     <li>根据会话ID查询记忆</li>
     *     <li>首先从Redis中查询最近的几条记忆</li>
     *     <li>如果Redis中没有，则从MySQL中查询最近的10条记忆</li>
     *     <li>将当前记忆异步添加到Redis和MySQL中</li>
     *     <li>将查询到的记忆整合到新的prompt中</li>
     *     <li>返回增强后的prompt给chatClientRequest</li>
     * </ol>
     * @param chatClientRequest 待处理的ChatClientRequest对象
     * @param advisorChain   AdvisorChain对象，用于获取其他Advisor
     * @return ChatClientRequest对象，包含增强后的prompt
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        Map<String, Object> context = chatClientRequest.context();
        // 从请求上下文中获取会话ID
        String conversationId = getConversationId(chatClientRequest.context(), defaultConversationId);
        if ("default".equals(conversationId)) {
            throw new RuntimeException("会话ID不能为空");
        }
        // 根据会话id获取到对应的记忆
        List<Message> memoryMessages = customizationMemory.get(conversationId);
        // 将记忆添加到新的prompt中
        // 构建记忆消息
        String memory = memoryMessages.stream().filter((m) -> m.getMessageType() == MessageType.USER || m.getMessageType() == MessageType.ASSISTANT).map((m) -> {
            // 将身份和对应的内容拼接
            String identity = String.valueOf(m.getMessageType());
            return identity + ":" + m.getText();
        }).collect(Collectors.joining(System.lineSeparator()));
        // 获取系统提示词
        SystemMessage systemMessage = chatClientRequest.prompt().getSystemMessage();
        // 构建新的系统提示词 （提示词+记忆）
        String augmentedSystemText = this.systemPromptTemplate.render(Map.of("instructions", systemMessage.getText(), "memory", memory));
        // 复制一个聊天请求出来，并将增强的系统提示词添加到新的聊天请求中
        ChatClientRequest processedChatClientRequest = chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentSystemMessage(augmentedSystemText))
                .build();
        // 提取用户的消息进行存储 -- 这里不需要
        UserMessage userMessage = chatClientRequest.prompt().getUserMessage();
        this.customizationMemory.add(conversationId, userMessage);
        // 将新的记忆进行返回
        return processedChatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        // 这里的主要作用是将AI返回的聊天内容进行存储
        String conversationId = getConversationId(chatClientResponse.context(), defaultConversationId);
        if ("default".equals(conversationId)) {
            throw new RuntimeException("会话ID不能为空");
        }
        // 获取到AI返回的消息
        ChatResponse chatResponse = chatClientResponse.chatResponse();
        if (chatResponse == null) {
            throw new RuntimeException("AI返回的消息为空");
        }
        StringBuilder content = new StringBuilder();
        for (Generation result : chatResponse.getResults()) {
            content.append(result.getOutput().getText());
        }
        AssistantMessage assistantMessage = new AssistantMessage(content.toString());
        // 将AI返回的消息存储到本地
        this.customizationMemory.add(conversationId, assistantMessage);
        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        // 先执行 before（添加用户消息等）
        ChatClientRequest processedRequest = this.before(chatClientRequest, streamAdvisorChain);
        // 继续下游流式调用
        Flux<ChatClientResponse> streamedResponses = streamAdvisorChain.nextStream(processedRequest);
        return new ChatClientMessageAggregator().aggregateChatClientResponse(
                streamedResponses,
                aggregatedResponse -> {
                    // 只在整个流结束、内容完整时调用一次 after()
                    this.after(aggregatedResponse, streamAdvisorChain);
                }
        );
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
