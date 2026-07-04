<script setup lang="ts">
import { ref } from 'vue'

import { SAFE_LOGO_PATH, SAFE_PORTAL_URL } from '../../config/safePortal'

const expanded = ref(false)

function collapse() {
  expanded.value = false
}
</script>

<template>
  <aside class="safe-entry" :class="{ 'safe-entry-expanded': expanded }">
    <button
      v-if="!expanded"
      aria-label="展开国家外汇管理局数字外管平台入口"
      class="safe-entry-logo"
      type="button"
      @click="expanded = true"
    >
      <img :src="SAFE_LOGO_PATH" alt="SAFE" />
    </button>
    <template v-else>
      <a
        :href="SAFE_PORTAL_URL"
        aria-label="打开国家外汇管理局数字外管平台"
        class="safe-entry-link"
        rel="noopener noreferrer"
      >
        <img :src="SAFE_LOGO_PATH" alt="SAFE" />
        <span>国家外汇管理局数字外管平台</span>
      </a>
      <button
        aria-label="收起国家外汇管理局数字外管平台入口"
        class="safe-entry-close"
        type="button"
        @click="collapse"
      >
        ×
      </button>
    </template>
  </aside>
</template>
