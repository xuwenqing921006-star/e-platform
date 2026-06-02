<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import AppIcon from '../../components/common/AppIcon.vue'
import { getPublicProductDetail } from '../../services/publicProductService'
import type { ProductType, PublicProductDetailData } from '../../types/api'

const route = useRoute()
const router = useRouter()
const product = ref<PublicProductDetailData | null>(null)
const loading = ref(true)
const errorMessage = ref('')

const productId = computed(() => Number(route.params.id))

function productTypeLabel(productType: ProductType) {
  return productType === 'AGRICULTURAL' ? '涉农信贷' : '小微信贷'
}

async function loadProduct() {
  loading.value = true
  errorMessage.value = ''

  try {
    const data = await getPublicProductDetail(productId.value)
    product.value = data
  } catch {
    errorMessage.value = '产品详情加载失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }

  router.push('/h5/')
}

onMounted(loadProduct)
</script>

<template>
  <main class="h5-page h5-product-detail-page">
    <header class="article-topbar">
      <button aria-label="返回" type="button" @click="goBack">
        <AppIcon name="chevron-left" />
      </button>
      <span>助企通道</span>
    </header>

    <p v-if="loading" class="h5-feedback">正在加载...</p>
    <div v-else-if="errorMessage" class="h5-feedback">
      <p>{{ errorMessage }}</p>
      <button type="button" @click="loadProduct">重新加载</button>
    </div>

    <template v-else-if="product">
      <section class="product-identity-card">
        <h1>{{ product.product_name }}</h1>
        <span class="product-bank-pill">{{ product.bank_name }}</span>
        <strong :class="`product-type-${product.product_type.toLowerCase()}`">
          {{ productTypeLabel(product.product_type) }}
        </strong>
      </section>

      <section class="product-detail-card">
        <article class="product-detail-field">
          <h2>准入条件</h2>
          <p>{{ product.admission_conditions }}</p>
        </article>
        <article class="product-detail-field">
          <h2>产品介绍</h2>
          <p>{{ product.product_intro }}</p>
        </article>
        <article class="product-detail-field">
          <h2>业务经办人</h2>
          <p>{{ product.business_manager }}</p>
        </article>
        <article class="product-detail-field">
          <h2>联系方式</h2>
          <p>{{ product.contact_info }}</p>
        </article>
      </section>
    </template>
  </main>
</template>
