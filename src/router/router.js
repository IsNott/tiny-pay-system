import VueRouter from 'vue-router'
import Vue from 'vue'

const routes = [
  { path: '/home', 
  component: ()=> import('views/home/index.vue') },
]

const router = new VueRouter({
  mode: 'history',
  routes
})

Vue.use(VueRouter)

export default router
