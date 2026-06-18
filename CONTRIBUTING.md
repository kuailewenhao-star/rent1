# 贡献指南

感谢你对 Rent1 项目的关注！以下是参与项目开发的指南。

---

## 📖 代码规范

### 后端（Java / Spring Boot）

- **Java 版本**：JDK 11
- **代码风格**：遵循 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- **缩进**：4 个空格
- **行长度**：最大 120 字符
- **命名**：
  - 类名：PascalCase（`ContractController`）
  - 方法名/变量名：camelCase（`createContract`）
  - 常量：UPPER_SNAKE_CASE（`MAX_PAGE_SIZE`）
- **注释**：复杂业务逻辑必须有行内注释说明设计意图
- **DDD 分层**：
  - **API 层**（Controller / DTO / Interceptor）：参数校验、响应封装
  - **Application 层**（AppService）：业务流程编排、事务边界
  - **Domain 层**（Entity / Repository / DomainService）：核心业务规则、业务不变量校验
  - **Infrastructure 层**：技术实现（缓存、持久化、调度、安全）

### 前端（Vue 3 / TypeScript）

- **TypeScript**：所有组件和函数必须有类型标注
- **缩进**：2 个空格
- **组件命名**：PascalCase（`ContractDetail.vue`）
- **Vue 风格**：遵循 [Vue 官方风格指南](https://cn.vuejs.org/style-guide/)

### 微信小程序

- **页面命名**：Kebab-case 目录 + PascalCase 组件名
- **API 调用**：统一使用封装的 `wx.request` 辅助函数

---

## 🌿 分支策略

### 分支模型

```
main （生产分支，受保护）
  ↑
develop （开发集成分支，受保护）
  ↑   ↑
feature/*  bugfix/*  hotfix/*  release/*
```

### 分支命名规范

| 类型 | 命名 | 用途 | 来源 | 合并目标 |
|------|------|------|------|---------|
| 特性分支 | `feature/<功能描述>` | 新功能开发 | `develop` | `develop` |
| Bug 修复 | `bugfix/<问题描述>` | 修复非紧急 Bug | `develop` | `develop` |
| 紧急修复 | `hotfix/<问题描述>` | 修复线上紧急问题 | `main` | `main` + `develop` |
| 发布分支 | `release/v<版本号>` | 版本发布准备 | `develop` | `main` + `develop` |

### 示例

```bash
# 新功能分支
git checkout -b feature/dashboard-statistics develop

# Bug 修复分支
git checkout -b bugfix/contract-end-date-calculation develop

# 紧急修复
git checkout -b hotfix/tenant-login-500 main

# 发布分支
git checkout -b release/v1.1.0 develop
```

---

## 📝 提交信息规范

### Commit 格式

```
<type>(<scope>): <subject>
<空行>
<body>
<空行>
<footer>
```

### Type 类型

| 类型 | 说明 |
|------|------|
| `feat` | 新功能 |
| `fix` | Bug 修复 |
| `refactor` | 代码重构（非功能/非修复） |
| `perf` | 性能优化 |
| `style` | 代码格式调整（不影响代码逻辑） |
| `docs` | 文档变更 |
| `test` | 新增或修改测试 |
| `build` | 构建流程、工具库变更 |
| `ci` | CI 配置变更 |
| `chore` | 其他杂项（依赖升级、工具配置等） |
| `revert` | 回滚之前的提交 |

### Scope（可选）

影响的模块，如：`backend`, `frontend`, `mini-program`, `contract`, `dashboard`

### 示例

```
feat(backend): 新增合约到期自动生成账单功能

- 在 BillingCycleScheduler 中新增到期合约扫描逻辑
- 对接 ContractDomainService 获取到期合约列表
- 通过 InvoiceDomainService 批量生成账单记录
- 新增对应单元测试，覆盖正常/边界/异常场景

Closes: #123
```

```
fix(frontend): 修复仪表盘统计数据未随筛选条件刷新

- Dashboard 页 watch 逻辑缺少对 timeRange 的响应
- 添加 loading 状态提示，优化用户体验

Related: #456
```

---

## 🚀 Pull Request 流程

### 1. PR 前自检

- [ ] 代码已通过本地编译（`mvn clean package -DskipTests` / `npm run build`）
- [ ] 代码已通过代码检查工具（IDE 静态检查无错误）
- [ ] 相关单元测试已通过
- [ ] 变更相关文档已更新（如有）
- [ ] `.gitignore` 已正确排除敏感文件
- [ ] 无敏感信息（密钥、密码、个人信息）硬编码

### 2. PR 标题格式

```
<type>(<scope>): <简要描述>
```

例如：
- `feat(backend): 新增合约到期自动生成账单`
- `fix(frontend): 修复仪表盘统计筛选不刷新`
- `docs: 更新 README 部署说明`

### 3. PR 描述模板

请使用仓库内置的 PR 模板（`.github/PULL_REQUEST_TEMPLATE.md`）填写。

### 4. 代码评审（Code Review）

- 每个 PR 至少需要 **1 位核心成员** review 通过才能合并
- 评审反馈建议 **48 小时内**响应
- 评审意见可分为：
  - **必须修复（Blocker）**：必须修改后才能合并
  - **建议修改（Suggestion）**：可讨论，建议修改
  - **点赞 / 建议（Nitpick）**：非强制性优化建议

### 5. 合并策略

- 推荐使用 **Squash and merge**（压缩提交后合并）
- 保持 commit history 清晰可读
- 合并后删除源分支

---

## ✅ 测试要求

### 后端测试

- **单元测试**：覆盖核心业务逻辑（DomainService 方法）
- **集成测试**：关键 API 端点
- 运行命令：
  ```bash
  cd backend
  mvn test
  ```

### 前端测试

- **组件测试**：关键业务组件
- **E2E 测试**：Playwright 覆盖主要业务流程
- 运行命令：
  ```bash
  cd admin-web
  npm run test
  ```

---

## 🔐 安全规范

### 敏感信息处理

- **绝对禁止**将任何密钥、密码、Token 提交到 Git
- 数据库连接、JWT Secret、AES Key、微信 Secret 等必须通过环境变量注入
- `application.yml` 中的敏感配置使用 `${ENV_VAR}` 占位符

### 数据保护

- 用户手机号、身份证、姓名必须使用 AES 加密存储
- 接口返回必须做脱敏处理（`138****8888`、`33010********1234`）
- 日志中不得打印敏感字段明文

### 依赖安全

- 定期检查依赖安全漏洞（`npm audit` / Maven 插件）
- 发现高危漏洞优先处理，不要合并包含漏洞的代码

---

## 📜 版本号规范

遵循 [Semantic Versioning 2.0.0](https://semver.org/lang/zh-CN/)

```
主版本号.次版本号.修订号 （MAJOR.MINOR.PATCH）
```

- **MAJOR**：不兼容的 API 改动
- **MINOR**：向后兼容的功能性新增
- **PATCH**：向后兼容的问题修正

示例：`v1.0.0`, `v1.1.0`, `v1.1.3`

---

## 💬 沟通方式

- **GitHub Issues**：Bug 报告、功能请求
- **Pull Request**：代码评审、变更讨论
- **代码评审注释**：针对特定代码行的建议

---

## 🚧 常见问题

### 1. 如何处理数据库迁移？

- 所有 DDL 变更放在 `backend/src/main/resources/db/ddl/` 或 `sql/` 下
- 版本号命名：`YYYYMMDD_<描述>.sql`
- 在 PR 描述中明确说明数据库变更内容

### 2. 新功能是否需要编写测试？

- 是，必须包含对应的单元测试
- 核心业务逻辑覆盖率建议 ≥ 80%

### 3. 依赖升级如何处理？

- 在独立的 PR 中进行，不要与功能开发混在一起
- 升级前检查升级说明，注意 Breaking Change
- 升级后完整跑一遍测试套件

---

## 🤝 感谢

感谢每一位贡献者的付出！让我们一起把 Rent1 做得更好 🌟
