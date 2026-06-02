<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import AppIcon from '../../components/common/AppIcon.vue'
import { getDashboardSummary } from '../../services/dashboardService'
import type { ContentCategory, DashboardSummaryData } from '../../types/api'

const summary = ref<DashboardSummaryData | null>(null)

const metrics = computed(() => [
  {
    icon: 'file-check',
    label: '已发布内容',
    tone: 'blue',
    value: summary.value?.published_content_count ?? 0,
  },
  {
    icon: 'landmark',
    label: '金融产品',
    tone: 'green',
    value: summary.value?.product_count ?? 0,
  },
  {
    icon: 'users',
    label: '后台账号',
    tone: 'purple',
    value: summary.value?.account_count ?? 0,
  },
  {
    icon: 'activity',
    label: '今日操作',
    tone: 'orange',
    value: summary.value?.today_operation_count ?? 0,
  },
])

const quickEntries = [
  { icon: 'file-plus-2', label: '发布内容' },
  { icon: 'badge-plus', label: '新增金融产品' },
  { icon: 'sheet', label: '导入产品表格' },
]

const categoryLabels: Record<ContentCategory, string> = {
  POLICY_PROMOTION: '政策宣传',
  SERVICE_GUIDE: '服务指引',
}

onMounted(async () => {
  summary.value = await getDashboardSummary()
})

function dateOnly(dateTime: string) {
  return dateTime.slice(0, 10)
}
</script>

<template>
  <section class="dashboard-page">
    <h1>工作概览</h1>
    <p>查看平台内容、产品与账号的当前状态</p>
    <div class="metric-grid">
      <article v-for="metric in metrics" :key="metric.label" class="metric-card">
        <div :class="['metric-icon', `metric-icon-${metric.tone}`]">
          <AppIcon :name="metric.icon" />
        </div>
        <div class="metric-card-content">
          <span>{{ metric.label }}</span>
          <strong>{{ metric.value }}</strong>
        </div>
      </article>
    </div>
    <div class="dashboard-grid">
      <section class="dashboard-card">
        <h2>最近发布内容</h2>
        <div class="recent-content-list">
          <article v-for="item in summary?.recent_contents || []" :key="item.id">
            <strong>{{ item.title }}</strong>
            <span>{{ categoryLabels[item.category] }} · {{ dateOnly(item.published_at) }}</span>
          </article>
        </div>
      </section>
      <section class="dashboard-card">
        <h2>快捷操作</h2>
        <div class="quick-entry-list">
          <div v-for="entry in quickEntries" :key="entry.label" class="quick-entry">
            <AppIcon :name="entry.icon" />
            <strong>{{ entry.label }}</strong>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>
