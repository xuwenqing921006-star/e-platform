<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import AppIcon from '../../components/common/AppIcon.vue'
import { useAuthStore } from '../../stores/useAuthStore'

const router = useRouter()
const authStore = useAuthStore()
const errorMessage = ref('')
const form = reactive({
  username: '',
  password: '',
  remember: true,
})

async function submit() {
  errorMessage.value = ''
  try {
    await authStore.login(form)
    await router.push('/admin/dashboard')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败'
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-intro">
      <div class="login-icon"><AppIcon name="landmark" /></div>
      <h1>央行 E 平台</h1>
      <h2>面向公众号 H5 的金融服务内容发布管理后台</h2>
      <p>统一管理服务指引、政策宣传、助企产品与乡村振兴服务信息。</p>
    </section>
    <section class="login-panel">
      <form class="login-card" @submit.prevent="submit">
        <h2>登录后台</h2>
        <p>请输入账号和密码继续操作</p>
        <label>
          <span>账号</span>
          <input v-model="form.username" placeholder="请输入账号" />
        </label>
        <label>
          <span>密码</span>
          <input
            v-model="form.password"
            placeholder="请输入密码"
            type="password"
          />
        </label>
        <label class="remember-row">
          <input v-model="form.remember" type="checkbox" />
          <span>记住登录状态</span>
        </label>
        <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>
        <button type="submit">登录</button>
      </form>
    </section>
  </main>
</template>
