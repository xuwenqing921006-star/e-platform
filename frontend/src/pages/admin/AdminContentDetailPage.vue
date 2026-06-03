<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

import AppIcon from '../../components/common/AppIcon.vue'
import { adminContentCategoryLabels } from '../../config/adminContent'
import { getAdminContentDetail } from '../../services/adminContentService'
import type { AdminContentDetailData } from '../../types/api'

const route = useRoute()
const content = ref<AdminContentDetailData | null>(null)
const contentId = computed(() => Number(route.params.id))

onMounted(async () => {
  content.value = await getAdminContentDetail(contentId.value)
})

function dateOnly(dateTime: string) {
  return dateTime.slice(0, 10)
}
</script>

<template>
  <section v-if="content" class="admin-content-page">
    <div class="admin-page-title-row">
      <div>
        <h1>内容详情</h1>
        <p>查看 H5 页面将展示的完整内容</p>
      </div>
      <div class="admin-page-actions">
        <RouterLink class="admin-secondary-button" to="/admin/contents">
          返回列表
        </RouterLink>
        <RouterLink
          class="admin-primary-button"
          :to="`/admin/contents/${content.id}/edit`"
        >
          编辑内容
        </RouterLink>
      </div>
    </div>

    <p v-if="route.query.saved === '1'" class="admin-feedback">
      内容已保存并发布
    </p>

    <article class="content-detail-card">
      <strong class="content-category-badge">
        {{ adminContentCategoryLabels[content.category] }}
      </strong>
      <h2>{{ content.title }}</h2>
      <p class="content-detail-meta">
        发布机构：{{ content.office_name }}　 发布时间：{{ dateOnly(content.published_at) }}
      </p>
      <div class="content-detail-rich-text" v-html="content.rich_text_html" />
      <section v-if="content.attachments.length" class="content-detail-attachments">
        <h3>附件下载</h3>
        <a
          v-for="attachment in content.attachments"
          :key="attachment.id"
          :href="attachment.download_url"
        >
          <AppIcon name="paperclip" />
          {{ attachment.file_name }}
        </a>
      </section>
    </article>
  </section>
</template>
