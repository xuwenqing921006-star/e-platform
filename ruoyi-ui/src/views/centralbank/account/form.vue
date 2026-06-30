<template>
  <div class="app-container account-form">
    <div class="page-head">
      <el-breadcrumb separator=">">
        <el-breadcrumb-item>后台管理</el-breadcrumb-item>
        <el-breadcrumb-item>账号管理</el-breadcrumb-item>
        <el-breadcrumb-item>{{ pageTitle }}</el-breadcrumb-item>
      </el-breadcrumb>
      <div class="title-row">
        <div>
          <h2>{{ pageTitle }}</h2>
          <p>{{ pageSubtitle }}</p>
        </div>
        <div class="actions">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">保存账号</el-button>
        </div>
      </div>
    </div>

    <div class="form-panel">
      <div class="panel-title">账号基础信息</div>
      <el-form ref="form" :model="form" :rules="rules" label-width="96px">
        <div class="form-grid">
          <el-form-item label="登录账号" prop="username">
            <el-input v-model="form.username" :disabled="isEdit" maxlength="40" placeholder="请输入登录账号" />
          </el-form-item>
          <el-form-item label="姓名" prop="display_name">
            <el-input v-model="form.display_name" maxlength="40" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="账号角色" prop="role">
            <el-select v-model="form.role" placeholder="请选择账号角色" @change="handleRoleChange">
              <el-option v-for="item in roles" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="所属机构" prop="office_code">
            <el-select
              v-model="form.office_code"
              :disabled="form.role === 'ADMIN'"
              placeholder="请选择所属机构"
              clearable
              filterable
            >
              <el-option v-for="item in offices" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="!isEdit" label="初始密码" prop="initial_password">
            <el-input v-model="form.initial_password" type="password" maxlength="64" placeholder="请输入初始密码" show-password />
          </el-form-item>
          <el-form-item v-if="!isEdit" label="确认密码" prop="confirm_password">
            <el-input v-model="form.confirm_password" type="password" maxlength="64" placeholder="请再次输入密码" show-password />
          </el-form-item>
          <el-form-item label="账号状态" prop="enabled">
            <el-switch v-model="form.enabled" active-text="启用" inactive-text="停用" />
          </el-form-item>
        </div>
      </el-form>
      <div class="form-tip">普通账号仅可维护所属机构范围内的数据；管理员可查看全部机构信息。</div>
    </div>
  </div>
</template>

<script>
import { addAccount, getAccount, updateAccount } from '@/api/centralbank/account'
import { listOptions } from '@/api/centralbank/product'

export default {
  name: 'CentralBankAccountForm',
  data() {
    const validateOffice = (rule, value, callback) => {
      if (this.form.role === 'OFFICE_USER' && !value) {
        callback(new Error('请选择所属机构'))
        return
      }
      callback()
    }
    const validateConfirmPassword = (rule, value, callback) => {
      if (!this.isEdit && value !== this.form.initial_password) {
        callback(new Error('两次输入密码不一致'))
        return
      }
      callback()
    }
    return {
      saving: false,
      offices: [],
      roles: [
        { value: 'ADMIN', label: '管理员' },
        { value: 'OFFICE_USER', label: '普通账号' }
      ],
      form: {
        username: '',
        display_name: '',
        role: 'OFFICE_USER',
        office_code: undefined,
        initial_password: '',
        confirm_password: '',
        enabled: true
      },
      rules: {
        username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
        display_name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        role: [{ required: true, message: '请选择账号角色', trigger: 'change' }],
        office_code: [{ validator: validateOffice, trigger: 'change' }],
        initial_password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }],
        confirm_password: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    isEdit() {
      return Boolean(this.$route.params.id)
    },
    pageTitle() {
      return this.isEdit ? '编辑账号' : '新增账号'
    },
    pageSubtitle() {
      return this.isEdit ? '修改后台账号并分配机构权限' : '创建后台账号并分配机构权限'
    }
  },
  created() {
    this.loadOptions()
    if (this.isEdit) {
      this.loadDetail()
    }
  },
  methods: {
    loadOptions() {
      listOptions().then(response => {
        this.offices = response.data.offices || []
      })
    },
    loadDetail() {
      getAccount(this.$route.params.id).then(response => {
        const data = response.data
        this.form.username = data.username
        this.form.display_name = data.display_name
        this.form.role = data.role
        this.form.office_code = data.office_code || undefined
        this.form.enabled = data.enabled
      })
    },
    handleRoleChange(role) {
      if (role === 'ADMIN') {
        this.form.office_code = undefined
      }
      this.$nextTick(() => {
        this.$refs.form && this.$refs.form.validateField('office_code')
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        this.saving = true
        const payload = {
          display_name: this.form.display_name,
          role: this.form.role,
          office_code: this.form.role === 'ADMIN' ? null : this.form.office_code,
          enabled: this.form.enabled
        }
        const request = this.isEdit
          ? updateAccount(this.$route.params.id, payload)
          : addAccount({
              ...payload,
              username: this.form.username,
              initial_password: this.form.initial_password
            })
        request.then(() => {
          this.$modal.msgSuccess('保存成功')
          return this.refreshCurrentUserInfo()
        }).then(() => {
          this.goBack()
        }).finally(() => {
          this.saving = false
        })
      })
    },
    refreshCurrentUserInfo() {
      if (!this.isEdit || Number(this.$route.params.id) !== Number(this.$store.getters.id)) {
        return Promise.resolve()
      }
      return this.$store.dispatch('GetInfo')
    },
    goBack() {
      this.$router.push('/centralbank/account')
    }
  }
}
</script>

<style scoped>
.account-form {
  max-width: 1080px;
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

.actions {
  display: flex;
  gap: 10px;
}

.form-panel {
  padding: 22px 24px 18px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fff;
}

.panel-title {
  margin-bottom: 24px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2d3d;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(260px, 1fr));
  column-gap: 42px;
}

.form-grid .el-select {
  width: 100%;
}

.form-tip {
  margin-top: 4px;
  padding-top: 16px;
  border-top: 1px solid #edf0f5;
  color: #6b7280;
}

@media (max-width: 860px) {
  .title-row {
    flex-direction: column;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
