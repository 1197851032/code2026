import {createRouter, createWebHistory} from 'vue-router'
import { checkAuth } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' },
    {
      path: '/manager',
      component: () => import('@/views/Manager.vue'),
      redirect: '/manager/home',
      meta: { requiresAuth: true, role: '管理员' },
      children: [
        { path: 'home', component: () => import('@/views/manager/Home.vue')},
        { path: 'admin', component: () => import('@/views/manager/Admin.vue')},
        { path: 'user', component: () => import('@/views/manager/User.vue')},
        { path: 'person', component: () => import('@/views/manager/Person.vue')},
        { path: 'Password', component: () => import('@/views/manager/Password.vue')},
        { path: 'category', component: () => import('@/views/manager/Category.vue')},
        { path: 'goods', component: () => import('@/views/manager/Goods.vue')},
        { path: 'carousel', component: () => import('@/views/manager/Carousel.vue')},
        { path: 'collect', component: () => import('@/views/manager/Collect.vue')},
        { path: 'recharge', component: () => import('@/views/manager/Recharge.vue')},
        { path: 'orders', component: () => import('@/views/manager/Orders.vue')},
        { path: 'comment', component: () => import('@/views/manager/Comment.vue')},
        { path: 'dataManager', component: () => import('@/views/manager/DataManager.vue')},
      ]
    },
    {
      path: '/front',
      component: () => import('@/views/Front.vue'),
      redirect: '/front/home',
      meta: { requiresAuth: true, role: '普通用户' },
      children: [
        { path: 'home', component: () => import('@/views/front/Home.vue')},
        { path: 'person', component: () => import('@/views/front/Person.vue')},
        { path: 'password', component: () => import('@/views/front/Password.vue')},
        { path: 'goods', component: () => import('@/views/front/Goods.vue')},
        { path: 'goodsDetail', component: () => import('@/views/front/GoodsDetail.vue')},
        { path: 'userCollect', component: () => import('@/views/front/UserCollect.vue')},
        { path: 'userRecharge', component: () => import('@/views/front/UserRecharge.vue')},
        { path: 'cart', component: () => import('@/views/front/Cart.vue')},
        { path: 'userOrders', component: () => import('@/views/front/UserOrders.vue')},
        { path: 'userComment', component: () => import('@/views/front/UserComment.vue')},
      ]
    },
    { path: '/login', component: () => import('@/views/Login.vue') },
    { path: '/register', component: () => import('@/views/Register.vue') }
  ]
})

router.beforeEach((to, from, next) => {
  window.scroll({top: 0, behavior:"smooth"})
  
  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    if (!checkAuth()) {
      ElMessage.error('请先登录')
      next('/login')
      return
    }
    
    // 检查用户角色
    const currentUser = JSON.parse(localStorage.getItem('system-user') || '{}')
    if (to.meta.role && currentUser.role !== to.meta.role) {
      ElMessage.error('权限不足')
      next('/login')
      return
    }
  }
  
  // 如果已登录且访问登录页，重定向到对应首页
  if (to.path === '/login' && checkAuth()) {
    const user = JSON.parse(localStorage.getItem('system-user') || '{}')
    if (user.role === '管理员') {
      next('/manager/home')
    } else {
      next('/front/home')
    }
    return
  }
  
  next()
})

export default router
