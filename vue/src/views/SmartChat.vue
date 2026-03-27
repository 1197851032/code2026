<template>
  <div class="smart-chat-container">
    <!-- 页面头部 -->
    <div class="chat-header">
      <div class="header-left">
        <i class="el-icon-chat-dot-round"></i>
        <h2>智能客服</h2>
      </div>
      <div class="header-right">
        <el-button 
          type="text" 
          @click="showHistory = !showHistory"
          :class="{ 'active': showHistory }"
        >
          <i class="el-icon-time"></i>
          对话历史
        </el-button>
        <el-button 
          type="text" 
          @click="startNewConversation"
        >
          <i class="el-icon-plus"></i>
          新对话
        </el-button>
      </div>
    </div>

    <div class="chat-content">
      <!-- 对话历史侧边栏 -->
      <div v-if="showHistory" class="history-sidebar">
        <div class="history-header">
          <h3>对话历史</h3>
          <el-button 
            type="text" 
            size="small" 
            @click="clearAllHistory"
            class="clear-btn"
          >
            清空历史
          </el-button>
        </div>
        <div class="history-list">
          <div 
            v-for="session in sessions" 
            :key="session.sessionId"
            class="history-item"
            :class="{ 'active': currentSessionId === session.sessionId }"
            @click="loadSession(session.sessionId)"
          >
            <div class="session-info">
              <div class="session-title">对话 {{ session.sessionId.slice(-8) }}</div>
              <div class="session-time">{{ formatTime(session.lastMessageTime) }}</div>
            </div>
            <div class="session-preview">{{ session.lastMessage }}</div>
          </div>
        </div>
      </div>

      <!-- 聊天主区域 -->
      <div class="chat-main" :class="{ 'with-history': showHistory }">
        <!-- 消息列表 -->
        <div class="messages-container" ref="messagesContainer">
          <div 
            v-for="(message, index) in messages" 
            :key="index"
            class="message-item"
            :class="{ 'user': message.type === 'user', 'ai': message.type === 'ai' }"
          >
            <div class="message-avatar">
              <i :class="message.type === 'user' ? 'el-icon-user' : 'el-icon-service'"></i>
            </div>
            <div class="message-content">
              <div class="message-text">{{ message.content }}</div>
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
            </div>
          </div>
          
          <!-- 加载中提示 -->
          <div v-if="loading" class="message-item ai">
            <div class="message-avatar">
              <i class="el-icon-service"></i>
            </div>
            <div class="message-content">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 智能输入组件 -->
        <div class="input-section">
          <SmartInput 
            :session-id="currentSessionId"
            @send="handleSendMessage"
            @response="handleReceiveResponse"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import SmartInput from '@/components/SmartInput.vue'

export default {
  name: 'SmartChat',
  components: {
    SmartInput
  },
  data() {
    return {
      currentSessionId: '',
      messages: [],
      sessions: [],
      showHistory: false,
      loading: false
    }
  },
  mounted() {
    this.startNewConversation()
    this.loadSessions()
  },
  methods: {
    // 开始新对话
    async startNewConversation() {
      try {
        const response = await axios.post('/conversation/start')
        if (response.data.code === 200) {
          this.currentSessionId = response.data.data.sessionId
          this.messages = []
          this.loadSessions() // 刷新会话列表
        }
      } catch (error) {
        console.error('开始新对话失败:', error)
        this.$message.error('开始新对话失败')
      }
    },

    // 加载会话列表
    async loadSessions() {
      try {
        const response = await axios.get('/conversation/sessions')
        if (response.data.code === 200) {
          // 这里简化处理，实际应该从后端获取会话详情
          this.sessions = response.data.data.map(sessionId => ({
            sessionId,
            lastMessage: '点击加载对话...',
            lastMessageTime: new Date()
          }))
        }
      } catch (error) {
        console.error('加载会话列表失败:', error)
      }
    },

    // 加载特定会话
    async loadSession(sessionId) {
      try {
        const response = await axios.get(`/conversation/history/${sessionId}`)
        if (response.data.code === 200) {
          this.currentSessionId = sessionId
          // 转换消息格式
          this.messages = response.data.data.map(msg => ({
            type: msg.messageType,
            content: msg.messageType === 'user' ? msg.message : msg.response,
            timestamp: msg.createTime
          }))
          this.scrollToBottom()
        }
      } catch (error) {
        console.error('加载会话失败:', error)
        this.$message.error('加载会话失败')
      }
    },

    // 处理发送消息
    handleSendMessage(message) {
      // 添加用户消息到界面
      this.messages.push({
        type: 'user',
        content: message,
        timestamp: new Date()
      })
      this.loading = true
      this.scrollToBottom()
    },

    // 处理接收回复
    handleReceiveResponse(responseData) {
      this.loading = false
      // 添加AI回复到界面
      this.messages.push({
        type: 'ai',
        content: responseData.aiResponse,
        timestamp: new Date()
      })
      this.scrollToBottom()
      
      // 更新会话列表中的最后消息
      this.updateSessionLastMessage(responseData.userMessage)
    },

    // 更新会话最后消息
    updateSessionLastMessage(message) {
      const sessionIndex = this.sessions.findIndex(s => s.sessionId === this.currentSessionId)
      if (sessionIndex !== -1) {
        this.sessions[sessionIndex].lastMessage = message
        this.sessions[sessionIndex].lastMessageTime = new Date()
      }
    },

    // 清空所有历史
    async clearAllHistory() {
      try {
        const response = await axios.delete('/conversation/all')
        if (response.data.code === 200) {
          this.sessions = []
          this.messages = []
          this.startNewConversation()
          this.$message.success('历史记录已清空')
        }
      } catch (error) {
        console.error('清空历史失败:', error)
        this.$message.error('清空历史失败')
      }
    },

    // 滚动到底部
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    },

    // 格式化时间
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) { // 1分钟内
        return '刚刚'
      } else if (diff < 3600000) { // 1小时内
        return Math.floor(diff / 60000) + '分钟前'
      } else if (diff < 86400000) { // 1天内
        return Math.floor(diff / 3600000) + '小时前'
      } else {
        return date.toLocaleDateString()
      }
    }
  }
}
</script>

<style scoped>
.smart-chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

/* 页面头部 */
.chat-header {
  background: white;
  padding: 15px 20px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-left h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.header-right {
  display: flex;
  gap: 10px;
}

.header-right .el-button.active {
  color: #409eff;
  background-color: #ecf5ff;
}

/* 聊天内容区域 */
.chat-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 对话历史侧边栏 */
.history-sidebar {
  width: 300px;
  background: white;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.history-header {
  padding: 15px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.clear-btn {
  color: #f56c6c;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.history-item {
  padding: 12px;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.history-item:hover {
  background: #f5f7fa;
  border-color: #e4e7ed;
}

.history-item.active {
  background: #ecf5ff;
  border-color: #409eff;
}

.session-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.session-title {
  font-weight: 500;
  color: #303133;
}

.session-time {
  font-size: 12px;
  color: #909399;
}

.session-preview {
  font-size: 13px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 聊天主区域 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.chat-main.with-history {
  margin-left: 1px;
}

/* 消息容器 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #fafafa;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
}

.message-item.user {
  justify-content: flex-end;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  margin: 0 12px;
}

.message-item.user .message-avatar {
  background: #67c23a;
  order: 2;
}

.message-content {
  max-width: 70%;
  background: white;
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.message-item.user .message-content {
  background: #409eff;
  color: white;
  order: 1;
}

.message-text {
  line-height: 1.5;
  word-wrap: break-word;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  text-align: right;
}

.message-item.user .message-time {
  color: rgba(255, 255, 255, 0.8);
}

/* 输入中指示器 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 10px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409eff;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 输入区域 */
.input-section {
  background: white;
  border-top: 1px solid #e4e7ed;
  padding: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-header {
    padding: 10px 15px;
  }
  
  .header-left h2 {
    font-size: 16px;
  }
  
  .history-sidebar {
    width: 250px;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .messages-container {
    padding: 15px;
  }
  
  .input-section {
    padding: 15px;
  }
}

@media (max-width: 480px) {
  .history-sidebar {
    position: absolute;
    left: -250px;
    top: 0;
    height: 100%;
    z-index: 1000;
    transition: left 0.3s ease;
  }
  
  .history-sidebar.show {
    left: 0;
  }
  
  .chat-main {
    margin-left: 0 !important;
  }
}
</style>
