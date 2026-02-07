import {createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' },
    {
      path: '/manager',
      component: () => import('@/views/Manager.vue'),
      redirect: '/manager/home',
      children: [
        { path: 'home', component: () => import('@/views/manager/Home.vue')},
        { path: 'admin', component: () => import('@/views/manager/Admin.vue')},
        { path: 'user', component: () => import('@/views/manager/User.vue')},
        { path: 'person', component: () => import('@/views/manager/Person.vue')},
        { path: 'Password', component: () => import('@/views/manager/Password.vue')},
        { path: 'category', component: () => import('@/views/manager/Category.vue')},
        { path: 'goods', component: () => import('@/views/manager/Goods.vue')},
        { path: 'carousel', component: () => import('@/views/manager/Carousel.vue')},
      ]
    },
    {
      path: '/front',
      component: () => import('@/views/Front.vue'),
      redirect: '/front/home',
      children: [
        { path: 'home', component: () => import('@/views/front/Home.vue')},
        { path: 'person', component: () => import('@/views/front/Person.vue')},
        { path: 'password', component: () => import('@/views/front/Password.vue')},
        { path: 'goods', component: () => import('@/views/front/Goods.vue')},
      ]
    },
    { path: '/login', component: () => import('@/views/Login.vue') },
    { path: '/register', component: () => import('@/views/Register.vue') }
  ]
})

export default router
