<script setup lang="ts">
defineProps<{
  name: string
}>()
</script>

<template>
  <svg
    aria-hidden="true"
    fill="none"
    stroke="currentColor"
    stroke-linecap="round"
    stroke-linejoin="round"
    stroke-width="1.9"
    viewBox="0 0 24 24"
  >
    <template v-if="name === 'landmark'">
      <path d="m3 10 9-5 9 5" />
      <path d="M5 10h14M6 18h12M4 21h16M7 10v8M12 10v8M17 10v8" />
    </template>
    <template v-else-if="name === 'layout-dashboard'">
      <rect height="7" rx="1" width="7" x="3" y="3" />
      <rect height="7" rx="1" width="7" x="14" y="3" />
      <rect height="7" rx="1" width="7" x="3" y="14" />
      <rect height="7" rx="1" width="7" x="14" y="14" />
    </template>
    <template v-else-if="name === 'file-text'">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8Z" />
      <path d="M14 2v6h6M8 13h8M8 17h8M8 9h2" />
    </template>
    <template v-else-if="name === 'users'">
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" />
    </template>
    <template v-else-if="name === 'scroll-text'">
      <path d="M8 21h9a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2H7a2 2 0 0 0-2 2v14" />
      <path d="M5 21a2 2 0 1 1 0-4h14M9 7h6M9 11h6" />
    </template>
    <template v-else-if="name === 'key-round'">
      <circle cx="7.5" cy="15.5" r="5.5" />
      <path d="m21 2-9.6 9.6M15.5 7.5l3 3M18 5l3 3" />
    </template>
    <template v-else-if="name === 'chevron-down'">
      <path d="m6 9 6 6 6-6" />
    </template>
    <template v-else-if="name === 'file-check'">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8Z" />
      <path d="M14 2v6h6m-7.5 5.5-3 3-1.5-1.5" />
    </template>
    <template v-else-if="name === 'activity'">
      <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
    </template>
    <template v-else-if="name === 'file-plus-2'">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8Z" />
      <path d="M14 2v6h6M12 18v-6M9 15h6" />
    </template>
    <template v-else-if="name === 'badge-plus'">
      <path d="M12 3 14.1 5.2l3-.4.8 2.9 2.7 1.4-1.4 2.7.4 3-2.9.8-1.4 2.7-3-.4L10.2 20l-2.1-2.2-3 .4-.8-2.9-2.7-1.4L3 11.2l-.4-3 2.9-.8L6.9 4.7l3 .4Z" />
      <path d="M12 8v8M8 12h8" />
    </template>
    <template v-else-if="name === 'sheet'">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8Z" />
      <path d="M14 2v6h6M8 13h8M8 17h8M12 9v12" />
    </template>
    <template v-else-if="name === 'building-2'">
      <path d="M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18M6 12H4a2 2 0 0 0-2 2v8h20v-8a2 2 0 0 0-2-2h-2M10 6h4M10 10h4M10 14h4M10 18h4" />
    </template>
    <template v-else-if="name === 'clock-3'">
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5h4" />
    </template>
  </svg>
</template>
