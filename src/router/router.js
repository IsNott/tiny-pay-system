import VueRouter from 'vue-router'
import Vue from 'vue'

const routes = [
  { path: '/',component: ()=> import('views/home/index.vue') },
  { path: '/home',component: ()=> import('views/home/index.vue') },
  { path: '/qr',component: ()=> import('views/qr-pay/index.vue') }
]

const router = new VueRouter({
  mode: 'history',
  routes
})

Vue.use(VueRouter)

export default router
