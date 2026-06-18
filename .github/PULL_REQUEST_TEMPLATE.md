## 🎯 变更类型

<!-- 请勾选适用的类型 -->

- [ ] 🆕 `feat` 新功能
- [ ] 🐛 `fix` Bug 修复
- [ ] ♻️ `refactor` 代码重构
- [ ] ⚡ `perf` 性能优化
- [ ] 💄 `style` 代码格式调整
- [ ] 📝 `docs` 文档变更
- [ ] ✅ `test` 测试相关
- [ ] 🔧 `build` 构建相关
- [ ] 🤖 `ci` CI 配置变更
- [ ] 🧹 `chore` 其他杂项
- [ ] ⏪ `revert` 回滚提交

---

## 📋 变更描述

<!-- 清晰描述本次 PR 的主要变更内容 -->

### 核心变更

1. 
2. 
3. 

### 设计思路 / 背景

<!-- 说明为什么这样做，解决了什么问题 -->

---

## 🔗 关联 Issue

<!-- 关联的 GitHub Issue，使用 Closes/Fixes/Refs 关键字 -->

- Closes: #<issue-number>
- Refs: #<issue-number>

---

## 🧪 自测清单

### 后端（如适用）

- [ ] `mvn clean package` 编译通过
- [ ] 单元测试 `mvn test` 全部通过
- [ ] 新增测试覆盖核心业务逻辑
- [ ] 启动服务后 `/api/health` 返回正常
- [ ] 关键 API 手动测试通过

### 前端管理后台 admin-web（如适用）

- [ ] `npm install` 依赖安装成功
- [ ] `npm run build` 构建通过
- [ ] `npm run dev` 启动后页面正常加载
- [ ] 相关页面交互功能自测通过

### 前端 H5 mobile-app（如适用）

- [ ] `npm install` 依赖安装成功
- [ ] `npm run build` 构建通过
- [ ] 相关页面交互功能自测通过

### 微信小程序（如适用）

- [ ] 微信开发者工具可正常导入
- [ ] 编译通过无报错
- [ ] 核心页面功能正常

---

## 🏗 数据库变更（如适用）

- [ ] 本次包含 DDL 变更
- [ ] 已在 `backend/src/main/resources/db/ddl/` 或 `sql/` 下新增脚本
- [ ] 脚本命名：`YYYYMMDD_<描述>.sql`
- [ ] 升级/回滚方案已说明

**变更内容**：

---

## 🔐 安全检查

- [ ] 无密钥/密码/Token 硬编码
- [ ] 敏感配置使用 `${ENV_VAR}` 注入
- [ ] 接口参数校验已覆盖
- [ ] SQL 注入风险排查（无字符串拼接 SQL）
- [ ] XSS 风险排查（前端对用户输入做处理）
- [ ] 日志中无敏感字段明文

---

## 📸 截图 / 演示

<!-- 如有 UI 变更，请附上截图或动图 -->

---

## 📚 文档更新

- [ ] README.md 已更新（如适用）
- [ ] CONTRIBUTING.md 已更新（如适用）
- [ ] 接口文档 / API 注释已更新
- [ ] 无文档变更

---

## ⚠️ 注意事项 / 待办

<!-- 列出需要评审者特别关注的内容，或尚未完成的 TODO -->

- 
- 

---

## ✅ 合并前检查清单

- [ ] PR 标题符合 `type(scope): subject` 格式
- [ ] 分支命名符合规范（`feature/*`, `bugfix/*`, `hotfix/*`）
- [ ] 至少 1 位核心成员已 Review 通过
- [ ] 所有 CI 检查已通过
- [ ] 与目标分支（develop/main）无冲突
- [ ] 无临时调试代码（console.log / System.out.println 等）

---

**感谢你的贡献！💪**
