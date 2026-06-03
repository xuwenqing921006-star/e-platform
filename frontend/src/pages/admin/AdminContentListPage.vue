<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'

import {
  adminContentCategoryLabels,
  adminContentCategoryOptions,
  adminContentOfficeOptions,
} from '../../config/adminContent'
import {
  deleteAdminContent,
  getAdminContents,
} from '../../services/adminContentService'
import type { AdminContentListData, AdminContentListParams } from '../../types/api'

const filters = reactive<AdminContentListParams>({
  keyword: '',
  category: '',
  office_code: '',
  published_from: '',
  published_to: '',
  page: 1,
  page_size: 20,
})
const result = ref<AdminContentListData>({
  items: [],
  total: 0,
  page: 1,
  page_size: 20,
})
const feedback = ref('')
const totalPages = computed(() =>
  Math.max(1, Math.ceil(result.value.total / result.value.page_size)),
)
const pageNumbers = computed(() =>
  Array.from({ length: totalPages.value }, (_, index) => index + 1),
)
const canGoPrevious = computed(() => result.value.page > 1)
const canGoNext = computed(() => result.value.page < totalPages.value)

async function loadContents() {
  result.value = await getAdminContents(filters)
}

async function goToPage(page: number) {
  if (page < 1 || page > totalPages.value || page === result.value.page) return

  filters.page = page
  feedback.value = ''
  await loadContents()
}

async function search() {
  filters.page = 1
  feedback.value = ''
  await loadContents()
}

async function reset() {
  Object.assign(filters, {
    keyword: '',
    category: '',
    office_code: '',
    published_from: '',
    published_to: '',
    page: 1,
  })
  feedback.value = ''
  await loadContents()
}

async function removeContent(id: number) {
  if (!window.confirm('确认删除该内容吗？')) return

  await deleteAdminContent(id)
  feedback.value = '内容已删除'
  await loadContents()
}

function dateOnly(dateTime: string) {
  return dateTime.slice(0, 10)
}

onMounted(loadContents)
</script>

<template>
  <section class="admin-content-page">
    <div class="admin-page-title-row">
      <div>
        <h1>内容管理</h1>
        <p>检索、发布和维护 H5 展示内容</p>
      </div>
      <RouterLink class="admin-primary-button" to="/admin/contents/new">
        发布内容
      </RouterLink>
    </div>

    <form class="content-filter-card" @submit.prevent="search">
      <input v-model="filters.keyword" placeholder="输入标题关键词" />
      <select v-model="filters.category">
        <option value="">全部分类</option>
        <option
          v-for="option in adminContentCategoryOptions"
          :key="option.value"
          :value="option.value"
        >
          {{ option.label }}
        </option>
      </select>
      <select v-model="filters.office_code">
        <option value="">全部机构</option>
        <option
          v-for="option in adminContentOfficeOptions"
          :key="option.value"
          :value="option.value"
        >
          {{ option.label }}
        </option>
      </select>
      <div class="content-date-range">
        <input v-model="filters.published_from" aria-label="发布时间起始" type="date" />
        <span>至</span>
        <input v-model="filters.published_to" aria-label="发布时间结束" type="date" />
      </div>
      <button class="admin-primary-button" type="submit">查询</button>
      <button class="admin-secondary-button" type="button" @click="reset">
        重置
      </button>
    </form>

    <p v-if="feedback" class="admin-feedback">{{ feedback }}</p>

    <div class="admin-table-card">
      <table class="admin-table">
        <thead>
          <tr>
            <th>标题</th>
            <th>分类</th>
            <th>发布机构</th>
            <th>发布时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in result.items" :key="item.id">
            <td>{{ item.title }}</td>
            <td>{{ adminContentCategoryLabels[item.category] }}</td>
            <td>{{ item.office_name }}</td>
            <td>{{ dateOnly(item.published_at) }}</td>
            <td class="admin-table-actions">
              <RouterLink :to="`/admin/contents/${item.id}`">查看</RouterLink>
              <RouterLink :to="`/admin/contents/${item.id}/edit`">编辑</RouterLink>
              <button type="button" @click="removeContent(item.id)">删除</button>
            </td>
          </tr>
          <tr v-if="!result.items.length">
            <td class="admin-empty-cell" colspan="5">暂无匹配内容</td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class="admin-table-footer">
      <p class="admin-table-summary">
        共 {{ result.total }} 条内容，第 {{ result.page }} / {{ totalPages }} 页
      </p>
      <nav class="admin-pagination" aria-label="内容分页">
        <button
          type="button"
          :disabled="!canGoPrevious"
          @click="goToPage(result.page - 1)"
        >
          上一页
        </button>
        <button
          v-for="page in pageNumbers"
          :key="page"
          type="button"
          :aria-current="page === result.page ? 'page' : undefined"
          :class="{ active: page === result.page }"
          @click="goToPage(page)"
        >
          {{ page }}
        </button>
        <button
          type="button"
          :disabled="!canGoNext"
          @click="goToPage(result.page + 1)"
        >
          下一页
        </button>
      </nav>
    </div>
  </section>
</template>
