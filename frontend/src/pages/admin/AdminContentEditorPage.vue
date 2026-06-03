<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import AppIcon from '../../components/common/AppIcon.vue'
import {
  adminContentCategoryOptions,
  adminContentOfficeOptions,
} from '../../config/adminContent'
import {
  createAdminContent,
  getAdminContentDetail,
  updateAdminContent,
} from '../../services/adminContentService'
import {
  deleteAdminAttachment,
  uploadAdminAttachment,
} from '../../services/attachmentService'
import type {
  AdminContentAttachment,
  AdminContentSaveRequest,
} from '../../types/api'

const route = useRoute()
const router = useRouter()
const editor = ref<HTMLElement | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)
const attachments = ref<AdminContentAttachment[]>([])
const errorMessage = ref('')
const mockPrefix = ['[', 'Mock', ']'].join('')
const form = reactive<AdminContentSaveRequest>({
  title: '',
  category: 'SERVICE_GUIDE',
  office_code: 'CREDIT_REPORT',
  rich_text_html: '',
  attachment_ids: [],
})
const contentId = computed(() => Number(route.params.id || 0))
const isEditing = computed(() => Boolean(route.params.id))

async function loadContent() {
  if (!isEditing.value) return

  const detail = await getAdminContentDetail(contentId.value)
  Object.assign(form, {
    title: detail.title,
    category: detail.category,
    office_code: detail.office_code,
    rich_text_html: detail.rich_text_html,
    attachment_ids: detail.attachments.map((attachment) => attachment.id),
  })
  attachments.value = detail.attachments
  await nextTick()
  if (editor.value) editor.value.innerHTML = form.rich_text_html
}

function format(command: string) {
  editor.value?.focus()
  document.execCommand(command)
}

function insertRichHtml(html: string) {
  if (!editor.value) return

  editor.value.focus()
  document.execCommand('insertHTML', false, html)
  form.rich_text_html = editor.value.innerHTML
}

function insertMockLink() {
  insertRichHtml(
    `<a href="/h5/contents/201">${mockPrefix} 查看关联服务指引</a>`,
  )
}

function insertMockImage() {
  insertRichHtml(
    `<p><img src="/article-credit-service-long.svg" alt="${mockPrefix} 正文配图"></p>`,
  )
}

function insertMockTable() {
  insertRichHtml(
    `<table><tbody><tr><th>事项</th><th>说明</th></tr><tr><td>${mockPrefix} 办理材料</td><td>${mockPrefix} 有效身份证件</td></tr></tbody></table>`,
  )
}

function syncRichText(event: Event) {
  form.rich_text_html = (event.currentTarget as HTMLElement).innerHTML
}

async function addAttachments(event: Event) {
  const input = event.currentTarget as HTMLInputElement
  const files = Array.from(input.files || [])
  errorMessage.value = ''

  for (const file of files) {
    if (attachments.value.length >= 3) {
      errorMessage.value = '每篇内容最多上传 3 个附件'
      break
    }

    try {
      const attachment = await uploadAdminAttachment(file)
      attachments.value.push(attachment)
      form.attachment_ids.push(attachment.id)
    } catch (uploadError) {
      errorMessage.value =
        uploadError instanceof Error ? uploadError.message : '附件上传失败'
      break
    }
  }

  input.value = ''
}

async function removeAttachment(id: number) {
  errorMessage.value = ''
  try {
    await deleteAdminAttachment(id)
    attachments.value = attachments.value.filter(
      (attachment) => attachment.id !== id,
    )
    form.attachment_ids = form.attachment_ids.filter(
      (attachmentId) => attachmentId !== id,
    )
  } catch (deleteError) {
    errorMessage.value =
      deleteError instanceof Error ? deleteError.message : '附件删除失败'
  }
}

async function save() {
  errorMessage.value = ''
  form.rich_text_html = editor.value?.innerHTML || ''

  try {
    const id = isEditing.value
      ? (await updateAdminContent(contentId.value, form)).id
      : (await createAdminContent(form)).id
    await router.push({
      path: `/admin/contents/${id}`,
      query: {
        saved: '1',
      },
    })
  } catch (saveError) {
    errorMessage.value =
      saveError instanceof Error ? saveError.message : '内容保存失败'
  }
}

onMounted(loadContent)
</script>

<template>
  <section class="admin-content-page">
    <div class="admin-page-title-row">
      <div>
        <h1>{{ isEditing ? '编辑内容' : '发布内容' }}</h1>
        <p>填写信息并保存，内容将立即展示在 H5 页面</p>
      </div>
      <div class="admin-page-actions">
        <RouterLink class="admin-secondary-button" to="/admin/contents">
          取消
        </RouterLink>
        <button class="admin-primary-button" type="button" @click="save">
          保存并发布
        </button>
      </div>
    </div>

    <div class="content-editor-card">
      <h2>基础信息</h2>
      <div class="content-form-grid">
        <label class="content-title-field">
          <span>内容标题</span>
          <input v-model="form.title" placeholder="请输入内容标题" />
        </label>
        <label>
          <span>展示分类</span>
          <select v-model="form.category">
            <option
              v-for="option in adminContentCategoryOptions"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </option>
          </select>
        </label>
        <label>
          <span>发布机构</span>
          <select v-model="form.office_code">
            <option
              v-for="option in adminContentOfficeOptions"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </option>
          </select>
        </label>
      </div>

      <label class="content-rich-label">正文内容</label>
      <div class="rich-text-box">
        <div class="rich-text-toolbar" aria-label="富文本工具栏">
          <button type="button" @click="format('bold')"><strong>B</strong></button>
          <button type="button" @click="format('italic')"><em>I</em></button>
          <button type="button" @click="format('underline')"><u>U</u></button>
          <button type="button" @click="format('insertUnorderedList')">• 列表</button>
          <button type="button" @click="format('insertOrderedList')">1. 列表</button>
          <button
            aria-label="插入链接"
            title="插入链接"
            type="button"
            @click="insertMockLink"
          >
            <AppIcon name="link" />
          </button>
          <button
            aria-label="插入图片"
            title="插入图片"
            type="button"
            @click="insertMockImage"
          >
            <AppIcon name="image" />
          </button>
          <button
            aria-label="插入表格"
            title="插入表格"
            type="button"
            @click="insertMockTable"
          >
            <AppIcon name="table" />
          </button>
        </div>
        <div
          ref="editor"
          class="rich-text-editor"
          contenteditable="true"
          @input="syncRichText"
        />
      </div>

      <div class="attachment-heading">
        <strong>附件（最多 3 个，单个不超过 20MB）</strong>
        <span>{{ attachments.length }}/3</span>
      </div>
      <button
        class="attachment-upload-button"
        type="button"
        @click="fileInput?.click()"
      >
        <AppIcon name="file-plus-2" />
        点击上传 PDF、Word 或 Excel 文件
      </button>
      <input
        ref="fileInput"
        accept=".pdf,.doc,.docx,.xls,.xlsx"
        class="visually-hidden"
        multiple
        type="file"
        @change="addAttachments"
      />
      <ul v-if="attachments.length" class="admin-attachment-list">
        <li v-for="attachment in attachments" :key="attachment.id">
          <AppIcon name="paperclip" />
          <span>{{ attachment.file_name }}</span>
          <button type="button" @click="removeAttachment(attachment.id)">
            删除
          </button>
        </li>
      </ul>
      <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>
    </div>
  </section>
</template>
