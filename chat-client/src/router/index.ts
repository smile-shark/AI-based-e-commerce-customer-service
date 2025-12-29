import { createRouter, createWebHistory } from 'vue-router'
import CtHome from '../views/ct/CtHome.vue'
import UserHome from '../views/user/UserHome.vue'
import CtLogin from '../views/ct/Login.vue'
import UserLogin from '../views/user/Login.vue'
import GoodsList from '../views/user/GoodsList.vue'
import UserChat from '../views/user/UserChat.vue'

const routes = [
  {
    path: '/',
    redirect: '/user/login'
  },
  {
    path: '/ct/login',
    name: 'CtLogin',
    component: CtLogin
  },
  {
    path: '/ct/home',
    name: 'CustomerService',
    component: CtHome
  },
  {
    path: '/user/login',
    name: 'UserLogin',
    component: UserLogin
  },
  {
    path: '/user/home',
    name: 'UserManagement',
    component: UserHome
  },
  {
    path: '/user/goods',
    name: 'GoodsList',
    component: GoodsList
  },
  {
    path: '/user/chat',
    name: 'UserChat',
    component: UserChat
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
