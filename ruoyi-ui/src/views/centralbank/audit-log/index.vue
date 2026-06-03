<template>
  <div class="app-container audit-log-page">
    <div class="page-head">
      <el-breadcrumb separator=">">
        <el-breadcrumb-item>后台管理</el-breadcrumb-item>
        <el-breadcrumb-item>操作日志</el-breadcrumb-item>
      </el-breadcrumb>
      <h2>操作日志</h2>
      <p>查看后台账号的关键操作记录</p>
    </div>

    <div class="query-bar">
      <el-input v-model="queryParams.operator_keyword" placeholder="输入操作人" clearable @keyup.enter.native="handleQuery" />
      <el-select v-model="queryParams.operation_type" placeholder="全部类型" clearable>
        <el-option v-for="item in operationTypes" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-date-picker
        v-model="operatedRange"
        type="daterange"
        value-format="yyyy-MM-dd"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
      />
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="auditLogs" class="audit-table">
      <el-table-column label="操作时间" prop="operated_at" min-width="180">
        <template slot-scope="scope">{{ formatTime(scope.row.operated_at) }}</template>
      </el-table-column>
      <el-table-column label="操作人" prop="operator_name" min-width="150" />
      <el-table-column label="类型" prop="operation_type" min-width="130">
        <template slot-scope="scope">{{ operationTypeLabel(scope.row.operation_type) }}</template>
      </el-table-column>
      <el-table-column label="对象" prop="object_name" min-width="260" />
      <el-table-column label="操作说明" prop="description" min-width="280">
        <template slot-scope="scope">
          <span class="description">{{ scope.row.description }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.page"
      :limit.sync="queryParams.page_size"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listAuditLog } from '@/api/centralbank/auditLog'

export default {
  name: 'CentralBankAuditLog',
  data() {
    return {
      loading: false,
      total: 0,
      auditLogs: [],
      operatedRange: [],
      operationTypes: [
        { value: 'CREATE', label: '新增' },
        { value: 'UPDATE', label: '编辑' },
        { value: 'DELETE', label: '删除' },
        { value: 'IMPORT', label: '导入' },
        { value: 'ACCOUNT', label: '账号' },
        { value: 'PASSWORD', label: '密码' }
      ],
      queryParams: {
        page: 1,
        page_size: 20,
        operator_keyword: undefined,
        operation_type: undefined,
        operated_from: undefined,
        operated_to: undefined
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      this.syncRange()
      listAuditLog(this.queryParams).then(response => {
        this.auditLogs = response.data.items
        this.total = response.data.total
      }).finally(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.page = 1
      this.getList()
    },
    resetQuery() {
      this.operatedRange = []
      this.queryParams = {
        page: 1,
        page_size: 20,
        operator_keyword: undefined,
        operation_type: undefined,
        operated_from: undefined,
        operated_to: undefined
      }
      this.getList()
    },
    syncRange() {
      this.queryParams.operated_from = this.operatedRange && this.operatedRange.length === 2 ? this.operatedRange[0] : undefined
      this.queryParams.operated_to = this.operatedRange && this.operatedRange.length === 2 ? this.operatedRange[1] : undefined
    },
    operationTypeLabel(value) {
      const item = this.operationTypes.find(type => type.value === value)
      return item ? item.label : value
    },
    formatTime(value) {
      if (!value) {
        return ''
      }
      return value.replace('T', ' ').replace('+08:00', '')
    }
  }
}
</script>

<style scoped>
.audit-log-page {
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

.query-bar {
  display: grid;
  grid-template-columns: minmax(210px, 1fr) minmax(180px, 0.8fr) minmax(300px, 1.2fr) 88px 88px;
  gap: 12px;
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fff;
}

.query-bar .el-date-editor {
  width: 100%;
}

.audit-table {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}

.description {
  color: #1f5fcc;
  font-weight: 600;
}

@media (max-width: 980px) {
  .query-bar {
    grid-template-columns: 1fr;
  }
}
</style>
