package com.example.controller;

import com.example.common.Result;
import com.example.controller.BaseController;
import com.example.entity.ChatMessage;
import com.example.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 带记忆功能的对话控制器
 */
@RestController
@RequestMapping("/conversation")
@CrossOrigin(origins = "*")
public class ConversationController extends BaseController {

    @Autowired
    private ConversationService conversationService;

    /**
     * 开始新对话
     */
    @PostMapping("/start")
    public Result startNewConversation(HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            String sessionId = conversationService.startNewConversation(userId);
            
            Map<String, Object> data = Map.of(
                "sessionId", sessionId,
                "message", "新对话已开始"
            );
            
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("开始对话失败: " + e.getMessage());
        }
    }

    /**
     * 带记忆的聊天
     */
    @PostMapping("/chat")
    public Result chatWithMemory(@RequestBody Map<String, String> params, HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            String sessionId = params.get("sessionId");
            String message = params.get("message");
            
            // 输入验证
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return Result.error("会话ID不能为空");
            }
            if (message == null || message.trim().isEmpty()) {
                return Result.error("消息内容不能为空");
            }
            if (message.length() > 4000) {
                return Result.error("消息长度不能超过4000字符");
            }
            
            // 带记忆的聊天
            String response = conversationService.chatWithMemory(userId, sessionId, message);
            
            Map<String, Object> data = Map.of(
                "sessionId", sessionId,
                "userMessage", message,
                "aiResponse", response
            );
            
            return Result.success(data);
            
        } catch (Exception e) {
            return Result.error("对话失败: " + e.getMessage());
        }
    }

    /**
     * 获取对话历史
     */
    @GetMapping("/history/{sessionId}")
    public Result getConversationHistory(@PathVariable String sessionId, HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            List<ChatMessage> history = conversationService.getConversationHistory(sessionId);
            
            return Result.success(history);
        } catch (Exception e) {
            return Result.error("获取对话历史失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的所有会话
     */
    @GetMapping("/sessions")
    public Result getUserSessions(HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            List<String> sessions = conversationService.getUserSessions(userId);
            
            return Result.success(sessions);
        } catch (Exception e) {
            return Result.error("获取会话列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户最近的对话记录
     */
    @GetMapping("/recent")
    public Result getUserRecentMessages(@RequestParam(defaultValue = "10") int limit, HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            List<ChatMessage> messages = conversationService.getUserRecentMessages(userId, limit);
            
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error("获取最近对话失败: " + e.getMessage());
        }
    }

    /**
     * 清除会话记录
     */
    @DeleteMapping("/session/{sessionId}")
    public Result clearConversation(@PathVariable String sessionId, HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            boolean success = conversationService.clearConversation(sessionId);
            
            if (success) {
                return Result.success("会话已清除");
            } else {
                return Result.error("清除会话失败");
            }
        } catch (Exception e) {
            return Result.error("清除会话失败: " + e.getMessage());
        }
    }

    /**
     * 清除用户所有对话记录
     */
    @DeleteMapping("/all")
    public Result clearAllUserConversations(HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            boolean success = conversationService.clearAllUserConversations(userId);
            
            if (success) {
                return Result.success("所有对话记录已清除");
            } else {
                return Result.error("清除对话记录失败");
            }
        } catch (Exception e) {
            return Result.error("清除对话记录失败: " + e.getMessage());
        }
    }
}
