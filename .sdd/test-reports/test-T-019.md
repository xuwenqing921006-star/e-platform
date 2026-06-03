# 测试报告：T-019 初始账号与产品样本数据真实闭环

**测试时间**：2026-06-03 23:39:21 +08:00
**Tester Agent ID**：codex-tester

## 结果：PASS

## 验收范围说明

用户已确认本轮 T-019 验收口径为：真实 Excel 来源共有 112 条产品，本轮仅从中抽取 10 条真实样本进行初始化闭环，不声明 112 条全量初始化。

## 验收标准逐条验证

| # | 标准 | 结果 | 说明 |
|---|------|------|------|
| 1 | 初始化后产品样本总数为 10，覆盖涉农/小微两类 | PASS | H2 seed 重复执行后 `cb_financial_product` 为 10 条；`AGRICULTURAL` 5 条、`SMALL_MICRO` 5 条。用户最新口径覆盖原任务中的 112 全量要求。 |
| 2 | 固定银行和阳光惠农贷特殊行校验通过 | PASS | seed 包含 `阳光惠农贷`，且 `bank_name/product_name` 均为 `阳光惠农贷`，`bank_code` 为 `SUNSHINE_AGRICULTURE`，类型为 `AGRICULTURAL`；测试已覆盖。 |
| 3 | 本地测试密码和 JWT 值不写入代码、docs/** 或 .sdd/** | PASS | 对 Developer 产出文件执行敏感信息扫描，未发现真实密码、JWT Secret、数据库密码、Bearer Token 或 sk 类密钥；只存在字段名和合规说明。 |
| 4 | 初始化脚本或 SQL 可重复执行 | PASS | H2 seed 使用 `MERGE`，MySQL seed 使用固定 ID + `ON DUPLICATE KEY UPDATE`；T019 测试重复执行 H2 seed 后产品和账号扩展数量不重复。 |
| 5 | backend Maven 测试通过 | PASS | 指定 T019/CentralBankSchema 测试通过；`central-bank-business -am test` 通过；补充 `ruoyi-admin -am -DskipTests package` 通过。 |
| 6 | H5 与后台产品页面显示真实 seed 数据 | PASS | H5 产品 service 调用 `/public/products*`；若依后台产品 API 调用 `/api/admin/products*`；页面默认真实接口路径可读取后端 seed 数据，Mock 提示仅在 `VITE_USE_MOCK !== 'false'` 时出现。 |

## 独立 Excel 核验

- Excel 文件存在：`C:\Users\31333\Desktop\全市银行机构涉农、小微信贷产品汇总表.xlsx`
- 使用 Python 标准库解析 xlsx OOXML 结构，非人工猜测。
- Excel 计数：`表1涉农产品` 52 条，`表2小微产品` 60 条。
- seed 10 条样本均能在 Excel 中按序号定位：

| 类型 | Excel 序号 | 银行机构 | 产品名称 | 结果 |
|---|---:|---|---|---|
| 涉农 | 表1-27 | 建设银行 | 善营贷 | PASS |
| 涉农 | 表1-31 | 阳光惠农贷 | 阳光惠农贷 | PASS |
| 涉农 | 表1-45 | 中信银行 | 粮农贷 | PASS |
| 涉农 | 表1-46 | 哈尔滨银行 | 农兴贷 | PASS |
| 涉农 | 表1-50 | 昆仑银行 | 昆仑E贷 | PASS |
| 小微 | 表2-1 | 中国工商银行 | 新一代经营快贷 | PASS |
| 小微 | 表2-3 | 农业银行 | 微捷贷 | PASS |
| 小微 | 表2-7 | 中国银行 | 个人经营贷 | PASS |
| 小微 | 表2-8 | 建设银行 | 信用快贷 | PASS |
| 小微 | 表2-51 | 哈尔滨银行 | 科新贷 | PASS |

## 验证命令摘要

| 命令 | 结果 | 说明 |
|---|---|---|
| `.\mvnw.cmd -q -pl central-bank-business -am "-Dtest=CentralBankSchemaTest,T019InitialDataClosureTest" test` | 环境问题 | Maven wrapper 在当前 PowerShell 环境报 `Cannot start maven from wrapper`，改用本机 Maven 3.9.16 执行同等命令。 |
| `mvn -q -pl central-bank-business -am "-Dtest=CentralBankSchemaTest,T019InitialDataClosureTest" "-Dsurefire.failIfNoSpecifiedTests=false" test` | PASS | 指定 T019 与 schema 测试通过。 |
| `mvn -q -pl central-bank-business -am test` | PASS | central-bank-business 及依赖模块测试通过。 |
| `mvn -q -pl ruoyi-admin -am -DskipTests package` | PASS | ruoyi-admin 打包通过。 |

## 备注

- `.sdd/tasks.json` 在验证前 T-019 notes 后缺少逗号，导致文件不是合法 JSON；本轮随 T-019 状态更新一并修正该语法点。
- 未写入 `.sdd/bug-logs/T-019.md`，因为本轮无 FAIL。
