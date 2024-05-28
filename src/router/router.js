import Vue from 'vue';
import { createMemoryHistory, createRouter } from 'vue-router'

import HomeView from 'views/home'

const routes = [
  { path: '/home', component: HomeView },
]


const router = createRouter({
  history: createMemoryHistory(),
  routes,
})

export default router
