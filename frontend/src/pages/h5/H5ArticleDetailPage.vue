<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import AppIcon from '../../components/common/AppIcon.vue'
import { getPublicContentDetail } from '../../services/publicContentService'
import type { PublicContentDetailData } from '../../types/api'
import { resolveH5BackTarget } from '../../utils/h5BackTarget'

const route = useRoute()
const router = useRouter()
const article = ref<PublicContentDetailData | null>(null)
const loading = ref(true)
const errorMessage = ref('')
const activeImageUrl = ref('')

const articleId = computed(() => Number(route.params.id))
const categoryLabel = computed(() =>
  article.value?.category === 'POLICY_PROMOTION' ? '政策宣传' : '服务指引',
)

function dateOnly(dateTime: string) {
  return dateTime.slice(0, 10)
}

function fileTypeLabel(fileType: PublicContentDetailData['attachments'][number]['file_type']) {
  const labels = {
    EXCEL: 'Excel',
    PDF: 'PDF',
    WORD: 'Word',
  }

  return labels[fileType]
}

async function loadArticle() {
  loading.value = true
  errorMessage.value = ''

  try {
    const data = await getPublicContentDetail(articleId.value)
    article.value = data
  } catch {
    errorMessage.value = '文章内容加载失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push(resolveH5BackTarget(route.query))
}

function openRichTextImage(event: MouseEvent) {
  const target = event.target

  if (!(target instanceof HTMLImageElement)) return

  activeImageUrl.value = target.currentSrc || target.src
}

onMounted(loadArticle)
</script>

<template>
  <main class="h5-page h5-article-page">
    <header class="article-topbar">
      <button aria-label="返回" type="button" @click="goBack">
        <AppIcon name="chevron-left" />
      </button>
      <span>{{ categoryLabel }}</span>
    </header>

    <p v-if="loading" class="h5-feedback">正在加载...</p>
    <div v-else-if="errorMessage" class="h5-feedback">
      <p>{{ errorMessage }}</p>
      <button type="button" @click="loadArticle">重新加载</button>
    </div>

    <article v-else-if="article" class="article-detail-card">
      <h1>{{ article.title }}</h1>
      <div class="article-meta">
        <span>{{ article.office_name }}</span>
        <time :datetime="article.published_at">
          {{ dateOnly(article.published_at) }}
        </time>
      </div>

      <!-- eslint-disable-next-line vue/no-v-html -->
      <div
        class="article-rich-text"
        v-html="article.rich_text_html"
        @click="openRichTextImage"
      />

      <section v-if="article.attachments.length > 0" class="article-attachments">
        <h2>附件下载</h2>
        <a
          v-for="attachment in article.attachments.slice(0, 3)"
          :key="attachment.id"
          class="article-attachment-link"
          :download="attachment.file_name"
          :href="attachment.download_url"
        >
          <AppIcon name="paperclip" />
          <span>{{ attachment.file_name }}</span>
          <small>{{ fileTypeLabel(attachment.file_type) }}</small>
        </a>
      </section>
    </article>

    <div
      v-if="activeImageUrl"
      class="article-image-viewer"
      role="dialog"
      aria-modal="true"
      aria-label="查看原图"
      @click.self="activeImageUrl = ''"
    >
      <button
        class="article-image-viewer-close"
        aria-label="关闭原图"
        type="button"
        @click="activeImageUrl = ''"
      >
        <AppIcon name="x" />
      </button>
      <img alt="正文图片原图" :src="activeImageUrl">
    </div>
  </main>
</template>
