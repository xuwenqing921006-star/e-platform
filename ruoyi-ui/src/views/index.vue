<template>
  <div class="app-container dashboard-page">
    <div class="page-head">
      <el-breadcrumb separator=">">
        <el-breadcrumb-item>后台管理</el-breadcrumb-item>
        <el-breadcrumb-item>工作概览</el-breadcrumb-item>
      </el-breadcrumb>
      <h2>工作概览</h2>
      <p>查看平台内容、产品与账号的当前状态</p>
    </div>

    <div class="metric-grid">
      <div v-for="item in metrics" :key="item.label" class="metric-card">
        <div :class="['metric-icon', item.color]">
          <i :class="item.icon"></i>
        </div>
        <div>
          <div class="metric-label">{{ item.label }}</div>
          <div class="metric-value">{{ item.value }}</div>
        </div>
      </div>
    </div>

    <div class="main-grid">
      <section class="panel recent-panel">
        <h3>最近发布内容</h3>
        <div v-loading="loading" class="recent-list">
          <div v-for="item in recentContents" :key="item.id" class="recent-item">
            <div class="recent-title">{{ item.title }}</div>
            <div class="recent-meta">{{ categoryLabel(item.category) }} · {{ formatDate(item.published_at) }}</div>
          </div>
          <el-empty v-if="!loading && recentContents.length === 0" :image-size="96" description="暂无发布内容" />
        </div>
      </section>

      <section class="panel actions-panel">
        <h3>快捷操作</h3>
        <button class="quick-action" type="button" @click="go('/centralbank/content')">
          <i class="el-icon-document-add"></i>
          <span>发布内容</span>
        </button>
        <button class="quick-action" type="button" @click="go('/centralbank/product')">
          <i class="el-icon-circle-plus-outline"></i>
          <span>新增金融产品</span>
        </button>
        <button class="quick-action" type="button" @click="go('/centralbank/product/import')">
          <i class="el-icon-document"></i>
          <span>导入产品表格</span>
        </button>
      </section>
    </div>
  </div>
</template>

<script>
import { getDashboardSummary } from '@/api/centralbank/dashboard'

export default {
  name: 'Index',
  data() {
    return {
      loading: false,
      summary: {
        published_content_count: 0,
        product_count: 0,
        account_count: 0,
        today_operation_count: 0,
        recent_contents: []
      },
      categories: {
        POLICY_PROMOTION: '政策宣传',
        SERVICE_GUIDE: '服务指引',
        RURAL_REVITALIZATION: '乡村振兴'
      }
    }
  },
  computed: {
    recentContents() {
      return this.summary.recent_contents || []
    },
    metrics() {
      return [
        {
          label: '已发布内容',
          value: this.summary.published_content_count,
          icon: 'el-icon-document-checked',
          color: 'blue'
        },
        {
          label: '金融产品',
          value: this.summary.product_count,
          icon: 'el-icon-bank-card',
          color: 'green'
        },
        {
          label: '后台账号',
          value: this.summary.account_count,
          icon: 'el-icon-user',
          color: 'purple'
        },
        {
          label: '今日操作',
          value: this.summary.today_operation_count,
          icon: 'el-icon-data-line',
          color: 'orange'
        }
      ]
    }
  },
  created() {
    this.loadSummary()
  },
  methods: {
    loadSummary() {
      this.loading = true
      getDashboardSummary().then(response => {
        this.summary = response.data
      }).finally(() => {
        this.loading = false
      })
    },
    categoryLabel(value) {
      return this.categories[value] || value
    },
    formatDate(value) {
      if (!value) {
        return ''
      }
      return value.slice(0, 10)
    },
    go(path) {
      this.$router.push(path)
    }
  }
}
</script>

<style scoped>
.dashboard-page {
  max-width: 1280px;
}

.page-head {
  margin-bottom: 20px;
}

.page-head h2 {
  margin: 22px 0 8px;
  font-size: 28px;
  font-weight: 600;
  color: #1f2d3d;
}

.page-head p {
  margin: 0;
  color: #6b7280;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 22px;
}

.metric-card,
.panel {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fff;
}

.metric-card {
  display: flex;
  align-items: center;
  min-height: 120px;
  padding: 24px;
  gap: 18px;
}

.metric-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 8px;
  color: #fff;
  font-size: 24px;
}

.metric-icon.blue {
  background: #1f5bd9;
}

.metric-icon.green {
  background: #14a66b;
}

.metric-icon.purple {
  background: #7c4df2;
}

.metric-icon.orange {
  background: #f59e0b;
}

.metric-label {
  margin-bottom: 8px;
  color: #6b7280;
  font-weight: 600;
}

.metric-value {
  color: #111827;
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.8fr) minmax(300px, 0.8fr);
  gap: 22px;
}

.panel {
  padding: 22px 24px;
}

.panel h3 {
  margin: 0 0 18px;
  font-size: 18px;
  font-weight: 700;
  color: #1f2d3d;
}

.recent-list {
  min-height: 250px;
}

.recent-item {
  padding: 18px 0;
  border-top: 1px solid #edf0f5;
}

.recent-item:first-child {
  border-top: 0;
  padding-top: 0;
}

.recent-title {
  margin-bottom: 8px;
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 700;
}

.recent-meta {
  color: #7b8794;
  font-weight: 600;
}

.actions-panel {
  align-self: start;
}

.quick-action {
  display: flex;
  align-items: center;
  width: 100%;
  min-height: 56px;
  padding: 0 16px;
  margin-top: 12px;
  border: 0;
  border-radius: 6px;
  background: #f5f7fb;
  color: #1f2d3d;
  font: inherit;
  font-weight: 700;
  text-align: left;
  cursor: pointer;
}

.quick-action:first-of-type {
  margin-top: 0;
}

.quick-action i {
  margin-right: 12px;
  color: #1f5bd9;
  font-size: 20px;
}

.quick-action:hover {
  background: #edf3ff;
}

@media (max-width: 1100px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(180px, 1fr));
  }

  .main-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
