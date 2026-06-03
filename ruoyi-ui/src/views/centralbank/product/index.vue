<template>
  <div class="app-container product-admin">
    <el-form ref="queryForm" :model="queryParams" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="产品名称" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          placeholder="请输入产品名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="银行机构" prop="bank_code">
        <el-select v-model="queryParams.bank_code" placeholder="请选择银行" clearable filterable>
          <el-option
            v-for="item in banks"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="产品类型" prop="product_type">
        <el-select v-model="queryParams.product_type" placeholder="请选择类型" clearable>
          <el-option
            v-for="item in productTypes"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleDownloadTemplate"
          v-hasPermi="['centralbank:product:template']"
        >下载模板</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-upload2"
          size="mini"
          @click="handleImport"
          v-hasPermi="['centralbank:product:import']"
        >Excel 导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['centralbank:product:add']"
        >新增产品</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="productList">
      <el-table-column label="产品名称" prop="product_name" min-width="240" :show-overflow-tooltip="true" />
      <el-table-column label="银行机构" prop="bank_name" width="200" />
      <el-table-column label="产品类型" prop="product_type" width="130">
        <template slot-scope="scope">
          <span>{{ productTypeLabel(scope.row.product_type) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" prop="updated_at" width="190" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['centralbank:product:query']"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['centralbank:product:edit']"
          >编辑</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['centralbank:product:remove']"
          >删除</el-button>
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

    <el-dialog :title="title" :visible.sync="open" width="880px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="108px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="银行机构" prop="bank_code">
              <el-select v-model="form.bank_code" placeholder="请选择银行机构" filterable :disabled="viewMode">
                <el-option
                  v-for="item in banks"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品类型" prop="product_type">
              <el-select v-model="form.product_type" placeholder="请选择产品类型" :disabled="viewMode">
                <el-option
                  v-for="item in productTypes"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="产品名称" prop="product_name">
              <el-input v-model="form.product_name" placeholder="请输入产品名称" maxlength="150" show-word-limit :disabled="viewMode" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="准入条件" prop="admission_conditions">
              <el-input
                v-model="form.admission_conditions"
                type="textarea"
                :rows="4"
                placeholder="请输入准入条件"
                :disabled="viewMode"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="产品介绍" prop="product_intro">
              <el-input
                v-model="form.product_intro"
                type="textarea"
                :rows="4"
                placeholder="请输入产品介绍"
                :disabled="viewMode"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务经办人" prop="business_manager">
              <el-input v-model="form.business_manager" placeholder="请输入业务经办人" maxlength="80" :disabled="viewMode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系方式" prop="contact_info">
              <el-input v-model="form.contact_info" placeholder="请输入联系方式" maxlength="80" :disabled="viewMode" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button v-if="!viewMode" type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">{{ viewMode ? '关 闭' : '取 消' }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { saveAs } from 'file-saver'
import {
  addProduct,
  delProduct,
  downloadProductTemplate,
  getProduct,
  listOptions,
  listProduct,
  updateProduct
} from '@/api/centralbank/product'

export default {
  name: 'CentralBankProduct',
  data() {
    return {
      loading: false,
      showSearch: true,
      total: 0,
      productList: [],
      productTypes: [],
      banks: [],
      open: false,
      viewMode: false,
      title: '',
      queryParams: {
        page: 1,
        page_size: 20,
        keyword: undefined,
        bank_code: undefined,
        product_type: undefined
      },
      form: this.emptyForm(),
      rules: {
        bank_code: [{ required: true, message: '银行机构不能为空', trigger: 'change' }],
        product_name: [{ required: true, message: '产品名称不能为空', trigger: 'blur' }],
        product_type: [{ required: true, message: '产品类型不能为空', trigger: 'change' }],
        admission_conditions: [{ required: true, message: '准入条件不能为空', trigger: 'blur' }],
        product_intro: [{ required: true, message: '产品介绍不能为空', trigger: 'blur' }],
        business_manager: [{ required: true, message: '业务经办人不能为空', trigger: 'blur' }],
        contact_info: [{ required: true, message: '联系方式不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadOptions()
    this.getList()
  },
  methods: {
    emptyForm() {
      return {
        id: undefined,
        bank_code: undefined,
        bank_name: '',
        product_name: '',
        product_type: undefined,
        admission_conditions: '',
        product_intro: '',
        business_manager: '',
        contact_info: ''
      }
    },
    loadOptions() {
      listOptions().then(response => {
        this.productTypes = response.data.product_types || []
        this.banks = response.data.banks || []
      })
    },
    getList() {
      this.loading = true
      listProduct(this.queryParams).then(response => {
        this.productList = response.data.items
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
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleAdd() {
      this.reset()
      this.viewMode = false
      this.open = true
      this.title = '新增金融产品'
    },
    handleUpdate(row) {
      this.loadDetail(row.id, false, '编辑金融产品')
    },
    handleView(row) {
      this.loadDetail(row.id, true, '金融产品详情')
    },
    loadDetail(id, viewMode, title) {
      this.reset()
      getProduct(id).then(response => {
        this.form = {
          ...this.emptyForm(),
          ...response.data
        }
        this.viewMode = viewMode
        this.open = true
        this.title = title
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        const payload = {
          bank_code: this.form.bank_code,
          product_name: this.form.product_name,
          product_type: this.form.product_type,
          admission_conditions: this.form.admission_conditions,
          product_intro: this.form.product_intro,
          business_manager: this.form.business_manager,
          contact_info: this.form.contact_info
        }
        const request = this.form.id ? updateProduct(this.form.id, payload) : addProduct(payload)
        request.then(() => {
          this.$modal.msgSuccess(this.form.id ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除产品"' + row.product_name + '"？').then(() => {
        return delProduct(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleDownloadTemplate() {
      downloadProductTemplate().then(data => {
        const blob = new Blob([data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        })
        saveAs(blob, 'financial-product-import-template.xlsx')
      })
    },
    handleImport() {
      this.$router.push('/centralbank/product/import')
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = this.emptyForm()
      this.viewMode = false
      this.$nextTick(() => {
        if (this.$refs.form) {
          this.resetForm('form')
        }
      })
    },
    productTypeLabel(value) {
      const item = this.productTypes.find(option => option.value === value)
      return item ? item.label : value
    }
  }
}
</script>

<style scoped>
.product-admin .el-select {
  width: 220px;
}

.product-admin .el-textarea {
  max-width: 100%;
}
</style>
