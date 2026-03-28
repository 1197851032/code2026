package com.example.service;

import com.example.entity.ChatMessage;
import com.example.entity.Goods;
import com.example.mapper.ChatMessageMapper;
import com.example.mapper.GoodsMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 对话管理服务（带记忆功能）
 */
@Service
public class ConversationService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    @Autowired
    private GoodsMapper goodsMapper;
    
    private final OpenAiChatModel model;

    public ConversationService(@Value("${openai.api.key:}") String apiKey,
                              @Value("${openai.api.base-url:https://api.openai.com/v1}") String baseUrl,
                              @Value("${openai.model.name:gpt-3.5-turbo}") String modelName) {
        
        // 使用测试API
        this.model = OpenAiChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .build();
    }

    /**
     * 开始新对话
     * @param userId 用户ID
     * @return 会话ID
     */
    public String startNewConversation(Integer userId) {
        String sessionId = UUID.randomUUID().toString();
        System.out.println("用户 " + userId + " 开始新对话，会话ID: " + sessionId);
        return sessionId;
    }

    /**
     * 带记忆的聊天
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param message 用户消息
     * @return AI回复
     */
    public String chatWithMemory(Integer userId, String sessionId, String message) {
        try {
            // 1. 获取历史对话记录
            List<ChatMessage> history = getConversationHistory(sessionId);
            
            // 2. 查询相关商品数据
            List<Goods> relevantGoods = searchRelevantGoods(message);
            
            // 3. 构建带上下文的提示词
            String enhancedPrompt = buildContextualPrompt(message, history, relevantGoods);
            
            // 4. 调用AI模型
            UserMessage userMessage = UserMessage.from(enhancedPrompt);
            Response<AiMessage> response = model.generate(userMessage);
            String aiResponse = response.content().text();
            
            // 5. 保存对话记录
            saveChatMessage(userId, sessionId, message, aiResponse);
            
            return aiResponse;
            
        } catch (Exception e) {
            System.err.println("对话处理失败: " + e.getMessage());
            e.printStackTrace();
            return "抱歉，AI服务暂时不可用: " + e.getMessage();
        }
    }

    /**
     * 获取对话历史
     * @param sessionId 会话ID
     * @return 历史记录列表
     */
    public List<ChatMessage> getConversationHistory(String sessionId) {
        return chatMessageMapper.findBySessionId(sessionId);
    }

    /**
     * 获取用户的所有会话
     * @param userId 用户ID
     * @return 会话ID列表
     */
    public List<String> getUserSessions(Integer userId) {
        return chatMessageMapper.findSessionIdsByUserId(userId);
    }

    /**
     * 获取用户最近的对话记录
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 对话记录列表
     */
    public List<ChatMessage> getUserRecentMessages(Integer userId, int limit) {
        return chatMessageMapper.findByUserId(userId, limit);
    }

    /**
     * 清除会话记录
     * @param sessionId 会话ID
     * @return 清除结果
     */
    public boolean clearConversation(String sessionId) {
        try {
            int result = chatMessageMapper.deleteBySessionId(sessionId);
            System.out.println("清除会话 " + sessionId + "，删除 " + result + " 条记录");
            return result > 0;
        } catch (Exception e) {
            System.err.println("清除会话失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 清除用户所有对话记录
     * @param userId 用户ID
     * @return 清除结果
     */
    public boolean clearAllUserConversations(Integer userId) {
        try {
            int result = chatMessageMapper.deleteByUserId(userId);
            System.out.println("清除用户 " + userId + " 的所有对话，删除 " + result + " 条记录");
            return result > 0;
        } catch (Exception e) {
            System.err.println("清除用户对话失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 保存对话记录
     */
    private void saveChatMessage(Integer userId, String sessionId, String message, String response) {
        try {
            // 保存用户消息
            ChatMessage userMessage = new ChatMessage(userId, sessionId, message, null, "user");
            chatMessageMapper.insert(userMessage);
            
            // 保存AI回复
            ChatMessage aiMessage = new ChatMessage(userId, sessionId, null, response, "ai");
            chatMessageMapper.insert(aiMessage);
            
            System.out.println("保存对话记录成功，会话ID: " + sessionId);
        } catch (Exception e) {
            System.err.println("保存对话记录失败: " + e.getMessage());
        }
    }

    /**
     * 构建带上下文的提示词
     */
    private String buildContextualPrompt(String currentMessage, List<ChatMessage> history, List<Goods> relevantGoods) {
        StringBuilder promptBuilder = new StringBuilder();
        
        promptBuilder.append("你是一个专业的商品推荐助手，请基于对话历史和商品信息回答用户的问题。\n\n");
        
        // 添加对话历史
        if (!history.isEmpty()) {
            promptBuilder.append("=== 对话历史 ===\n");
            for (ChatMessage msg : history) {
                if ("user".equals(msg.getMessageType())) {
                    promptBuilder.append("用户: ").append(msg.getMessage()).append("\n");
                } else if ("ai".equals(msg.getMessageType())) {
                    promptBuilder.append("助手: ").append(msg.getResponse()).append("\n");
                }
            }
            promptBuilder.append("\n");
        }
        
        // 添加商品信息
        promptBuilder.append("=== 相关商品信息 ===\n");
        if (relevantGoods.isEmpty()) {
            promptBuilder.append("没有找到直接相关的商品信息。\n");
        } else {
            for (int i = 0; i < relevantGoods.size(); i++) {
                Goods goods = relevantGoods.get(i);
                promptBuilder.append("商品").append(i + 1).append("：\n");
                promptBuilder.append("名称: ").append(goods.getName()).append("\n");
                promptBuilder.append("简介: ").append(goods.getDescription() != null ? goods.getDescription() : "暂无简介").append("\n");
                promptBuilder.append("价格: ").append(goods.getPrice()).append("元\n");
                promptBuilder.append("分类: ").append(goods.getCategoryName() != null ? goods.getCategoryName() : "未分类").append("\n\n");
            }
        }
        
        promptBuilder.append("=== 当前问题 ===\n");
        promptBuilder.append("用户: ").append(currentMessage).append("\n\n");
        promptBuilder.append("请基于对话历史和商品信息回答用户的问题。如果之前讨论过相关内容，请保持上下文一致性。");
        
        return promptBuilder.toString();
    }

    /**
     * 搜索相关商品（复用SimpleRagService的逻辑）
     */
    private List<Goods> searchRelevantGoods(String userMessage) {
        try {
            List<Goods> allGoods = goodsMapper.selectAll(null);
            System.out.println("数据库中总商品数量: " + allGoods.size());
            
            // 直接返回所有商品，让AI模型自己判断相关性
            System.out.println("直接返回所有商品，让AI模型处理");
            return allGoods;
             
        } catch (Exception e) {
            System.err.println("搜索商品失败: " + e.getMessage());
            return List.of();
        }
    }

    private boolean isGoodsRelevant(Goods goods, String userMessage) {
        String searchText = userMessage.toLowerCase();
        String name = goods.getName() != null ? goods.getName().toLowerCase() : "";
        String description = goods.getDescription() != null ? goods.getDescription().toLowerCase() : "";
        String category = goods.getCategoryName() != null ? goods.getCategoryName().toLowerCase() : "";
        
        // 检查是否是通用推荐问题
        if (isGeneralQuestion(userMessage)) {
            return true; // 通用推荐问题返回所有商品
        }
        
        // 检查是否包含关键词
        return name.contains(searchText) || 
               description.contains(searchText) || 
               category.contains(searchText);
    }
    
    /**
     * 检查是否是通用推荐问题
     */
    private boolean isGeneralQuestion(String userMessage) {
        String searchText = userMessage.toLowerCase();
        return searchText.contains("推荐") || 
               searchText.contains("买什么") || 
               searchText.contains("有什么") ||
               searchText.contains("可以买") ||
               searchText.contains("选择") ||
               searchText.contains("商品") ||
               searchText.contains("热门") ||
               searchText.contains("新商品");
    }
}
