<script setup lang="ts">
import AppIcon from '../common/AppIcon.vue'
import { useAuthStore } from '../../stores/useAuthStore'

const authStore = useAuthStore()
const menuItems = [
  { icon: 'layout-dashboard', label: '工作概览' },
  { icon: 'file-text', label: '内容管理' },
  { icon: 'landmark', label: '金融产品' },
  { icon: 'users', label: '账号管理' },
  { icon: 'scroll-text', label: '操作日志' },
  { icon: 'key-round', label: '修改密码' },
]
</script>

<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <div>
        <strong class="admin-brand">央行E平台</strong>
        <p>内容发布管理后台</p>
      </div>
      <nav class="admin-nav" aria-label="后台菜单">
        <span
          v-for="(item, index) in menuItems"
          :key="item.label"
          :class="{ active: index === 0 }"
        >
          <AppIcon :name="item.icon" />
          {{ item.label }}
        </span>
      </nav>
    </aside>
    <main class="admin-main">
      <header class="admin-header">
        <span class="admin-breadcrumb">后台管理　&gt;　<strong>工作概览</strong></span>
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
