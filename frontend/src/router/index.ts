import {
  createRouter,
  createWebHistory,
  type RouterHistory,
} from 'vue-router'

import AdminLayout from '../components/admin/AdminLayout.vue'
import AdminDashboardPage from '../pages/admin/AdminDashboardPage.vue'
import AdminLoginPage from '../pages/admin/AdminLoginPage.vue'
import H5ArticleDetailPage from '../pages/h5/H5ArticleDetailPage.vue'
import H5LandingPage from '../pages/h5/H5LandingPage.vue'
import { createAdminAuthGuard } from './authGuard'

export function createAppRouter(history: RouterHistory = createWebHistory()) {
  const router = createRouter({
    history,
    routes: [
      {
        path: '/',
        redirect: '/h5/',
      },
      {
        path: '/h5/',
        component: H5LandingPage,
      },
      {
        path: '/h5/contents/:id',
        component: H5ArticleDetailPage,
      },
      {
        path: '/admin/login',
        component: AdminLoginPage,
      },
      {
        path: '/admin',
        component: AdminLayout,
        meta: {
          requiresAuth: true,
        },
        children: [
          {
            path: '',
            redirect: '/admin/dashboard',
          },
          {
            path: 'dashboard',
            component: AdminDashboardPage,
          },
        ],
      },
    ],
  })

  router.beforeEach(createAdminAuthGuard())

  return router
}

export default createAppRouter()
