-- 对话记录表
CREATE TABLE IF NOT EXISTS chat_message (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '用户ID',
    session_id VARCHAR(100) NOT NULL COMMENT '会话ID',
    message TEXT COMMENT '用户消息内容',
    response TEXT COMMENT 'AI回复内容',
    message_type VARCHAR(10) NOT NULL COMMENT '消息类型：user/ai',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_create_time (create_time),
    INDEX idx_user_session (user_id, session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话记录表';

-- 示例数据
INSERT INTO chat_message (user_id, session_id, message, response, message_type) VALUES
(33, 'session-001', '你好，我想买手机', '您好！我可以帮您推荐手机。请问您的预算大概是多少呢？', 'user'),
(33, 'session-001', '预算在3000元左右', '3000元预算可以选择很多不错的手机。我推荐您考虑小米、华为等品牌，性价比很高。', 'ai'),
(44, 'session-002', '有什么便宜的电脑推荐吗', '我为您推荐几款性价比高的电脑。您的用途主要是办公还是游戏呢？', 'user'),
(44, 'session-002', '主要是办公使用', '对于办公使用，我推荐联想ThinkPad系列或者华为MateBook，性能稳定，价格适中。', 'ai');
