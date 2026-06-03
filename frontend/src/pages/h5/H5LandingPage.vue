<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

import AppIcon from '../../components/common/AppIcon.vue'
import {
  COUNTY_SERVICE_TEAMS,
  FINANCIAL_TABS,
  H5_MOCK_NOTICE,
  PRIMARY_TABS,
  RURAL_SECTION_TABS,
  RURAL_TABS,
} from '../../config/h5Home'
import { listPublicContents } from '../../services/publicContentService'
import { listPublicProducts } from '../../services/publicProductService'
import type {
  ContentCategory,
  CountyCode,
  PublicContentListItem,
  PublicProductListItem,
  PublicScope,
} from '../../types/api'

type PrimaryTab = PublicScope
type RuralSection = (typeof RURAL_SECTION_TABS)[number]['id']
type FinancialTab = (typeof FINANCIAL_TABS)[number]['id']

const PAGE_SIZE = 3
const PRODUCT_PAGE_SIZE = 4
const showMockNotice = import.meta.env.VITE_USE_MOCK !== 'false'

const primaryTab = ref<PrimaryTab>('RURAL')
const countyCode = ref<CountyCode>('ZHAOZHOU')
const ruralSection = ref<RuralSection>('SERVICE_GUIDE')
const financialTab = ref<FinancialTab>('SERVICE_GUIDE')
const contentItems = ref<PublicContentListItem[]>([])
const productItems = ref<PublicProductListItem[]>([])
const page = ref(1)
const total = ref(0)
const loading = ref(false)
const errorMessage = ref('')
let autoLoadTimer = 0

const showingProducts = computed(
  () => primaryTab.value === 'FINANCIAL' && financialTab.value === 'PRODUCTS',
)
const showingServiceTeam = computed(
  () => primaryTab.value === 'RURAL' && ruralSection.value === 'SERVICE_TEAM',
)
const currentItemsCount = computed(() =>
  showingProducts.value ? productItems.value.length : contentItems.value.length,
)
const hasMore = computed(
  () =>
    !showingServiceTeam.value &&
    currentItemsCount.value > 0 &&
    currentItemsCount.value < total.value,
)
const selectedTeam = computed(() => COUNTY_SERVICE_TEAMS[countyCode.value])

function dateOnly(dateTime: string) {
  return dateTime.slice(0, 10)
}

function productTypeLabel(productType: PublicProductListItem['product_type']) {
  return productType === 'AGRICULTURAL' ? '涉农信贷' : '小微信贷'
}

async function loadList(reset = true) {
  if (showingServiceTeam.value) return

  if (reset) {
    page.value = 1
    contentItems.value = []
    productItems.value = []
  }

  loading.value = true
  errorMessage.value = ''

  try {
    if (showingProducts.value) {
      const data = await listPublicProducts({
        page: page.value,
        page_size: PRODUCT_PAGE_SIZE,
      })
      productItems.value = reset
        ? data?.items || []
        : [...productItems.value, ...(data?.items || [])]
      total.value = data?.total || 0
      return
    }

    const scope = primaryTab.value
    const category: ContentCategory =
      scope === 'FINANCIAL' && financialTab.value === 'POLICY_PROMOTION'
        ? 'POLICY_PROMOTION'
        : 'SERVICE_GUIDE'
    const data = await listPublicContents({
      category,
      ...(scope === 'RURAL' ? { county_code: countyCode.value } : {}),
      page: page.value,
      page_size: PAGE_SIZE,
      scope,
    })
    contentItems.value = reset
      ? data?.items || []
      : [...contentItems.value, ...(data?.items || [])]
    total.value = data?.total || 0
  } catch {
    errorMessage.value = '内容加载失败，请检查网络后重试。'
  } finally {
    loading.value = false
    scheduleViewportAutoLoad()
  }
}

async function selectPrimary(tab: PrimaryTab) {
  primaryTab.value = tab
  await loadList()
}

async function selectCounty(tab: CountyCode) {
  countyCode.value = tab
  if (ruralSection.value === 'SERVICE_GUIDE') await loadList()
}

async function selectRuralSection(tab: RuralSection) {
  ruralSection.value = tab
  if (tab === 'SERVICE_GUIDE') await loadList()
}

async function selectFinancialTab(tab: FinancialTab) {
  financialTab.value = tab
  await loadList()
}

async function loadNextPage() {
  if (loading.value || !hasMore.value) return

  page.value += 1
  await loadList(false)
}

function scheduleViewportAutoLoad() {
  window.clearTimeout(autoLoadTimer)
  autoLoadTimer = window.setTimeout(() => {
    const root = document.documentElement
    if (root.scrollHeight > 0 && root.scrollHeight <= window.innerHeight + 160) {
      void loadNextPage()
    }
  }, 120)
}

function handleWindowScroll() {
  const root = document.documentElement
  const distanceToBottom = root.scrollHeight - window.innerHeight - window.scrollY

  if (distanceToBottom <= 160) void loadNextPage()
}

onMounted(() => {
  void loadList()
  window.addEventListener('scroll', handleWindowScroll, { passive: true })
})

onBeforeUnmount(() => {
  window.clearTimeout(autoLoadTimer)
  window.removeEventListener('scroll', handleWindowScroll)
})
</script>

<template>
  <main class="h5-page">
    <p v-if="showMockNotice" class="h5-mock-notice">{{ H5_MOCK_NOTICE }}</p>
    <nav class="h5-primary-tabs" aria-label="首页栏目">
      <button
        v-for="tab in PRIMARY_TABS"
        :key="tab.id"
        :class="{ active: primaryTab === tab.id }"
        type="button"
        @click="selectPrimary(tab.id)"
      >
        {{ tab.label }}
      </button>
    </nav>

    <template v-if="primaryTab === 'RURAL'">
      <nav class="h5-pill-tabs h5-county-tabs" aria-label="县域">
        <button
          v-for="tab in RURAL_TABS"
          :key="tab.id"
          :class="{ active: countyCode === tab.id }"
          type="button"
          @click="selectCounty(tab.id)"
        >
          {{ tab.label }}
        </button>
      </nav>
      <nav class="h5-section-tabs" aria-label="乡村振兴栏目">
        <button
          v-for="tab in RURAL_SECTION_TABS"
          :key="tab.id"
          :class="{ active: ruralSection === tab.id }"
          type="button"
          @click="selectRuralSection(tab.id)"
        >
          {{ tab.label }}
        </button>
      </nav>
    </template>

    <nav v-else class="h5-pill-tabs h5-financial-tabs" aria-label="金融服务栏目">
      <button
        v-for="tab in FINANCIAL_TABS"
        :key="tab.id"
        :class="{ active: financialTab === tab.id }"
        type="button"
        @click="selectFinancialTab(tab.id)"
      >
        {{ tab.label }}
      </button>
    </nav>

    <section v-if="showingServiceTeam" class="h5-team-card">
      <h1>{{ selectedTeam.title }}</h1>
      <p>{{ selectedTeam.description }}</p>
      <article v-for="member in selectedTeam.members" :key="member.phone">
        <div>
          <strong>{{ member.name }}</strong>
          <span>{{ member.role }}</span>
        </div>
        <a :href="`tel:${member.phone}`">
          <AppIcon name="phone" />{{ member.phone }}
        </a>
      </article>
    </section>

    <template v-else>
      <div v-if="showingProducts" class="h5-product-summary">
        <span>共 {{ total }} 项助企金融产品</span>
        <strong>持续更新</strong>
      </div>

      <p v-if="loading && currentItemsCount === 0" class="h5-feedback">
        正在加载...
      </p>
      <div v-else-if="errorMessage" class="h5-feedback">
        <p>{{ errorMessage }}</p>
        <button type="button" @click="loadList()">重新加载</button>
      </div>
      <p v-else-if="currentItemsCount === 0" class="h5-feedback">
        暂无相关内容
      </p>

      <section v-else-if="showingProducts" class="h5-card-list">
        <RouterLink
          v-for="item in productItems"
          :key="item.id"
          class="h5-product-card"
          :to="`/h5/products/${item.id}`"
        >
          <div>
            <span>{{ item.bank_name }}</span>
            <strong :class="`product-type-${item.product_type.toLowerCase()}`">
              {{ productTypeLabel(item.product_type) }}
            </strong>
          </div>
          <h1>{{ item.product_name }}</h1>
        </RouterLink>
      </section>

      <section v-else class="h5-card-list">
        <RouterLink
          v-for="item in contentItems"
          :key="item.id"
          class="h5-content-card"
          :to="`/h5/contents/${item.id}`"
        >
          <h1>{{ item.title }}</h1>
          <div>
            <span><AppIcon name="building-2" />{{ item.office_name }}</span>
            <time :datetime="item.published_at">
              <AppIcon name="clock-3" />{{ dateOnly(item.published_at) }}
            </time>
          </div>
        </RouterLink>
      </section>

    </template>
  </main>
</template>
