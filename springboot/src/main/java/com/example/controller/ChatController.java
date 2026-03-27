package com.example.controller;

import com.example.common.Result;
import com.example.service.SimpleRagService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class ChatController {

    private final OpenAiChatModel model;
    
    @Autowired
    private SimpleRagService simpleRagService;

    public ChatController(@Value("${openai.api.key:}") String apiKey,
                         @Value("${openai.api.base-url:https://api.openai.com/v1}") String baseUrl,
                         @Value("${openai.model.name:gpt-3.5-turbo}") String modelName) {
        
        // 使用测试API
        this.model = OpenAiChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .build();
    }

    @PostMapping("/chat")
    public Result chat(@RequestBody Map<String, String> params) {
        try {
            String message = params.get("message");
            
            // 输入验证
            if (message == null || message.trim().isEmpty()) {
                return Result.error("消息内容不能为空");
            }
            
            // 限制消息长度
            if (message.length() > 4000) {
                return Result.error("消息长度不能超过4000字符");
            }
            
            // 使用RAG服务进行聊天
            String answer = simpleRagService.chat(message);
            return Result.success(answer);
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("AI服务暂时不可用: " + e.getMessage());
        }
    }
    
    @PostMapping("/simple-chat")
    public Result simpleChat(@RequestBody Map<String, String> params) {
        try {
            String message = params.get("message");
            
            // 输入验证
            if (message == null || message.trim().isEmpty()) {
                return Result.error("消息内容不能为空");
            }
            
            // 限制消息长度
            if (message.length() > 4000) {
                return Result.error("消息长度不能超过4000字符");
            }
            
            // 使用简单聊天模式（不使用RAG）
            UserMessage userMessage = UserMessage.from(message);
            Response<AiMessage> response = model.generate(userMessage);
            String answer = response.content().text();
            return Result.success(answer);
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("AI服务暂时不可用: " + e.getMessage());
        }
    }
}
