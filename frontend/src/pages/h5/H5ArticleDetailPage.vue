<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import type { CSSProperties } from 'vue'
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
const imageScale = ref(1)
const imageOffsetX = ref(0)
const imageOffsetY = ref(0)
const gestureStartDistance = ref(0)
const gestureStartScale = ref(1)
const dragStartX = ref(0)
const dragStartY = ref(0)
const dragStartOffsetX = ref(0)
const dragStartOffsetY = ref(0)

const articleId = computed(() => Number(route.params.id))
const categoryLabel = computed(() =>
  article.value?.category === 'POLICY_PROMOTION' ? '政策宣传' : '服务指引',
)
const imageViewerStyle = computed<CSSProperties>(() => ({
  transform: `translate3d(${imageOffsetX.value}px, ${imageOffsetY.value}px, 0) scale(${imageScale.value})`,
}))

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

  resetImageGesture()
  activeImageUrl.value = target.currentSrc || target.src
}

function closeImageViewer() {
  activeImageUrl.value = ''
  resetImageGesture()
}

function resetImageGesture() {
  imageScale.value = 1
  imageOffsetX.value = 0
  imageOffsetY.value = 0
  gestureStartDistance.value = 0
  gestureStartScale.value = 1
}

function clampImageScale(scale: number) {
  return Math.min(Math.max(scale, 1), 4)
}

function touchDistance(touches: TouchList) {
  const first = touches.item(0)
  const second = touches.item(1)

  if (!first || !second) return 0

  return Math.hypot(second.clientX - first.clientX, second.clientY - first.clientY)
}

function startImageGesture(event: TouchEvent) {
  if (event.touches.length === 2) {
    event.preventDefault()
    gestureStartDistance.value = touchDistance(event.touches)
    gestureStartScale.value = imageScale.value
    return
  }

  const touch = event.touches.item(0)

  if (!touch || imageScale.value <= 1) return

  event.preventDefault()
  dragStartX.value = touch.clientX
  dragStartY.value = touch.clientY
  dragStartOffsetX.value = imageOffsetX.value
  dragStartOffsetY.value = imageOffsetY.value
}

function moveImageGesture(event: TouchEvent) {
  if (event.touches.length === 2 && gestureStartDistance.value > 0) {
    event.preventDefault()
    const nextDistance = touchDistance(event.touches)
    imageScale.value = clampImageScale(
      gestureStartScale.value * (nextDistance / gestureStartDistance.value),
    )

    if (imageScale.value === 1) {
      imageOffsetX.value = 0
      imageOffsetY.value = 0
    }

    return
  }

  const touch = event.touches.item(0)

  if (!touch || imageScale.value <= 1) return

  event.preventDefault()
  imageOffsetX.value = dragStartOffsetX.value + touch.clientX - dragStartX.value
  imageOffsetY.value = dragStartOffsetY.value + touch.clientY - dragStartY.value
}

function endImageGesture() {
  gestureStartDistance.value = 0
  gestureStartScale.value = imageScale.value

  if (imageScale.value <= 1.02) {
    resetImageGesture()
  }
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
      @click.self="closeImageViewer"
    >
      <button
        class="article-image-viewer-close"
        aria-label="关闭原图"
        type="button"
        @click="closeImageViewer"
      >
        <AppIcon name="x" />
      </button>
      <img
        alt="正文图片原图"
        :src="activeImageUrl"
        :style="imageViewerStyle"
        @touchstart="startImageGesture"
        @touchmove="moveImageGesture"
        @touchend="endImageGesture"
        @touchcancel="endImageGesture"
      >
    </div>
  </main>
</template>
