<template>
  <div class="app-container product-import">
    <div class="page-head">
      <el-breadcrumb separator=">">
        <el-breadcrumb-item>后台管理</el-breadcrumb-item>
        <el-breadcrumb-item>Excel 导入</el-breadcrumb-item>
      </el-breadcrumb>
      <h2>Excel 导入</h2>
      <p>批量校验并导入金融产品数据</p>
    </div>

    <div class="steps">
      <div v-for="step in steps" :key="step.index" class="step" :class="{ active: activeStep === step.index }">
        <span>{{ step.index }}</span>
        <em>{{ step.label }}</em>
      </div>
    </div>

    <div class="upload-panel" v-if="!result">
      <el-upload
        :show-file-list="false"
        :before-upload="handleFile"
        accept=".xlsx"
        action=""
      >
        <el-button type="primary" icon="el-icon-upload2" :loading="uploading">上传 xlsx 文件</el-button>
      </el-upload>
    </div>

    <div v-if="result" class="summary-grid">
      <div class="summary-item">
        <span>总数据</span>
        <strong>{{ result.total_count }}</strong>
        <em>条</em>
      </div>
      <div class="summary-item">
        <span>校验通过</span>
        <strong>{{ result.valid_count }}</strong>
        <em>条</em>
      </div>
      <div class="summary-item">
        <span>需修正</span>
        <strong>{{ result.invalid_count }}</strong>
        <em>条</em>
      </div>
    </div>

    <el-table v-if="result" :data="result.errors" class="error-table" border>
      <el-table-column label="行号" prop="row_number" width="100" />
      <el-table-column label="问题字段" prop="field" width="160" />
      <el-table-column label="原始内容" prop="raw_value" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="问题说明" prop="message" min-width="220" :show-overflow-tooltip="true" />
      <el-table-column label="处理建议" min-width="180">
        <template slot-scope="scope">
          {{ suggestionFor(scope.row) }}
        </template>
      </el-table-column>
    </el-table>

    <div v-if="commitResult" class="commit-result">
      已导入 {{ commitResult.imported_count }} 条，跳过 {{ commitResult.skipped_count }} 条
    </div>

    <div class="actions">
      <el-button @click="goBack">返回产品列表</el-button>
      <el-button
        v-if="result"
        type="primary"
        :loading="committing"
        :disabled="committed"
        @click="handleCommit"
      >确认导入</el-button>
    </div>
  </div>
</template>

<script>
import { commitProductImport, validateProductImport } from '@/api/centralbank/product'

export default {
  name: 'CentralBankProductImport',
  data() {
    return {
      uploading: false,
      committing: false,
      committed: false,
      result: null,
      commitResult: null,
      steps: [
        { index: 1, label: '上传文件' },
        { index: 2, label: '数据校验' },
        { index: 3, label: '导入结果' }
      ]
    }
  },
  computed: {
    activeStep() {
      if (this.commitResult) {
        return 3
      }
      if (this.uploading) {
        return 2
      }
      return this.result ? 3 : 1
    }
  },
  methods: {
    handleFile(file) {
      const filename = file && file.name ? file.name.toLowerCase() : ''
      if (!filename.endsWith('.xlsx')) {
        this.$modal.msgError('仅支持 xlsx 文件')
        return false
      }
      const formData = new FormData()
      formData.append('file', file)
      this.uploading = true
      this.committed = false
      this.commitResult = null
      validateProductImport(formData).then(response => {
        this.result = response.data
      }).finally(() => {
        this.uploading = false
      })
      return false
    },
    handleCommit() {
      if (!this.result || !this.result.import_token) {
        return
      }
      this.committing = true
      commitProductImport({ import_token: this.result.import_token }).then(response => {
        this.commitResult = response.data
        this.committed = true
        this.$modal.msgSuccess('导入成功')
      }).finally(() => {
        this.committing = false
      })
    },
    suggestionFor(row) {
      if (row.field === '银行机构') {
        return '检查固定银行列表'
      }
      if (row.raw_value === '') {
        return '补充后重新上传'
      }
      return '修正后重新上传'
    },
    goBack() {
      this.$router.push('/centralbank/product')
    }
  }
}
</script>

<style scoped>
.product-import {
  max-width: 1160px;
}

.page-head {
  margin-bottom: 22px;
}

.page-head h2 {
  margin: 18px 0 8px;
  font-size: 28px;
  font-weight: 500;
  color: #1f2d3d;
}

.page-head p {
  margin: 0;
  color: #6b7280;
}

.steps {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 22px;
}

.step {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #8c939d;
}

.step span {
  width: 28px;
  height: 28px;
  line-height: 28px;
  border-radius: 50%;
  text-align: center;
  background: #edf2f7;
  color: #697386;
  font-style: normal;
}

.step em {
  font-style: normal;
}

.step.active {
  color: #2f6fed;
}

.step.active span {
  background: #2f6fed;
  color: #fff;
}

.upload-panel {
  padding: 28px 0;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(160px, 1fr));
  gap: 16px;
  margin-bottom: 22px;
}

.summary-item {
  min-height: 88px;
  padding: 18px 20px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fff;
}

.summary-item span,
.summary-item em {
  color: #6b7280;
  font-style: normal;
}

.summary-item strong {
  display: inline-block;
  margin: 8px 4px 0 0;
  font-size: 30px;
  font-weight: 600;
  color: #1f2d3d;
}

.error-table {
  margin-bottom: 18px;
}

.commit-result {
  margin-bottom: 18px;
  color: #1f7a4d;
}

.actions {
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .steps {
    align-items: flex-start;
    flex-direction: column;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
