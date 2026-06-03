<template>
  <div class="app-container account-admin">
    <div class="page-head">
      <el-breadcrumb separator=">">
        <el-breadcrumb-item>后台管理</el-breadcrumb-item>
        <el-breadcrumb-item>账号管理</el-breadcrumb-item>
      </el-breadcrumb>
      <div class="title-row">
        <div>
          <h2>账号管理</h2>
          <p>维护后台登录账号及所属机构权限</p>
        </div>
        <el-button type="primary" @click="handleAdd" v-hasPermi="['centralbank:account:add']">新增账号</el-button>
      </div>
    </div>

    <div class="query-bar">
      <el-input v-model="queryParams.keyword" placeholder="输入姓名或账号" clearable @keyup.enter.native="handleQuery" />
      <el-select v-model="queryParams.office_code" placeholder="全部机构" clearable filterable>
        <el-option v-for="item in offices" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="queryParams.role" placeholder="全部角色" clearable>
        <el-option v-for="item in roles" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="accountList" class="account-table">
      <el-table-column label="登录账号" prop="username" min-width="160" />
      <el-table-column label="姓名" prop="display_name" min-width="140" />
      <el-table-column label="角色" prop="role" min-width="140">
        <template slot-scope="scope">{{ roleLabel(scope.row.role) }}</template>
      </el-table-column>
      <el-table-column label="所属机构" prop="office_name" min-width="220">
        <template slot-scope="scope">{{ scope.row.office_name || '全部机构' }}</template>
      </el-table-column>
      <el-table-column label="状态" prop="enabled" width="120">
        <template slot-scope="scope">{{ scope.row.enabled ? '启用' : '停用' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template slot-scope="scope">
          <el-button type="text" @click="handleEdit(scope.row)" v-hasPermi="['centralbank:account:edit']">编辑</el-button>
          <el-button type="text" @click="handleReset(scope.row)" v-hasPermi="['centralbank:account:reset']">重置密码</el-button>
          <el-button type="text" @click="handleDelete(scope.row)" v-hasPermi="['centralbank:account:remove']">删除</el-button>
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
import { delAccount, listAccount, resetAccountPassword } from '@/api/centralbank/account'
import { listOptions } from '@/api/centralbank/product'

export default {
  name: 'CentralBankAccount',
  data() {
    return {
      loading: false,
      total: 0,
      accountList: [],
      offices: [],
      roles: [
        { value: 'ADMIN', label: '管理员' },
        { value: 'OFFICE_USER', label: '普通账号' }
      ],
      queryParams: {
        page: 1,
        page_size: 20,
        keyword: undefined,
        office_code: undefined,
        role: undefined
      }
    }
  },
  created() {
    this.loadOptions()
    this.getList()
  },
  methods: {
    loadOptions() {
      listOptions().then(response => {
        this.offices = response.data.offices || []
      })
    },
    getList() {
      this.loading = true
      listAccount(this.queryParams).then(response => {
        this.accountList = response.data.items
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
      this.queryParams = {
        page: 1,
        page_size: 20,
        keyword: undefined,
        office_code: undefined,
        role: undefined
      }
      this.getList()
    },
    handleAdd() {
      this.$router.push('/centralbank/account/new')
    },
    handleEdit(row) {
      this.$router.push('/centralbank/account/edit/' + row.id)
    },
    handleReset(row) {
      this.$prompt('请输入新密码', '重置密码', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        inputType: 'password',
        inputPattern: /.+/,
        inputErrorMessage: '新密码不能为空'
      }).then(({ value }) => {
        return resetAccountPassword(row.id, { new_password: value })
      }).then(() => {
        this.$modal.msgSuccess('重置成功')
      }).catch(() => {})
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除账号"' + row.username + '"？').then(() => {
        return delAccount(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    roleLabel(value) {
      const item = this.roles.find(role => role.value === value)
      return item ? item.label : value
    }
  }
}
</script>

<style scoped>
.account-admin {
  max-width: 1280px;
}

.page-head {
  margin-bottom: 20px;
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-top: 22px;
}

.title-row h2 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 600;
  color: #1f2d3d;
}

.title-row p {
  margin: 0;
  color: #6b7280;
}

.query-bar {
  display: grid;
  grid-template-columns: minmax(220px, 1.3fr) minmax(190px, 1fr) minmax(170px, 0.8fr) 88px 88px;
  gap: 12px;
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fff;
}

.account-table {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}

@media (max-width: 920px) {
  .title-row {
    flex-direction: column;
  }

  .query-bar {
    grid-template-columns: 1fr;
  }
}
</style>
