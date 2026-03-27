# JWT认证实现说明

## 已完成的功能

### 1. JWT基础组件
- ✅ **JwtUtil.java** - JWT工具类，提供token生成、验证、解析功能
- ✅ **JwtInterceptor.java** - JWT拦截器，验证请求中的token
- ✅ **WebConfig.java** - Web配置，注册拦截器和CORS配置
- ✅ **SecurityConfig.java** - Spring Security配置，禁用默认认证
- ✅ **BaseController.java** - 基础控制器，提供获取当前用户信息的方法

### 2. 认证接口
- ✅ **/user/login** - 用户登录接口，返回JWT token
- ✅ **/user/register** - 用户注册接口，自动生成token

### 3. 已添加JWT认证的控制器
- ✅ **CartController** - 购物车控制器
  - 自动设置当前用户ID
  - 只能操作自己的购物车项
- ✅ **OrdersController** - 订单控制器
  - 自动设置当前用户ID
  - 只能操作自己的订单
- ✅ **CollectController** - 收藏控制器
  - 自动设置当前用户ID
  - 只能操作自己的收藏
- ✅ **CommentController** - 评论控制器
  - 自动设置当前用户ID
  - 只能操作自己的评论
- ✅ **RechargeController** - 充值控制器
  - 自动设置当前用户ID
  - 只能操作自己的充值记录

## 配置说明

### application.yml配置
```yaml
jwt:
  secret: mySecretKey123456789012345678901234567890  # JWT密钥
  expiration: 86400  # 过期时间（秒），24小时
```

### 拦截规则
**不需要token的接口：**
- `/user/login` - 登录
- `/user/register` - 注册  
- `/ai/simple-chat` - 简单聊天
- `/error` - 错误页面
- `/favicon.ico` - 图标

**需要token的接口：**
- 其他所有接口（包括购物车、订单、收藏、评论、充值、RAG聊天等）

## 使用方法

### 1. 用户登录
```bash
POST /user/login
Content-Type: application/json

{
  "username": "用户名",
  "password": "密码"
}
```

**响应：**
```json
{
  "code": "200",
  "msg": "请求成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "用户名",
      "name": "姓名",
      "role": "普通用户"
    }
  }
}
```

### 2. 访问受保护的接口
在请求头中添加JWT token：
```bash
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 3. 自动用户认证
所有需要认证的接口都会：
- 自动从token中解析用户信息
- 自动设置当前用户ID到相关实体
- 验证用户只能操作自己的数据

## 安全特性
- ✅ JWT token签名验证
- ✅ Token过期检查
- ✅ CORS跨域支持
- ✅ 统一错误响应格式
- ✅ 用户数据隔离（用户只能操作自己的数据）
- ✅ 自动用户信息注入

## 待完成的控制器
以下控制器还未添加JWT认证，可以按需添加：
- AdminController（管理员控制器）
- CarouselController（轮播图控制器）
- CategoryController（分类控制器）
- GoodsController（商品控制器）
- FileController（文件控制器）

## 注意事项
1. JWT密钥应该改为更安全的字符串
2. 生产环境应该使用HTTPS
3. 可以根据需要调整token过期时间
4. 管理员接口可能需要不同的权限控制策略
