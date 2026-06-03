<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

import AppIcon from '../common/AppIcon.vue'
import { useAuthStore } from '../../stores/useAuthStore'

const authStore = useAuthStore()
const route = useRoute()
const menuItems = [
  { icon: 'layout-dashboard', label: '工作概览', to: '/admin/dashboard' },
  { icon: 'file-text', label: '内容管理', to: '/admin/contents' },
  { icon: 'landmark', label: '金融产品', to: '/admin/products' },
  { icon: 'users', label: '账号管理', to: '/admin/accounts' },
  { icon: 'scroll-text', label: '操作日志', to: '/admin/audit-logs' },
  { icon: 'key-round', label: '修改密码', to: '/admin/password' },
]
const breadcrumb = computed(() => {
  if (route.path === '/admin/contents/new') return '发布内容'
  if (/^\/admin\/contents\/\d+\/edit$/.test(route.path)) return '编辑内容'
  if (/^\/admin\/contents\/\d+$/.test(route.path)) return '内容详情'
  if (route.path.startsWith('/admin/contents')) return '内容管理'
  return '工作概览'
})
</script>

<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <div>
        <strong class="admin-brand">央行E平台</strong>
        <p>内容发布管理后台</p>
      </div>
      <nav class="admin-nav" aria-label="后台菜单">
        <RouterLink
          v-for="item in menuItems"
          :key="item.label"
          :class="{ active: route.path.startsWith(item.to) }"
          :to="item.to"
        >
          <AppIcon :name="item.icon" />
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>
    <main class="admin-main">
      <header class="admin-header">
        <span class="admin-breadcrumb">后台管理　&gt;　<strong>{{ breadcrumb }}</strong></span>
        <div class="admin-user">
          <span class="admin-user-avatar">管</span>
          <strong class="admin-user-details">{{ authStore.displayName }}</strong>
          <AppIcon name="chevron-down" />
        </div>
      </header>
      <RouterView />
    </main>
  </div>
</template>
