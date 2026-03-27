package com.example.entity;

import java.time.LocalDateTime;

/**
 * 对话记录实体
 */
public class ChatMessage {
    private Integer id;
    private Integer userId;
    private String sessionId;
    private String message;
    private String response;
    private String messageType; // "user" 或 "ai"
    private LocalDateTime createTime;
    
    public ChatMessage() {}
    
    public ChatMessage(Integer userId, String sessionId, String message, String response, String messageType) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.message = message;
        this.response = response;
        this.messageType = messageType;
        this.createTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
