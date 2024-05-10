import { createRouter, createWebHashHistory } from "vue-router"
 
const routes = [
    {
      path: '',
      name: "Home",
      component: () => import('@/views/Home.vue')   
    },
    {
      path: '/chatRoom',
      name: "ChatRoom",
      component: () => import('@/views/ChatRoom.vue')   
    }
]
export const router = createRouter({
  history: createWebHashHistory(),
  routes: routes
})
 
export default router