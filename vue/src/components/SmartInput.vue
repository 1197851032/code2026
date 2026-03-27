<template>
  <div class="smart-input-container">
    <!-- 智能输入框 -->
    <div class="input-wrapper">
      <el-input
        v-model="userInput"
        type="textarea"
        :rows="3"
        placeholder="请输入您的问题，或选择下方的快捷问题..."
        @input="handleInputChange"
        @focus="showSuggestions = true"
        @blur="hideSuggestions"
        clearable
        resize="none"
      />
      
      <!-- 发送按钮 -->
      <el-button
        type="primary"
        :loading="sending"
        @click="sendMessage"
        :disabled="!userInput.trim()"
        class="send-btn"
      >
        发送
      </el-button>
    </div>

    <!-- 智能建议下拉框 -->
    <div v-if="showSuggestions && suggestions.length > 0" class="suggestions-dropdown">
      <div class="suggestions-header">
        <i class="el-icon-lightbulb"></i>
        <span>智能建议</span>
      </div>
      <div 
        v-for="(suggestion, index) in suggestions" 
        :key="index"
        class="suggestion-item"
        @click="selectSuggestion(suggestion)"
        @mousedown.prevent
      >
        {{ suggestion }}
      </div>
    </div>

    <!-- 快捷问题分类 -->
    <div class="quick-templates">
      <div class="templates-header">
        <i class="el-icon-star-on"></i>
        <span>快捷问题</span>
        <el-button 
          type="text" 
          size="small" 
          @click="showAllTemplates = !showAllTemplates"
        >
          {{ showAllTemplates ? '收起' : '展开' }}
        </el-button>
      </div>
      
      <div class="categories-container" :class="{ 'expanded': showAllTemplates }">
        <div 
          v-for="category in displayedCategories" 
          :key="category.category"
          class="category-section"
        >
          <div class="category-title">
            <i class="el-icon-folder"></i>
            {{ category.category }}
          </div>
          <div class="template-tags">
            <el-tag
              v-for="(template, index) in category.templates.slice(0, 3)"
              :key="index"
              type="info"
              effect="plain"
              @click="selectTemplate(template)"
              class="template-tag"
            >
              {{ template }}
            </el-tag>
            <el-tag
              v-if="category.templates.length > 3"
              type="info"
              effect="plain"
              @click="showCategoryDialog(category)"
              class="template-tag more-tag"
            >
              更多...
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 热门问题 -->
    <div class="popular-questions" v-if="!userInput.trim()">
      <div class="popular-header">
        <i class="el-icon-fire"></i>
        <span>热门问题</span>
      </div>
      <div class="popular-tags">
        <el-tag
          v-for="(question, index) in popularQuestions"
          :key="index"
          type="warning"
          effect="light"
          @click="selectTemplate(question)"
          class="popular-tag"
        >
          {{ question }}
        </el-tag>
      </div>
    </div>

    <!-- 分类详情对话框 -->
    <el-dialog
      v-model="categoryDialogVisible"
      :title="selectedCategory?.category + ' - 更多问题'"
      width="600px"
    >
      <div class="category-templates">
        <el-tag
          v-for="(template, index) in selectedCategory?.templates || []"
          :key="index"
          type="info"
          effect="plain"
          @click="selectTemplateFromDialog(template)"
          class="dialog-template-tag"
        >
          {{ template }}
        </el-tag>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'SmartInput',
  props: {
    sessionId: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      userInput: '',
      suggestions: [],
      templates: [],
      popularQuestions: [],
      showSuggestions: false,
      showAllTemplates: false,
      categoryDialogVisible: false,
      selectedCategory: null,
      sending: false,
      inputTimer: null,
      maxDisplayedCategories: 2
    }
  },
  computed: {
    displayedCategories() {
      if (this.showAllTemplates) {
        return this.templates
      }
      return this.templates.slice(0, this.maxDisplayedCategories)
    }
  },
  mounted() {
    this.loadTemplates()
    this.loadPopularQuestions()
  },
  methods: {
    // 加载所有模板
    async loadTemplates() {
      try {
        const response = await axios.get('/smart-input/templates')
        if (response.data.code === 200) {
          this.templates = response.data.data
        }
      } catch (error) {
        console.error('加载模板失败:', error)
      }
    },

    // 加载热门问题
    async loadPopularQuestions() {
      try {
        const response = await axios.get('/smart-input/popular')
        if (response.data.code === 200) {
          this.popularQuestions = response.data.data
        }
      } catch (error) {
        console.error('加载热门问题失败:', error)
      }
    },

    // 处理输入变化
    handleInputChange() {
      // 防抖处理
      if (this.inputTimer) {
        clearTimeout(this.inputTimer)
      }
      
      this.inputTimer = setTimeout(() => {
        this.fetchSuggestions()
      }, 300)
    },

    // 获取智能建议
    async fetchSuggestions() {
      if (!this.userInput.trim()) {
        this.suggestions = []
        return
      }

      try {
        const response = await axios.get('/smart-input/suggestions', {
          params: { input: this.userInput }
        })
        if (response.data.code === 200) {
          this.suggestions = response.data.data
        }
      } catch (error) {
        console.error('获取建议失败:', error)
      }
    },

    // 选择建议
    selectSuggestion(suggestion) {
      this.userInput = suggestion
      this.showSuggestions = false
      this.$emit('input', suggestion)
    },

    // 选择模板
    selectTemplate(template) {
      this.userInput = template
      this.$emit('input', template)
      // 自动发送
      this.sendMessage()
    },

    // 从对话框选择模板
    selectTemplateFromDialog(template) {
      this.userInput = template
      this.categoryDialogVisible = false
      this.$emit('input', template)
      this.sendMessage()
    },

    // 显示分类详情
    showCategoryDialog(category) {
      this.selectedCategory = category
      this.categoryDialogVisible = true
    },

    // 发送消息
    async sendMessage() {
      if (!this.userInput.trim() || this.sending) {
        return
      }

      this.sending = true
      const message = this.userInput.trim()
      
      // 清空输入框
      this.userInput = ''
      this.showSuggestions = false
      
      // 触发发送事件
      this.$emit('send', message)
      
      try {
        // 调用聊天API
        const response = await axios.post('/conversation/chat', {
          sessionId: this.sessionId,
          message: message
        })
        
        if (response.data.code === 200) {
          this.$emit('response', response.data.data)
        } else {
          this.$message.error(response.data.msg || '发送失败')
        }
      } catch (error) {
        console.error('发送消息失败:', error)
        this.$message.error('发送失败，请重试')
      } finally {
        this.sending = false
      }
    },

    // 隐藏建议（延迟，以便点击建议项）
    hideSuggestions() {
      setTimeout(() => {
        this.showSuggestions = false
      }, 200)
    }
  }
}
</script>

<style scoped>
.smart-input-container {
  position: relative;
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.input-wrapper {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.input-wrapper .el-textarea {
  flex: 1;
}

.send-btn {
  margin-top: 5px;
  min-width: 80px;
}

/* 智能建议下拉框 */
.suggestions-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: 200px;
  overflow-y: auto;
}

.suggestions-header {
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 5px;
}

.suggestion-item {
  padding: 10px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.suggestion-item:hover {
  background-color: #f5f7fa;
}

.suggestion-item:last-child {
  border-bottom: none;
}

/* 快捷问题分类 */
.quick-templates {
  margin-top: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #fafafa;
}

.templates-header {
  padding: 10px 15px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.categories-container {
  padding: 15px;
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.3s ease;
}

.categories-container.expanded {
  max-height: 500px;
}

.category-section {
  margin-bottom: 15px;
}

.category-section:last-child {
  margin-bottom: 0;
}

.category-title {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.template-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.template-tag {
  cursor: pointer;
  transition: all 0.2s;
  font-size: 12px;
}

.template-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.more-tag {
  border-style: dashed;
}

/* 热门问题 */
.popular-questions {
  margin-top: 15px;
  padding: 15px;
  background: linear-gradient(135deg, #fff9e6 0%, #fff5e6 100%);
  border-radius: 4px;
  border: 1px solid #f7ba2a;
}

.popular-header {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  font-weight: 500;
  color: #e6a23c;
  margin-bottom: 10px;
}

.popular-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.popular-tag {
  cursor: pointer;
  transition: all 0.2s;
  font-size: 12px;
}

.popular-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(230, 162, 60, 0.2);
}

/* 对话框样式 */
.category-templates {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.dialog-template-tag {
  cursor: pointer;
  transition: all 0.2s;
  margin: 5px;
}

.dialog-template-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .input-wrapper {
    flex-direction: column;
  }
  
  .send-btn {
    align-self: flex-end;
    margin-top: 10px;
  }
  
  .template-tags,
  .popular-tags {
    gap: 5px;
  }
  
  .template-tag,
  .popular-tag {
    font-size: 11px;
  }
}
</style>
