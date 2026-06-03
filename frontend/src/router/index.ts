import {
  createRouter,
  createWebHistory,
  type RouterHistory,
} from 'vue-router'

import AdminLayout from '../components/admin/AdminLayout.vue'
import AdminContentDetailPage from '../pages/admin/AdminContentDetailPage.vue'
import AdminContentEditorPage from '../pages/admin/AdminContentEditorPage.vue'
import AdminContentListPage from '../pages/admin/AdminContentListPage.vue'
import AdminDashboardPage from '../pages/admin/AdminDashboardPage.vue'
import AdminLoginPage from '../pages/admin/AdminLoginPage.vue'
import H5ArticleDetailPage from '../pages/h5/H5ArticleDetailPage.vue'
import H5LandingPage from '../pages/h5/H5LandingPage.vue'
import H5ProductDetailPage from '../pages/h5/H5ProductDetailPage.vue'
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
        path: '/h5/products/:id',
        component: H5ProductDetailPage,
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
          {
            path: 'contents',
            component: AdminContentListPage,
          },
          {
            path: 'contents/new',
            component: AdminContentEditorPage,
          },
          {
            path: 'contents/:id/edit',
            component: AdminContentEditorPage,
          },
          {
            path: 'contents/:id',
            component: AdminContentDetailPage,
          },
        ],
      },
    ],
  })

  router.beforeEach(createAdminAuthGuard())

  return router
}

export default createAppRouter()
