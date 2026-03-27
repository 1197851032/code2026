package com.example.mapper;

import com.example.entity.ChatMessage;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 对话记录Mapper
 */
@Mapper
public interface ChatMessageMapper {
    
    @Insert("INSERT INTO chat_message (user_id, session_id, message, response, message_type, create_time) " +
            "VALUES (#{userId}, #{sessionId}, #{message}, #{response}, #{messageType}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatMessage chatMessage);
    
    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} ORDER BY create_time ASC")
    List<ChatMessage> findBySessionId(String sessionId);
    
    @Select("SELECT * FROM chat_message WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<ChatMessage> findByUserId(Integer userId, int limit);
    
    @Select("SELECT DISTINCT session_id FROM chat_message WHERE user_id = #{userId} ORDER BY MAX(create_time) DESC")
    List<String> findSessionIdsByUserId(Integer userId);
    
    @Delete("DELETE FROM chat_message WHERE session_id = #{sessionId}")
    int deleteBySessionId(String sessionId);
    
    @Delete("DELETE FROM chat_message WHERE user_id = #{userId}")
    int deleteByUserId(Integer userId);
    
    @Select("SELECT COUNT(*) FROM chat_message WHERE session_id = #{sessionId}")
    int countBySessionId(String sessionId);
}
