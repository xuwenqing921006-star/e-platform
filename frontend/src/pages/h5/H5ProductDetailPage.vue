<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import AppIcon from '../../components/common/AppIcon.vue'
import { getPublicProductDetail } from '../../services/publicProductService'
import type { ProductType, PublicProductDetailData } from '../../types/api'
import { resolveH5BackTarget } from '../../utils/h5BackTarget'
import { splitProductContacts } from '../../utils/productContacts'

const route = useRoute()
const router = useRouter()
const product = ref<PublicProductDetailData | null>(null)
const loading = ref(true)
const errorMessage = ref('')

const productId = computed(() => Number(route.params.id))
const productContacts = computed(() =>
  product.value ? splitProductContacts(product.value) : [],
)

function productTypeLabel(productType: ProductType) {
  return productType === 'AGRICULTURAL' ? '涉农信贷' : '小微信贷'
}

function phoneHref(contactInfo: string) {
  const phoneMatch = String(contactInfo).match(/\+?\d[\d\s-]{5,}\d/)
  const dialNumber = (phoneMatch?.[0] || contactInfo).replace(/\s+/g, '')

  return `tel:${dialNumber}`
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
  router.push(resolveH5BackTarget(route.query))
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
        <div class="product-identity-tags">
          <span class="product-bank-pill">{{ product.bank_name }}</span>
          <strong :class="`product-type-${product.product_type.toLowerCase()}`">
            {{ productTypeLabel(product.product_type) }}
          </strong>
        </div>
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
          <h2>业务经办人 / 联系方式</h2>
          <div class="product-contact-list">
            <div
              v-for="(contact, index) in productContacts"
              :key="`${contact.business_manager}-${contact.contact_info}-${index}`"
              class="product-contact-item"
            >
              <span>{{ contact.business_manager }}</span>
              <a
                v-if="contact.contact_info"
                class="product-contact-phone"
                :href="phoneHref(contact.contact_info)"
              >
                <strong>{{ contact.contact_info }}</strong>
              </a>
            </div>
          </div>
        </article>
      </section>
    </template>
  </main>
</template>
