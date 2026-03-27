<template>
  <div>
    <div style="height: 60px; background-color: #2e3143; display: flex; align-items: center;">
      <div style="width: 20%">
        <div style="padding-left: 20px; display: flex; align-items: center">
          <img src="@/assets/imgs/logo.png" alt="" style="width: 40px">
          <div style="font-weight: bold; font-size: 24px; margin-left: 5px; color: #fff">校园小卖部</div>
        </div>
      </div>
      <div style="width: 60%; height: 60px; display: flex; align-items: center">
        <div style="flex: 1">
          <el-menu  router :default-active="router.currentRoute.value.path" style="background-color: #2e3143;" ellipsis mode="horizontal">
            <el-menu-item index="/front/home">首页</el-menu-item>
            <el-menu-item index="/front/goods">精选商品</el-menu-item>
            <el-menu-item index="/front/cart">购物车</el-menu-item>
            <el-menu-item index="/front/userOrders">商品订单</el-menu-item>
          </el-menu>
        </div>
        <div style="width: fit-content" v-if="router.currentRoute.value.path !== '/front/goods'">
          <el-input @keyup.enter="search" prefix-icon="Search" style="width: 300px; height: 40px" v-model="data.goodsName" placeholder="请输入商品名称查询"></el-input>
        </div>
      </div>
      <div style="width: 20%; text-align: right; padding-right: 10px;">
        <el-dropdown>
          <div style="display: flex; align-items: center;">
            <img style="width: 40px; height: 40px; border-radius: 50%" :src="data.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt="">
            <span style="color: #fff; margin-left: 5px">{{ data.user.name || '代码小白' }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click.native="router.push('/front/userRecharge')">我的充值</el-dropdown-item>
              <el-dropdown-item @click.native="router.push('/front/userCollect')">我的收藏</el-dropdown-item>
              <el-dropdown-item @click.native="router.push('/front/userComment')">我的评价</el-dropdown-item>
              <el-dropdown-item @click.native="router.push('/front/person')">个人信息</el-dropdown-item>
              <el-dropdown-item @click.native="router.push('/front/password')">修改密码</el-dropdown-item>
              <el-dropdown-item @click.native="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

      </div>
    </div>

    <div style="background-color: #f0f2ff">
      <router-view @updateUser="updateUser" />
    </div>

    <!-- 智能客服悬浮窗 -->
    <div class="chatbot-container">
      <!-- 客服图标 - 使用更美观的设计 -->
      <div v-if="!showChat" class="chatbot-icon" @click="toggleChat">
        <div class="icon-wrapper">
          <svg class="chat-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/>
          </svg>
          <div class="notification-dot"></div>
        </div>
        <div class="icon-text">客服</div>
      </div>
      
      <!-- 聊天窗口 -->
      <div v-else class="chatbot-window">
        <div class="chatbot-header">
          <div class="header-left">
            <div class="avatar">
              <svg class="avatar-icon" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"/>
              </svg>
            </div>
            <div class="header-info">
              <span class="title">智能客服</span>
              <span class="status">在线</span>
            </div>
          </div>
          <div class="header-actions">
            <el-icon @click="minimizeChat" class="header-icon"><Minus /></el-icon>
            <el-icon @click="toggleChat" class="header-icon"><Close /></el-icon>
          </div>
        </div>
        
        <div class="chatbot-messages" ref="messagesContainer">
          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="['message', message.type]"
          >
            <div class="message-content">{{ message.content }}</div>
            <div class="message-time">{{ message.time }}</div>
          </div>
        </div>
        
        <div class="chatbot-input">
          <el-input
            v-model="inputMessage"
            placeholder="请输入消息..."
            @keyup.enter="sendMessage"
            size="small"
          ></el-input>
          <el-button type="primary" @click="sendMessage" size="small" :loading="isLoading">
            发送
          </el-button>
        </div>
      </div>
    </div>

    <Footer />

  </div>
</template>

<script setup>
import { reactive, ref, nextTick, onMounted, onUnmounted } from "vue";
import router from "@/router";
import {ElMessage} from "element-plus";
import { Close, Minus } from '@element-plus/icons-vue'
import Footer from "@/components/Footer.vue";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  goodsName: null
})

// 智能客服相关数据和方法
const showChat = ref(false)
const inputMessage = ref('')
const messages = ref([])
const messagesContainer = ref(null)
const isLoading = ref(false)

const toggleChat = () => {
  showChat.value = !showChat.value
  if (showChat.value && messages.value.length === 0) {
    // 添加欢迎消息
    messages.value.push({
      type: 'received',
      content: '您好！我是校园小卖部的智能客服，请问有什么可以帮助您？',
      time: getCurrentTime()
    })
  }
}

const minimizeChat = () => {
  showChat.value = false
}

// 获取当前时间
const getCurrentTime = () => {
  const now = new Date()
  return now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

import request from "@/utils/request";

const sendMessage = () => {
  if (!inputMessage.value.trim() || isLoading.value) return

  // 添加用户消息
  messages.value.push({
    type: 'sent',
    content: inputMessage.value,
    time: getCurrentTime()
  })

  const userMessage = inputMessage.value;
  // 清空输入框
  inputMessage.value = ''
  isLoading.value = true

  // 调用后端接口
  request.post('/ai/chat', { message: userMessage }).then(res => {
    if (res.code === '200') {
      messages.value.push({
        type: 'received',
        content: res.data,
        time: getCurrentTime()
      })
    } else {
      messages.value.push({
        type: 'received',
        content: '抱歉，我现在有点忙，请稍后再试。',
        time: getCurrentTime()
      })
    }
    // 滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
  }).catch(error => {
    console.error(error)
    messages.value.push({
      type: 'received',
      content: '服务器繁忙，请稍后再试。',
      time: getCurrentTime()
    })
    nextTick(() => {
      scrollToBottom()
    })
  }).finally(() => {
    isLoading.value = false
  })

  // 滚动到底部
  nextTick(() => {
    scrollToBottom()
  })
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const search = () => {
  if (data.goodsName) {
    router.push('/front/goods?name=' + data.goodsName)
    data.goodsName = null
  }
}

if (!data.user?.id) {
  ElMessage.error('请登录！')
  router.push('/login')
}

const logout = () => {
  localStorage.removeItem('system-user')
  router.push('/login')
  ElMessage.success('退出成功')
}

// 更新Front里面的user对象为最新值
const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
}
</script>

<style>
.el-tooltip__trigger {
  cursor: pointer;
  outline: none !important;
}
.el-menu--horizontal .el-menu-item{
  color: white;
}
.el-menu--horizontal {
  border: none !important;
}
.el-menu--horizontal .el-menu-item {
  border: none;
  height: 60px;
}
.el-menu--horizontal .el-menu-item.is-active {
  border: none;
  color: white !important;
  background-color: #0c9c7a !important;
}
.el-menu--horizontal .el-menu-item:not(.is-active):hover {
  color: white;
  background-color: #8db6ab !important;
}
/* 智能客服样式 */
.chatbot-container {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9999;
}

.chatbot-icon {
  width: 70px;
  height: 70px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.chatbot-icon:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 35px rgba(102, 126, 234, 0.6);
}

.chatbot-icon:active {
  transform: scale(0.95);
}

.icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-icon {
  width: 28px;
  height: 28px;
  fill: white;
}

.notification-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 12px;
  height: 12px;
  background: #ff4757;
  border-radius: 50%;
  border: 2px solid white;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 71, 87, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(255, 71, 87, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(255, 71, 87, 0);
  }
}

.icon-text {
  font-size: 12px;
  margin-top: 2px;
  font-weight: 500;
}

.chatbot-window {
  width: 380px;
  height: 520px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideUp 0.3s ease;
  border: 1px solid #e1e8ed;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.chatbot-header {
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-icon {
  width: 24px;
  height: 24px;
  fill: white;
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.title {
  font-weight: 600;
  font-size: 16px;
}

.status {
  font-size: 12px;
  opacity: 0.9;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status::before {
  content: '';
  width: 6px;
  height: 6px;
  background: #4ade80;
  border-radius: 50%;
  animation: blink 2s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.header-actions {
  display: flex;
  gap: 12px;
}

.header-icon {
  font-size: 18px;
  cursor: pointer;
  opacity: 0.8;
  transition: opacity 0.2s;
  color: white;
}

.header-icon:hover {
  opacity: 1;
}

.chatbot-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #f8fafc;
}

.chatbot-messages::-webkit-scrollbar {
  width: 4px;
}

.chatbot-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chatbot-messages::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 2px;
}

.message {
  max-width: 75%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message.sent {
  align-self: flex-end;
}

.message.received {
  align-self: flex-start;
}

.message-content {
  padding: 12px 16px;
  border-radius: 18px;
  line-height: 1.4;
  word-wrap: break-word;
}

.message.sent .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message.received .message-content {
  background: white;
  color: #1f2937;
  border: 1px solid #e5e7eb;
  border-bottom-left-radius: 4px;
}

.message-time {
  font-size: 11px;
  color: #6b7280;
  padding: 0 8px;
}

.message.sent .message-time {
  text-align: right;
}

.chatbot-input {
  padding: 16px 20px;
  display: flex;
  gap: 12px;
  background: white;
  border-top: 1px solid #e5e7eb;
}

.chatbot-input .el-input {
  flex: 1;
}

.chatbot-input .el-input__wrapper {
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chatbot-input .el-button {
  border-radius: 20px;
  padding: 0 20px;
  font-weight: 500;
}
</style>