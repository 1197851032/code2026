import request from './request'
import { ElMessage } from 'element-plus'
import router from '../router'

// 检查用户是否已登录
export function checkAuth() {
  const token = localStorage.getItem('system-token');
  const user = localStorage.getItem('system-user');
  
  if (!token || !user) {
    return false;
  }
  
  return true;
}

// 获取当前用户信息
export function getCurrentUser() {
  const userStr = localStorage.getItem('system-user');
  if (userStr) {
    try {
      return JSON.parse(userStr);
    } catch (e) {
      console.error('解析用户信息失败:', e);
      return null;
    }
  }
  return null;
}

// 验证token是否有效
export function validateToken() {
  return request.get('/auth/validate').then(res => {
    return res.code === '200';
  }).catch(error => {
    console.error('Token验证失败:', error);
    return false;
  });
}

// 登出
export function logout() {
  localStorage.removeItem('system-token');
  localStorage.removeItem('system-user');
  ElMessage.success('已退出登录');
  router.push('/login');
}

// 检查登录状态并跳转
export function checkAndRedirect() {
  if (!checkAuth()) {
    ElMessage.error('请先登录');
    router.push('/login');
    return false;
  }
  return true;
}

// 获取token
export function getToken() {
  return localStorage.getItem('system-token');
}
