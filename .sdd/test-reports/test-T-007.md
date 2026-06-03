# 测试报告: T-007 若依全栈基座接入

**测试时间**: 2026-06-03 08:12 Asia/Shanghai
**Tester Agent ID**: f5b4b155-838d-4749-8aa3-aa1b0e7e85fb

## 结果: PASS

## 验收标准逐条验证

| # | 标准 | 结果 | 说明 |
|---|------|------|------|
| 1 | 项目中存在 backend/pom.xml、backend/mvnw.cmd、backend/ruoyi-admin、backend/ruoyi-system、backend/ruoyi-framework 等若依后端模块。 | PASS | `backend/scripts/verify-ruoyi-baseline.ps1` 输出 `Ruoyi baseline structure verified.`；关键若依后端模块均存在。 |
| 2 | 项目中存在 ruoyi-ui/package.json 和若依后台前端源码；现有 frontend/ H5 未被覆盖。 | PASS | `ruoyi-ui/package.json` 与 `ruoyi-ui/src/**` 存在；`frontend` 的 `npm run typecheck`、`npm run test`、`npm run build` 全部通过，Vitest 6 files / 46 tests passed。 |
| 3 | GET /health 或若依等价健康检查返回 docs/api-contracts.md 约定的统一成功格式。 | PASS | 使用 `health` profile 短时启动 `ruoyi-admin.jar` 到 8099，请求 `http://127.0.0.1:8099/health` 返回 HTTP 200，响应体为 `{"code":200,"message":"success","data":{"status":"UP"}}`。 |

## 技术检查

| # | 检查项 | 结果 | 证据 |
|---|------|------|------|
| 1 | backend `.\mvnw.cmd test`。 | PASS | Maven Reactor `BUILD SUCCESS`；`HealthControllerTest` Tests run: 2, Failures: 0, Errors: 0。 |
| 2 | 短时启动后端到 8099 并验证健康检查返回 200。 | PASS | `java -jar ruoyi-admin/target/ruoyi-admin.jar --spring.profiles.active=health --server.port=8099` 启动后，`/health` 返回契约成功格式。 |
| 3 | ruoyi-ui 构建。 | PASS | `npm run build:prod` 退出码 0；仅有 Vue CLI 资产体积 warning。 |
| 4 | frontend H5 未被覆盖。 | PASS | `npm run typecheck`、`npm run test`、`npm run build` 均通过。 |
| 5 | 确认业务代码未写入 templates/ 或 vendor/ruoyi。 | PASS | `templates/` 不存在；本轮未修改 `vendor/ruoyi`。 |

## Fallback 边界

- 完整若依业务 profile 后续仍依赖 MySQL/Redis 等运行配置；T-007 的健康检查使用 `health` profile 验证数据库无关启动基线。
- 本轮验证不宣称真实 MySQL 8 联调通过；真实数据层联调进入 T-008 及后续任务处理。

## 超出范围发现（不影响当前任务判定）

| # | 问题 | 所属模块 | 建议处理方式 |
|---|------|---------|------------|
| 1 | 当前 Codex 会话 PATH 未自动继承新安装的 Java，需要在命令内临时设置 `JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`。 | 本地环境 | 新开终端或重启 Codex 后应可继承系统 PATH；在此之前后端命令需显式设置 `JAVA_HOME`。 |
| 2 | `ruoyi-ui` 生产构建存在资产体积 warning。 | 若依模板前端 | 属若依模板常规构建提示，不影响 T-007；后续如需性能优化可单独处理。 |
