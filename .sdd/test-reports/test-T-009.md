# T-009 测试报告：若依安全、契约响应与 H5 公开路由适配

## 范围

- 复用若依 Spring Security/JWT 基础链路，显式放行 `/health`、`/api/public/**`，并要求 `/api/admin/**` 认证访问。
- 将 `AjaxResult` 统一为 `{ code, message, data }` 契约键名，并确保错误响应包含 `data: null`。
- 为未认证与权限不足分别提供 401/403 统一错误处理。
- 将 JWT、上传根目录、数据库连接和 Druid 监控登录密码改为本地环境变量配置。
- 将本地 H5 与管理端开发端口加入精确 CORS 允许源。

## 验证命令

```powershell
cd backend
.\mvnw.cmd -q -pl ruoyi-admin -am test
.\mvnw.cmd -q test
.\mvnw.cmd -q -pl ruoyi-admin -am -DskipTests package
```

```powershell
java -jar backend\ruoyi-admin\target\ruoyi-admin.jar --spring.profiles.active=health --server.port=8103
Invoke-RestMethod http://127.0.0.1:8103/health
```

```powershell
rg -n "abcdefghijklmnopqrstuvwxyz|password: password|login-password:\s*123456|D:/ruoyi/uploadPath" backend docs .sdd -g "!backend/ruoyi-admin/src/test/java/com/ruoyi/web/security/SecurityContractTest.java"
```

## 验证结果

- `ruoyi-admin` 模块测试通过。
- 后端 Maven 全量测试通过。
- `health` profile jar 短启动通过，`/health` 无登录返回 `code=200`、`message=success`、`data.status=UP`。
- 敏感值扫描未发现旧 JWT secret、模板数据库密码、Druid `123456` 或旧 Windows 上传路径残留。
- `SecurityContractTest` 覆盖 401/403 HTTP 状态码、响应体字段、`data: null` 序列化、公开/受保护路由声明、CORS 源和环境变量配置。

## 边界说明

- 本轮验证的是安全与契约基础适配；完整 `/api/auth/*` 登录、当前用户、本人改密仍归属后续认证闭环。
- 真实 MySQL 8 尚未提供，本轮不宣称真实数据库联调通过。
- `/api/public/**` 与 `/api/admin/**` 的具体业务接口运行态联调将在 T-010 及后续真实闭环任务中完成。
