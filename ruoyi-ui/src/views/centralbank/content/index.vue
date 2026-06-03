<template>
  <div class="app-container content-admin">
    <el-form ref="queryForm" :model="queryParams" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="标题" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          placeholder="请输入标题"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="请选择分类" clearable>
          <el-option
            v-for="item in contentCategories"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="办公室" prop="office_code">
        <el-select v-model="queryParams.office_code" placeholder="请选择办公室" clearable filterable>
          <el-option
            v-for="item in offices"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="发布时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="yyyy-MM-dd"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['centralbank:content:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="contentList">
      <el-table-column label="标题" prop="title" min-width="260" :show-overflow-tooltip="true" />
      <el-table-column label="分类" prop="category" width="130">
        <template slot-scope="scope">
          <span>{{ categoryLabel(scope.row.category) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="发布办公室" prop="office_name" width="180" />
      <el-table-column label="发布时间" prop="published_at" width="190" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['centralbank:content:query']"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['centralbank:content:edit']"
          >编辑</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['centralbank:content:remove']"
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

    <el-dialog :title="title" :visible.sync="open" width="920px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="92px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入标题" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择分类">
                <el-option
                  v-for="item in contentCategories"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="办公室" prop="office_code">
              <el-select v-model="form.office_code" placeholder="请选择办公室" filterable>
                <el-option
                  v-for="item in offices"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="正文" prop="rich_text_html">
              <editor v-model="form.rich_text_html" :min-height="240" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="附件">
              <el-upload
                :action="uploadUrl"
                :headers="uploadHeaders"
                name="file"
                :show-file-list="false"
                :before-upload="beforeAttachmentUpload"
                :on-success="handleAttachmentSuccess"
                :on-error="handleAttachmentError"
              >
                <el-button size="mini" icon="el-icon-upload2">上传</el-button>
              </el-upload>
              <div class="attachment-list">
                <div v-for="file in form.attachments" :key="file.id" class="attachment-row">
                  <span class="attachment-name">{{ file.file_name }}</span>
                  <span class="attachment-meta">{{ file.file_type }} / {{ formatFileSize(file.file_size) }}</span>
                  <el-button type="text" icon="el-icon-delete" @click="handleAttachmentRemove(file)">删除</el-button>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="内容详情" :visible.sync="viewOpen" width="860px" append-to-body>
      <div v-if="detail" class="content-detail">
        <h3>{{ detail.title }}</h3>
        <div class="detail-meta">
          <span>{{ categoryLabel(detail.category) }}</span>
          <span>{{ detail.office_name }}</span>
          <span>{{ detail.published_at }}</span>
        </div>
        <div class="rich-text" v-html="detail.rich_text_html"></div>
        <div class="attachment-list detail-attachments">
          <div v-for="file in detail.attachments" :key="file.id" class="attachment-row">
            <span class="attachment-name">{{ file.file_name }}</span>
            <a :href="file.download_url" target="_blank" rel="noopener">下载</a>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="viewOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'
import {
  addContent,
  deleteAttachment,
  delContent,
  getContent,
  listContent,
  listOptions,
  updateContent
} from '@/api/centralbank/content'

export default {
  name: 'CentralBankContent',
  data() {
    return {
      loading: false,
      showSearch: true,
      total: 0,
      contentList: [],
      contentCategories: [],
      offices: [],
      dateRange: [],
      open: false,
      viewOpen: false,
      title: '',
      detail: null,
      queryParams: {
        page: 1,
        page_size: 20,
        keyword: undefined,
        category: undefined,
        office_code: undefined
      },
      form: this.emptyForm(),
      rules: {
        title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
        category: [{ required: true, message: '分类不能为空', trigger: 'change' }],
        office_code: [{ required: true, message: '办公室不能为空', trigger: 'change' }],
        rich_text_html: [{ required: true, message: '正文不能为空', trigger: 'blur' }]
      }
    }
  },
  computed: {
    uploadUrl() {
      return process.env.VUE_APP_BASE_API + '/api/admin/attachments'
    },
    uploadHeaders() {
      return {
        Authorization: 'Bearer ' + getToken()
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
        title: '',
        category: undefined,
        office_code: undefined,
        rich_text_html: '',
        attachments: []
      }
    },
    loadOptions() {
      listOptions().then(response => {
        this.contentCategories = response.data.content_categories || []
        this.offices = response.data.offices || []
      })
    },
    getList() {
      this.loading = true
      const params = {
        ...this.queryParams,
        published_from: this.dateRange && this.dateRange.length ? this.dateRange[0] : undefined,
        published_to: this.dateRange && this.dateRange.length ? this.dateRange[1] : undefined
      }
      listContent(params).then(response => {
        this.contentList = response.data.items
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
      this.dateRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增内容'
    },
    handleUpdate(row) {
      this.reset()
      getContent(row.id).then(response => {
        const data = response.data
        this.form = {
          id: data.id,
          title: data.title,
          category: data.category,
          office_code: data.office_code,
          rich_text_html: data.rich_text_html,
          attachments: data.attachments || []
        }
        this.open = true
        this.title = '编辑内容'
      })
    },
    handleView(row) {
      getContent(row.id).then(response => {
        this.detail = response.data
        this.viewOpen = true
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        const payload = {
          title: this.form.title,
          category: this.form.category,
          office_code: this.form.office_code,
          rich_text_html: this.form.rich_text_html,
          attachment_ids: this.form.attachments.map(file => file.id)
        }
        const request = this.form.id ? updateContent(this.form.id, payload) : addContent(payload)
        request.then(() => {
          this.$modal.msgSuccess(this.form.id ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除标题为"' + row.title + '"的数据项？').then(() => {
        return delContent(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    beforeAttachmentUpload(file) {
      if (this.form.attachments.length >= 3) {
        this.$message.error('每篇内容最多上传 3 个附件')
        return false
      }
      const allowed = [
        'application/pdf',
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'application/vnd.ms-excel',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      ]
      if (!allowed.includes(file.type)) {
        this.$message.error('仅支持 PDF、Word 或 Excel 文件')
        return false
      }
      if (file.size / 1024 / 1024 > 20) {
        this.$message.error('单个附件不能超过 20MB')
        return false
      }
      return true
    },
    handleAttachmentSuccess(response) {
      if (response.code !== 200) {
        this.$message.error(response.message || '附件上传失败')
        return
      }
      this.form.attachments.push(response.data)
    },
    handleAttachmentError() {
      this.$message.error('附件上传失败')
    },
    handleAttachmentRemove(file) {
      deleteAttachment(file.id).then(() => {
        this.form.attachments = this.form.attachments.filter(item => item.id !== file.id)
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = this.emptyForm()
      this.$nextTick(() => {
        if (this.$refs.form) {
          this.resetForm('form')
        }
      })
    },
    categoryLabel(value) {
      const item = this.contentCategories.find(option => option.value === value)
      return item ? item.label : value
    },
    formatFileSize(size) {
      if (!size) {
        return '0 KB'
      }
      if (size < 1024 * 1024) {
        return Math.ceil(size / 1024) + ' KB'
      }
      return (size / 1024 / 1024).toFixed(1) + ' MB'
    }
  }
}
</script>

<style scoped>
.content-admin .el-select {
  width: 220px;
}

.attachment-list {
  margin-top: 8px;
}

.attachment-row {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 32px;
  border-bottom: 1px solid #ebeef5;
}

.attachment-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attachment-meta {
  width: 140px;
  color: #909399;
  font-size: 12px;
}

.content-detail h3 {
  margin: 0 0 12px;
  font-size: 18px;
  font-weight: 600;
}

.detail-meta {
  display: flex;
  gap: 16px;
  color: #909399;
  margin-bottom: 16px;
}

.rich-text {
  min-height: 160px;
  padding: 12px 0;
  border-top: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
}

.detail-attachments {
  margin-top: 12px;
}
</style>
