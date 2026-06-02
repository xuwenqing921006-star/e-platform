<script setup lang="ts">
import { onMounted, ref } from 'vue'

import AppIcon from '../../components/common/AppIcon.vue'
import api from '../../services/api'
import type {
  ApiResponse,
  PaginatedData,
  PublicContentListItem,
} from '../../types/api'

const items = ref<PublicContentListItem[]>([])

onMounted(async () => {
  const response = await api.get<ApiResponse<PaginatedData<PublicContentListItem>>>(
    '/public/contents',
    {
      params: {
        category: 'SERVICE_GUIDE',
        county_code: 'ZHAOZHOU',
        page: 1,
        page_size: 10,
        scope: 'RURAL',
      },
    },
  )
  items.value = response.data.data?.items || []
})

function dateOnly(dateTime: string) {
  return dateTime.slice(0, 10)
}
</script>

<template>
  <main class="h5-page">
    <div class="h5-primary-tabs">
      <strong>乡村振兴</strong>
      <span>金融服务</span>
    </div>
    <nav class="h5-county-tabs" aria-label="县域">
      <strong>肇州县</strong>
      <span>肇源县</span>
      <span>林甸县</span>
      <span>杜蒙县</span>
    </nav>
    <nav class="h5-section-tabs" aria-label="栏目">
      <strong>服务指引</strong>
      <span>金融业务服务队</span>
    </nav>
    <section class="h5-card-list">
      <article v-for="item in items" :key="item.id" class="h5-content-card">
        <h1>{{ item.title }}</h1>
        <div>
          <span><AppIcon name="building-2" />{{ item.office_name }}</span>
          <time :datetime="item.published_at">
            <AppIcon name="clock-3" />{{ dateOnly(item.published_at) }}
          </time>
        </div>
      </article>
    </section>
    <a
      aria-label="国家外汇管理局数字外管平台"
      class="safe-entry"
      href="http://zwfw.safe.gov.cn/asone/"
    >
      <img alt="SAFE" src="/safe-logo.png" />
    </a>
  </main>
</template>
