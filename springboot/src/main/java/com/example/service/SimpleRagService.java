package com.example.service;

import com.example.entity.Goods;
import com.example.mapper.GoodsMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SimpleRagService {

    @Autowired
    private GoodsMapper goodsMapper;
    
    private final OpenAiChatModel model;

    public SimpleRagService(@Value("${openai.api.key:}") String apiKey,
                           @Value("${openai.api.base-url:https://api.openai.com/v1}") String baseUrl,
                           @Value("${openai.model.name:gpt-3.5-turbo}") String modelName) {
        
        // 使用测试API
        this.model = OpenAiChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .build();
    }

    public String chat(String userMessage) {
        try {
            // 1. 查询相关商品数据（简单的关键词匹配）
            List<Goods> relevantGoods = searchRelevantGoods(userMessage);
            
            // 添加调试信息
            System.out.println("用户消息: " + userMessage);
            System.out.println("找到相关商品数量: " + relevantGoods.size());
            for (int i = 0; i < relevantGoods.size(); i++) {
                Goods goods = relevantGoods.get(i);
                System.out.println("商品" + (i+1) + ": " + goods.getName());
            }
            
            // 2. 构建增强的提示词
            String enhancedPrompt = buildEnhancedPrompt(userMessage, relevantGoods);
            
            // 3. 调用AI模型
            UserMessage message = UserMessage.from(enhancedPrompt);
            Response<AiMessage> response = model.generate(message);
            
            return response.content().text();
            
        } catch (Exception e) {
            System.err.println("RAG处理失败: " + e.getMessage());
            e.printStackTrace();
            // 如果RAG处理失败，回退到简单聊天
            try {
                UserMessage message = UserMessage.from(userMessage);
                Response<AiMessage> response = model.generate(message);
                return response.content().text();
            } catch (Exception fallbackError) {
                return "抱歉，AI服务暂时不可用: " + fallbackError.getMessage();
            }
        }
    }

    private List<Goods> searchRelevantGoods(String userMessage) {
        try {
            // 查询所有商品
            List<Goods> allGoods = goodsMapper.selectAll(null);
            System.out.println("数据库中总商品数量: " + allGoods.size());
            
            // 直接返回所有商品，让AI模型自己判断相关性
            System.out.println("SimpleRAG - 直接返回所有商品，让AI模型处理");
            return allGoods;
            
        } catch (Exception e) {
            System.err.println("搜索商品失败: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    private boolean isGeneralQuestion(String userMessage) {
        String searchText = userMessage.toLowerCase();
        return searchText.contains("买什么") || 
               searchText.contains("推荐") || 
               searchText.contains("有什么") ||
               searchText.contains("可以买") ||
               searchText.contains("选择") ||
               searchText.contains("商品") ||
               searchText.contains("热门") ||
               searchText.contains("新商品");
    }

    private boolean isGoodsRelevant(Goods goods, String userMessage) {
        String searchText = userMessage.toLowerCase();
        String name = goods.getName() != null ? goods.getName().toLowerCase() : "";
        String description = goods.getDescription() != null ? goods.getDescription().toLowerCase() : "";
        String content = goods.getContent() != null ? goods.getContent().toLowerCase() : "";
        String category = goods.getCategoryName() != null ? goods.getCategoryName().toLowerCase() : "";
        
        // 检查是否是通用推荐问题
        if (isGeneralQuestion(userMessage)) {
            return true; // 通用推荐问题返回所有商品
        }
        
        // 检查是否包含关键词
        return name.contains(searchText) || 
               description.contains(searchText) || 
               content.contains(searchText) || 
               category.contains(searchText) ||
               containsPriceKeywords(searchText, goods.getPrice()) ||
               containsCategoryKeywords(searchText, category);
    }

    private boolean containsPriceKeywords(String searchText, java.math.BigDecimal price) {
        if (price == null) return false;
        
        // 检查价格相关的关键词
        if (searchText.contains("便宜") && price.compareTo(java.math.BigDecimal.valueOf(100)) < 0) return true;
        if (searchText.contains("中等") && price.compareTo(java.math.BigDecimal.valueOf(100)) >= 0 && price.compareTo(java.math.BigDecimal.valueOf(500)) < 0) return true;
        if (searchText.contains("贵") && price.compareTo(java.math.BigDecimal.valueOf(500)) >= 0) return true;
        
        return false;
    }

    private boolean containsCategoryKeywords(String searchText, String category) {
        // 检查分类相关的关键词
        if (searchText.contains("手机") && category.contains("手机")) return true;
        if (searchText.contains("电脑") && category.contains("电脑")) return true;
        if (searchText.contains("服装") && category.contains("服装")) return true;
        if (searchText.contains("食品") && category.contains("食品")) return true;
        
        return false;
    }

    private String buildEnhancedPrompt(String userMessage, List<Goods> relevantGoods) {
        StringBuilder promptBuilder = new StringBuilder();
        
        promptBuilder.append("你是一个专业的商品推荐助手。请根据以下商品信息回答用户的问题。\n\n");
        promptBuilder.append("相关商品信息：\n");
        
        if (relevantGoods.isEmpty()) {
            promptBuilder.append("没有找到直接相关的商品信息。\n");
        } else {
            for (int i = 0; i < relevantGoods.size(); i++) {
                Goods goods = relevantGoods.get(i);
                promptBuilder.append("商品").append(i + 1).append("：\n");
                promptBuilder.append("名称: ").append(goods.getName()).append("\n");
                promptBuilder.append("简介: ").append(goods.getDescription() != null ? goods.getDescription() : "暂无简介").append("\n");
                promptBuilder.append("详情: ").append(goods.getContent() != null ? goods.getContent() : "暂无详情").append("\n");
                promptBuilder.append("价格: ").append(goods.getPrice()).append("元\n");
                promptBuilder.append("分类: ").append(goods.getCategoryName() != null ? goods.getCategoryName() : "未分类").append("\n");
                promptBuilder.append("库存: ").append(goods.getStore()).append("\n\n");
            }
        }
        
        promptBuilder.append("用户问题：").append(userMessage).append("\n\n");
        promptBuilder.append("请基于上述商品信息回答用户的问题。如果信息不足，请诚实地说明并提供一般性建议。");
        
        return promptBuilder.toString();
    }
}
