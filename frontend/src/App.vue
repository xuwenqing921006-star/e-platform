<script setup lang="ts">
import { useRoute } from 'vue-router'

import H5SafeEntry from './components/h5/H5SafeEntry.vue'
import { shouldShowSafeEntry } from './config/safePortal'

const route = useRoute()
</script>

<template>
  <RouterView />
  <H5SafeEntry v-if="shouldShowSafeEntry(route.path)" />
</template>
