# 央行 E 平台接口契约

> 前端 Mock 与后端实现的唯一对齐依据。任何字段变更必须同步更新本文档。

## 1. 通用约定

### 1.1 Base URL

```text
/api
```

健康检查例外：

```text
/health
```

### 1.2 认证

后台接口使用 Bearer Token：

```http
Authorization: Bearer <access_token>
```

公开 H5 接口不需要登录。

### 1.3 统一响应

成功：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

错误：

```json
{
  "code": 400,
  "message": "参数错误",
  "data": null
}
```

分页：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [],
    "total": 0,
    "page": 1,
    "page_size": 20
  }
}
```

### 1.4 枚举

| 枚举 | 值 |
| --- | --- |
| 内容分类 `content_category` | `SERVICE_GUIDE`、`POLICY_PROMOTION` |
| 产品类型 `product_type` | `AGRICULTURAL`、`SMALL_MICRO` |
| 县域编码 `county_code` | `ZHAOZHOU`、`ZHAOYUAN`、`LINDIAN`、`DUMENG` |
| 账号角色 `role` | `ADMIN`、`OFFICE_USER` |
| 附件类型 `file_type` | `PDF`、`WORD`、`EXCEL` |
| 操作类型 `operation_type` | `CREATE`、`UPDATE`、`DELETE`、`IMPORT`、`ACCOUNT`、`PASSWORD` |

### 1.5 通用 HTTP 状态码

| 状态码 | 含义 |
| --- | --- |
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录或 Token 失效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 数据冲突 |
| 413 | 上传文件过大 |
| 415 | 文件格式不支持 |
| 500 | 服务端错误 |

## 2. 健康检查

### GET /health

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "UP"
  }
}
```

**错误响应（500）：**

```json
{
  "code": 500,
  "message": "服务不可用",
  "data": null
}
```

## 3. 公开 H5 接口

### GET /api/public/contents

查询服务指引或政策宣传列表。

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `category` | String | 是 | `SERVICE_GUIDE` 或 `POLICY_PROMOTION` |
| `scope` | String | 是 | `FINANCIAL` 或 `RURAL` |
| `county_code` | String | 否 | `scope=RURAL` 时必填 |
| `page` | Integer | 是 | 从 `1` 开始 |
| `page_size` | Integer | 是 | 默认 `10`，最大 `50` |

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 101,
        "title": "征信-大庆市征信代理查询网点地址及电话",
        "category": "SERVICE_GUIDE",
        "office_name": "征信管理科",
        "published_at": "2026-05-28T09:30:00+08:00"
      }
    ],
    "total": 1,
    "page": 1,
    "page_size": 10
  }
}
```

**错误响应（400）：**

```json
{
  "code": 400,
  "message": "乡村振兴服务指引必须指定县域",
  "data": null
}
```

### GET /api/public/contents/{id}

查询文章详情。

**路径参数：**

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | Long | 内容 ID |

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 101,
    "title": "征信-大庆市县域征信服务指引",
    "category": "SERVICE_GUIDE",
    "office_name": "征信管理科",
    "published_at": "2026-05-28T09:30:00+08:00",
    "rich_text_html": "<h2>办理说明</h2><p>申请人可携带有效身份证件前往就近服务网点办理。</p>",
    "attachments": [
      {
        "id": 9001,
        "file_name": "县域征信服务网点信息表.xlsx",
        "file_type": "EXCEL",
        "file_size": 20480,
        "download_url": "/api/public/attachments/9001/download"
      }
    ]
  }
}
```

**错误响应（404）：**

```json
{
  "code": 404,
  "message": "内容不存在",
  "data": null
}
```

### GET /api/public/products

查询助企金融产品列表。H5 不提供筛选，只分页加载。

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | Integer | 是 | 从 `1` 开始 |
| `page_size` | Integer | 是 | 默认 `10`，最大 `50` |

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 2001,
        "bank_name": "农业银行",
        "product_name": "惠农e贷",
        "product_type": "AGRICULTURAL"
      }
    ],
    "total": 112,
    "page": 1,
    "page_size": 10
  }
}
```

**错误响应（400）：**

```json
{
  "code": 400,
  "message": "分页参数不合法",
  "data": null
}
```

### GET /api/public/products/{id}

查询产品详情。响应严格限制为已确认的 7 个业务字段。

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2001,
    "bank_name": "农业银行",
    "product_name": "惠农e贷",
    "product_type": "AGRICULTURAL",
    "admission_conditions": "面向农户、家庭农场及农民专业合作社，信用状况良好，经营稳定。",
    "product_intro": "用于满足农业生产经营中的流动资金需求，支持线上申请与线下服务对接。",
    "business_manager": "张经理",
    "contact_info": "0459-0002001"
  }
}
```

**错误响应（404）：**

```json
{
  "code": 404,
  "message": "产品不存在",
  "data": null
}
```

### GET /api/public/attachments/{id}/download

下载公开文章附件。

**成功响应（200）：**

```text
Content-Type: application/octet-stream
Content-Disposition: attachment; filename="县域征信服务网点信息表.xlsx"
Binary file stream
```

**错误响应（404）：**

```json
{
  "code": 404,
  "message": "附件不存在",
  "data": null
}
```

## 4. 认证接口

### POST /api/auth/login

**请求体：**

```json
{
  "username": "admin",
  "password": "Admin123!"
}
```

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "access_token": "jwt-token",
    "token_type": "Bearer",
    "expires_in": 7200,
    "user": {
      "id": 1,
      "username": "admin",
      "display_name": "系统管理员",
      "role": "ADMIN",
      "office_code": null,
      "office_name": null,
      "permissions": [
        "CONTENT_MANAGE",
        "PRODUCT_MANAGE",
        "ACCOUNT_MANAGE",
        "AUDIT_LOG_VIEW"
      ]
    }
  }
}
```

**错误响应（401）：**

```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null
}
```

### GET /api/auth/me

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "username": "hyxd",
    "display_name": "货币信贷政策管理科账号",
    "role": "OFFICE_USER",
    "office_code": "MONETARY_CREDIT",
    "office_name": "货币信贷政策管理科",
    "permissions": [
      "CONTENT_MANAGE",
      "PRODUCT_MANAGE"
    ]
  }
}
```

**错误响应（401）：**

```json
{
  "code": 401,
  "message": "登录状态已失效",
  "data": null
}
```

### POST /api/auth/change-password

**请求体：**

```json
{
  "current_password": "OldPassword1!",
  "new_password": "NewPassword2!",
  "confirm_password": "NewPassword2!"
}
```

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "changed": true
  }
}
```

**错误响应（400）：**

```json
{
  "code": 400,
  "message": "当前密码错误",
  "data": null
}
```

## 5. 后台概览

### GET /api/admin/dashboard/summary

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "published_content_count": 48,
    "product_count": 112,
    "account_count": 9,
    "today_operation_count": 16,
    "recent_contents": [
      {
        "id": 101,
        "title": "中国人民银行公告〔2025〕第12号",
        "category": "POLICY_PROMOTION",
        "published_at": "2026-05-30T16:42:00+08:00"
      }
    ]
  }
}
```

**错误响应（401）：**

```json
{
  "code": 401,
  "message": "登录状态已失效",
  "data": null
}
```

## 6. 后台选项

### GET /api/admin/options

返回固定下拉选项。

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content_categories": [
      {
        "value": "SERVICE_GUIDE",
        "label": "服务指引"
      },
      {
        "value": "POLICY_PROMOTION",
        "label": "政策宣传"
      }
    ],
    "product_types": [
      {
        "value": "AGRICULTURAL",
        "label": "涉农产品"
      },
      {
        "value": "SMALL_MICRO",
        "label": "小微产品"
      }
    ],
    "offices": [
      {
        "value": "MONETARY_CREDIT",
        "label": "货币信贷政策管理科",
        "county_code": null
      },
      {
        "value": "MACRO_PRUDENTIAL",
        "label": "宏观审慎与金融市场管理科",
        "county_code": null
      },
      {
        "value": "FINANCIAL_STABILITY",
        "label": "金融稳定科",
        "county_code": null
      },
      {
        "value": "STATISTICS_RESEARCH",
        "label": "统计研究科",
        "county_code": null
      },
      {
        "value": "PAYMENT_SETTLEMENT",
        "label": "支付结算科",
        "county_code": null
      },
      {
        "value": "CURRENCY_GOLD_SILVER",
        "label": "货币金银科",
        "county_code": null
      },
      {
        "value": "TREASURY",
        "label": "国库科",
        "county_code": null
      },
      {
        "value": "CREDIT_REPORT",
        "label": "征信管理科",
        "county_code": null
      },
      {
        "value": "ANTI_MONEY_LAUNDERING",
        "label": "反洗钱科",
        "county_code": null
      },
      {
        "value": "FOREIGN_EXCHANGE",
        "label": "外汇管理科",
        "county_code": null
      },
      {
        "value": "ZHAOZHOU",
        "label": "肇州县",
        "county_code": "ZHAOZHOU"
      },
      {
        "value": "ZHAOYUAN",
        "label": "肇源县",
        "county_code": "ZHAOYUAN"
      },
      {
        "value": "LINDIAN",
        "label": "林甸县",
        "county_code": "LINDIAN"
      },
      {
        "value": "DUMENG",
        "label": "杜蒙县",
        "county_code": "DUMENG"
      }
    ],
    "banks": [
      {
        "value": "ADBC_DAQING",
        "label": "农发行大庆市分行"
      },
      {
        "value": "ICBC",
        "label": "中国工商银行"
      },
      {
        "value": "ABC",
        "label": "农业银行"
      },
      {
        "value": "BOC",
        "label": "中国银行"
      },
      {
        "value": "CCB",
        "label": "建设银行"
      },
      {
        "value": "BOCOM_DAQING",
        "label": "交通银行大庆分行"
      },
      {
        "value": "SUNSHINE_AGRICULTURE",
        "label": "阳光惠农贷"
      },
      {
        "value": "CGB",
        "label": "广发银行"
      },
      {
        "value": "CIB",
        "label": "兴业银行"
      },
      {
        "value": "CMB",
        "label": "招商银行"
      },
      {
        "value": "SPDB_DAQING",
        "label": "浦发银行大庆分行"
      },
      {
        "value": "CMBC_DAQING",
        "label": "中国民生银行大庆分行"
      },
      {
        "value": "CITIC",
        "label": "中信银行"
      },
      {
        "value": "HARBIN_BANK",
        "label": "哈尔滨银行"
      },
      {
        "value": "KUNLUN_BANK",
        "label": "昆仑银行"
      },
      {
        "value": "LONGJIANG_BANK",
        "label": "龙江银行"
      },
      {
        "value": "CEB",
        "label": "光大银行"
      }
    ]
  }
}
```

**错误响应（401）：**

```json
{
  "code": 401,
  "message": "登录状态已失效",
  "data": null
}
```

## 7. 后台附件

### POST /api/admin/attachments

上传临时附件。

**请求：**

```text
Content-Type: multipart/form-data
file: PDF、Word 或 Excel 文件
```

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 9001,
    "file_name": "县域征信服务网点信息表.xlsx",
    "file_type": "EXCEL",
    "file_size": 20480,
    "download_url": "/api/public/attachments/9001/download"
  }
}
```

**错误响应（413）：**

```json
{
  "code": 413,
  "message": "单个附件不能超过 20MB",
  "data": null
}
```

### DELETE /api/admin/attachments/{id}

删除未绑定或有权限管理的附件。

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deleted": true
  }
}
```

**错误响应（404）：**

```json
{
  "code": 404,
  "message": "附件不存在",
  "data": null
}
```

## 8. 后台内容管理

### GET /api/admin/contents

**Query 参数：**

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `keyword` | String | 否 |
| `category` | String | 否 |
| `office_code` | String | 否 |
| `published_from` | Date | 否 |
| `published_to` | Date | 否 |
| `page` | Integer | 是 |
| `page_size` | Integer | 是 |

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 101,
        "title": "征信-大庆市征信代理查询网点地址及电话",
        "category": "SERVICE_GUIDE",
        "office_code": "CREDIT_REPORT",
        "office_name": "征信管理科",
        "published_at": "2026-05-28T09:30:00+08:00"
      }
    ],
    "total": 1,
    "page": 1,
    "page_size": 20
  }
}
```

**错误响应（403）：**

```json
{
  "code": 403,
  "message": "无权查看该办公室内容",
  "data": null
}
```

### POST /api/admin/contents

**请求体：**

```json
{
  "title": "征信-大庆市县域征信服务指引",
  "category": "SERVICE_GUIDE",
  "office_code": "CREDIT_REPORT",
  "rich_text_html": "<h2>办理说明</h2><p>申请人可携带有效身份证件办理。</p>",
  "attachment_ids": [
    9001
  ]
}
```

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 101,
    "published_at": "2026-05-28T09:30:00+08:00"
  }
}
```

**错误响应（400）：**

```json
{
  "code": 400,
  "message": "每篇内容最多上传 3 个附件",
  "data": null
}
```

### GET /api/admin/contents/{id}

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 101,
    "title": "征信-大庆市县域征信服务指引",
    "category": "SERVICE_GUIDE",
    "office_code": "CREDIT_REPORT",
    "office_name": "征信管理科",
    "rich_text_html": "<h2>办理说明</h2><p>申请人可携带有效身份证件办理。</p>",
    "published_at": "2026-05-28T09:30:00+08:00",
    "attachments": [
      {
        "id": 9001,
        "file_name": "县域征信服务网点信息表.xlsx",
        "file_type": "EXCEL",
        "file_size": 20480,
        "download_url": "/api/public/attachments/9001/download"
      }
    ]
  }
}
```

**错误响应（404）：**

```json
{
  "code": 404,
  "message": "内容不存在",
  "data": null
}
```

### PUT /api/admin/contents/{id}

**请求体：**

```json
{
  "title": "征信-大庆市县域征信服务指引（更新版）",
  "category": "SERVICE_GUIDE",
  "office_code": "CREDIT_REPORT",
  "rich_text_html": "<h2>办理说明</h2><p>请携带有效身份证件前往服务网点办理。</p>",
  "attachment_ids": [
    9001
  ]
}
```

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 101,
    "updated": true
  }
}
```

**错误响应（403）：**

```json
{
  "code": 403,
  "message": "无权修改该内容",
  "data": null
}
```

### DELETE /api/admin/contents/{id}

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deleted": true
  }
}
```

**错误响应（404）：**

```json
{
  "code": 404,
  "message": "内容不存在",
  "data": null
}
```

## 9. 后台金融产品

### GET /api/admin/products

**Query 参数：**

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `keyword` | String | 否 |
| `bank_code` | String | 否 |
| `product_type` | String | 否 |
| `page` | Integer | 是 |
| `page_size` | Integer | 是 |

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 2001,
        "product_name": "惠农e贷",
        "bank_code": "ABC",
        "bank_name": "农业银行",
        "product_type": "AGRICULTURAL",
        "updated_at": "2026-05-30T15:16:00+08:00"
      }
    ],
    "total": 112,
    "page": 1,
    "page_size": 20
  }
}
```

**错误响应（403）：**

```json
{
  "code": 403,
  "message": "无权管理金融产品",
  "data": null
}
```

### POST /api/admin/products

**请求体：**

```json
{
  "bank_code": "ABC",
  "product_name": "惠农e贷",
  "product_type": "AGRICULTURAL",
  "admission_conditions": "面向农户、家庭农场及农民专业合作社，信用状况良好，经营稳定。",
  "product_intro": "用于满足农业生产经营中的流动资金需求，支持线上申请与线下服务对接。",
  "business_manager": "张经理",
  "contact_info": "0459-0002001"
}
```

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2001
  }
}
```

**错误响应（400）：**

```json
{
  "code": 400,
  "message": "银行机构不在固定列表中",
  "data": null
}
```

### GET /api/admin/products/{id}

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2001,
    "bank_code": "ABC",
    "bank_name": "农业银行",
    "product_name": "惠农e贷",
    "product_type": "AGRICULTURAL",
    "admission_conditions": "面向农户、家庭农场及农民专业合作社，信用状况良好，经营稳定。",
    "product_intro": "用于满足农业生产经营中的流动资金需求，支持线上申请与线下服务对接。",
    "business_manager": "张经理",
    "contact_info": "0459-0002001",
    "updated_at": "2026-05-30T15:16:00+08:00"
  }
}
```

**错误响应（404）：**

```json
{
  "code": 404,
  "message": "产品不存在",
  "data": null
}
```

### PUT /api/admin/products/{id}

**请求体：**

```json
{
  "bank_code": "ABC",
  "product_name": "惠农e贷",
  "product_type": "AGRICULTURAL",
  "admission_conditions": "面向符合条件的涉农经营主体。",
  "product_intro": "用于农业生产经营中的流动资金需求。",
  "business_manager": "李经理",
  "contact_info": "0459-0002002"
}
```

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2001,
    "updated": true
  }
}
```

**错误响应（403）：**

```json
{
  "code": 403,
  "message": "无权修改金融产品",
  "data": null
}
```

### DELETE /api/admin/products/{id}

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deleted": true
  }
}
```

**错误响应（404）：**

```json
{
  "code": 404,
  "message": "产品不存在",
  "data": null
}
```

### GET /api/admin/products/import-template/download

下载 Excel 导入模板。

**成功响应（200）：**

```text
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="financial-product-import-template.xlsx"
Binary file stream
```

模板列：

| 列名 | 必填 |
| --- | --- |
| 银行机构 | 是 |
| 产品名称 | 是 |
| 类型 | 是 |
| 准入条件 | 是 |
| 产品介绍 | 是 |
| 业务经办人 | 是 |
| 联系方式 | 是 |

**错误响应（403）：**

```json
{
  "code": 403,
  "message": "无权下载金融产品模板",
  "data": null
}
```

### POST /api/admin/products/import/validate

上传并校验 Excel。

**请求：**

```text
Content-Type: multipart/form-data
file: .xlsx 文件
```

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "import_token": "import-20260602-001",
    "total_count": 112,
    "valid_count": 110,
    "invalid_count": 2,
    "errors": [
      {
        "row_number": 38,
        "field": "银行机构",
        "raw_value": "未登记银行",
        "message": "银行机构不在固定列表中"
      },
      {
        "row_number": 76,
        "field": "联系方式",
        "raw_value": "",
        "message": "联系方式不能为空"
      }
    ]
  }
}
```

**错误响应（415）：**

```json
{
  "code": 415,
  "message": "仅支持 xlsx 文件",
  "data": null
}
```

### POST /api/admin/products/import/commit

提交校验通过的数据。

**请求体：**

```json
{
  "import_token": "import-20260602-001"
}
```

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "imported_count": 110,
    "skipped_count": 2
  }
}
```

**错误响应（409）：**

```json
{
  "code": 409,
  "message": "导入任务已提交或已失效",
  "data": null
}
```

## 10. 后台账号管理

### GET /api/admin/accounts

**Query 参数：**

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `keyword` | String | 否 |
| `office_code` | String | 否 |
| `role` | String | 否 |
| `page` | Integer | 是 |
| `page_size` | Integer | 是 |

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "username": "admin",
        "display_name": "系统管理员",
        "role": "ADMIN",
        "office_code": null,
        "office_name": null,
        "enabled": true
      }
    ],
    "total": 1,
    "page": 1,
    "page_size": 20
  }
}
```

**错误响应（403）：**

```json
{
  "code": 403,
  "message": "仅管理员可管理账号",
  "data": null
}
```

### POST /api/admin/accounts

**请求体：**

```json
{
  "username": "zxglk",
  "display_name": "张伟",
  "role": "OFFICE_USER",
  "office_code": "CREDIT_REPORT",
  "initial_password": "Initial123!",
  "enabled": true
}
```

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2
  }
}
```

**错误响应（409）：**

```json
{
  "code": 409,
  "message": "登录账号已存在",
  "data": null
}
```

### GET /api/admin/accounts/{id}

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "username": "zxglk",
    "display_name": "张伟",
    "role": "OFFICE_USER",
    "office_code": "CREDIT_REPORT",
    "office_name": "征信管理科",
    "enabled": true
  }
}
```

**错误响应（404）：**

```json
{
  "code": 404,
  "message": "账号不存在",
  "data": null
}
```

### PUT /api/admin/accounts/{id}

**请求体：**

```json
{
  "display_name": "张伟",
  "role": "OFFICE_USER",
  "office_code": "CREDIT_REPORT",
  "enabled": true
}
```

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "updated": true
  }
}
```

**错误响应（400）：**

```json
{
  "code": 400,
  "message": "普通账号必须绑定一个办公室",
  "data": null
}
```

### DELETE /api/admin/accounts/{id}

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deleted": true
  }
}
```

**错误响应（409）：**

```json
{
  "code": 409,
  "message": "不能删除当前登录账号",
  "data": null
}
```

### POST /api/admin/accounts/{id}/reset-password

**请求体：**

```json
{
  "new_password": "ResetPassword1!"
}
```

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "reset": true
  }
}
```

**错误响应（404）：**

```json
{
  "code": 404,
  "message": "账号不存在",
  "data": null
}
```

## 11. 操作日志

### GET /api/admin/audit-logs

**Query 参数：**

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `operator_keyword` | String | 否 |
| `operation_type` | String | 否 |
| `operated_from` | Date | 否 |
| `operated_to` | Date | 否 |
| `page` | Integer | 是 |
| `page_size` | Integer | 是 |

**成功响应（200）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 7001,
        "operator_name": "系统管理员",
        "operation_type": "IMPORT",
        "object_type": "FINANCIAL_PRODUCT",
        "object_name": "金融产品",
        "description": "导入 112 条金融产品数据",
        "operated_at": "2026-05-30T15:16:00+08:00"
      }
    ],
    "total": 1,
    "page": 1,
    "page_size": 20
  }
}
```

**错误响应（403）：**

```json
{
  "code": 403,
  "message": "仅管理员可查看操作日志",
  "data": null
}
```
