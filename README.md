# 房东租赁管理系统（Rent1）

> 面向房东的房源、合约、账单、押金一体化管理系统
>
> 前端：Vue 3 + Vite · 后端：Spring Boot 2.7 + Java 11 · 客户端：微信小程序

---

## 📋 项目概述

Rent1 是一个为房东和租客提供的综合性租赁管理平台，核心功能包括：

- 🏠 **房源管理** - 房源和房间的创建、编辑与状态管理
- 📄 **合约管理** - 租赁合同签订、续签、终止与作废
- 💰 **账单管理** - 收入账单和支出账单的管理与统计
- 🏦 **押金管理** - 押金收取、退还与结算管理
- 📊 **数据统计** - 仪表盘与收支明细统计
- 🔔 **通知管理** - 系统通知与消息推送
- 👥 **成员管理** - 房东/租客信息与联系人管理
- 🔐 **权限控制** - 基于角色的访问控制与数据隔离

---

## 🛠 技术栈

### 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 11 | 开发语言 |
| Spring Boot | 2.7.14 | 后端框架 |
| MyBatis-Plus | 3.5.3.1 | ORM 数据访问 |
| MySQL | 8.0+ | 数据库 |
| Redis | 7.0+ | 缓存与会话 |
| JWT | - | 身份认证 |
| AES | - | 敏感字段加密 |
| Maven | 3.9+ | 构建工具 |
| Lombok | - | 代码简化 |

### 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5.34 | 前端框架 |
| Vite | 8.0.12 | 构建工具 |
| TypeScript | 6.0.2 | 类型安全 |
| Element Plus | 2.14.2 | UI 组件库 |
| Pinia | 3.0.4 | 状态管理 |
| Vue Router | 5.1.0 | 路由管理 |
| Axios | 1.18.0 | HTTP 客户端 |
| ECharts | 6.1.0 | 图表可视化 |

### 微信小程序

| 技术 | 版本 |
|------|------|
| 微信小程序原生框架 | 最新 |
| AppID | wx3ead2b8ed5519f92 |

---

## 📁 项目结构

```
rent1/
├── backend/                            # Spring Boot 后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/rent1/
│   │   │   │   ├── api/               # API 层：Controller / DTO / Interceptor
│   │   │   │   │   ├── config/        # 跨域/Web 配置
│   │   │   │   │   ├── controller/    # REST 控制器
│   │   │   │   │   ├── dto/           # 请求/响应数据传输对象
│   │   │   │   │   └── interceptor/   # 鉴权/权限拦截器
│   │   │   │   ├── application/       # 应用层：业务编排（AppService）
│   │   │   │   ├── common/            # 通用组件：枚举/异常/响应
│   │   │   │   ├── config/            # 配置：MyBatis/Swagger
│   │   │   │   ├── domain/            # 领域层：实体/仓库/领域服务
│   │   │   │   │   ├── billing/       # 计费规则域
│   │   │   │   │   ├── contract/      # 合约域
│   │   │   │   │   ├── deposit/       # 押金域
│   │   │   │   │   ├── invoice/       # 账单域
│   │   │   │   │   ├── member/        # 成员域
│   │   │   │   │   ├── notification/  # 通知域
│   │   │   │   │   ├── property/      # 房源域
│   │   │   │   │   └── statistics/    # 统计域
│   │   │   │   ├── infrastructure/    # 基础设施层：缓存/事件/持久化/调度/安全
│   │   │   │   ├── interfaces/        # 接口层：内部 Controller
│   │   │   │   └── RentManagementApplication.java
│   │   │   └── resources/
│   │   │       ├── application.yml    # 应用配置
│   │   │       ├── db/                # DDL 脚本
│   │   │       └── sql/               # SQL 脚本
│   │   └── test/java/com/rent1/       # 单元测试
│   ├── docs/                           # 文档
│   │   └── 微信云托管部署运维手册.md  # 部署运维手册
│   ├── pom.xml                         # Maven 配置
│   ├── .dockerignore
│   └── Dockerfile                      # Dockerfile
│
├── admin-web/                          # Vue 3 管理后台
│   ├── src/
│   │   ├── api/                        # API 接口封装
│   │   ├── assets/                     # 静态资源
│   │   ├── components/                 # 组件
│   │   ├── router/                     # 路由配置
│   │   ├── stores/                     # Pinia 状态管理
│   │   ├── types/                      # TypeScript 类型
│   │   ├── views/                      # 页面视图
│   │   ├── App.vue
│   │   └── main.ts
│   ├── public/                         # 公共资源
│   ├── .env.development                # 开发环境变量
│   ├── .env.production                 # 生产环境变量
│   ├── index.html
│   ├── vite.config.ts                  # Vite 配置
│   ├── tsconfig.json                   # TypeScript 配置
│   ├── package.json                    # 依赖配置
│   └── README.md
│
└── wechat-mini-program/                # 微信小程序
    ├── app.js                          # 小程序入口
    ├── app.json                        # 全局配置
    ├── project.config.json             # 项目配置
    ├── pages/                          # 页面目录
    │   ├── property/                   # 房源相关页面
    │   ├── room/                       # 房间相关页面
    │   ├── contract/                   # 合约相关页面
    │   ├── bill/                       # 账单相关页面
    │   ├── message/                    # 消息/通知
    │   ├── mine/                       # 个人中心
    │   ├── login/                      # 登录页
    │   └── index/                      # 首页
    └── components/                     # 自定义组件
```

---

## 🚀 快速开始

### 环境要求

| 工具 | 推荐版本 | 说明 |
|------|---------|------|
| JDK | 11.x | 必需 |
| Maven | 3.9.x | 必需 |
| MySQL | 8.0.x | 必需 |
| Redis | 7.x | 必需 |
| Node.js | 18.x / 20.x | 前端开发必需 |
| npm | 9.x / 10.x | 跟随 Node.js 安装 |
| 微信开发者工具 | 最新 | 小程序开发必需 |

### 1. 克隆项目

```bash
git clone https://github.com/<your-org>/rent1.git
cd rent1
```

### 2. 数据库初始化

```sql
-- 在 MySQL 中创建数据库
CREATE DATABASE rent_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 按顺序执行 backend/src/main/resources 下的 DDL 脚本
-- 1) member.sql
-- 2) db/ddl/property_domain_ddl.sql
-- 3) notification_schema.sql
-- 4) sql/billing_rules_ddl.sql
-- 5) 其他业务域 DDL
```

### 3. 后端启动

```bash
cd backend

# 方式一：Maven 启动
mvn spring-boot:run

# 方式二：先打包再运行
mvn clean package -DskipTests
java -jar target/rent-management-backend-1.0.0.jar

# 健康检查
curl http://localhost:8080/api/health
```

> 默认端口：`8080`，Context Path：`/api`

### 4. 前端启动

```bash
cd admin-web

# 安装依赖
npm install

# 开发模式（Vite）
npm run dev
# 默认打开 http://localhost:3000

# 生产构建
npm run build
```

### 5. 微信小程序启动

1. 打开 **微信开发者工具**
2. 选择 **导入项目** → 目录选择 `wechat-mini-program/`
3. 填写 AppID：`wx3ead2b8ed5519f92`
4. 点击编译运行

---

## 🌐 API 接口说明

### 健康检查

```
GET /api/health
```

### 主要接口前缀

| 模块 | 前缀 | 主要功能 |
|------|------|---------|
| 认证 | `/api/auth`, `/api/admin/auth` | 登录、登出、token 管理 |
| 房源 | `/api/property` | 房源 CRUD |
| 房间 | `/api/room` | 房间 CRUD、状态管理 |
| 合约 | `/api/contract` | 合约 CRUD、状态变更 |
| 账单 | `/api/income-invoice`, `/api/expense-invoice` | 收入/支出账单 |
| 押金 | `/api/deposit` | 押金收取/退还/结算 |
| 成员 | `/api/member`, `/api/admin/member` | 房东/租客管理 |
| 仪表盘 | `/api/dashboard`, `/api/dashboard/statistics` | 数据统计 |
| 通知 | `/api/notification` | 通知 CRUD、标记已读 |
| 计费规则 | `/api/billing-rules` | 计费规则管理 |

---

## 🔐 安全与权限

- **JWT 认证** - 基于 token 的无状态身份验证
- **AES 字段加密** - MySQL 中手机号、身份证、姓名等敏感字段加密存储
- **接口请求签名** - timestamp + nonce + 请求体的 HMAC-SHA256 校验
- **角色权限验证** - `@RequireRole` 注解实现角色访问控制
- **数据权限隔离** - ThreadLocal (`DataScopeContext`) 实现 member 级数据过滤
- **数据脱敏** - 接口返回时手机号、身份证、姓名自动脱敏
- **防重放攻击** - 时间戳和 nonce 校验，有效期 5 分钟
- **接口幂等性** - `@Idempotent` 注解 + Redis 分布式锁

---

## 📚 文档

- **Git 初始化与 GitHub 代码管理指南** - [docs/GIT_GUIDE.md](docs/GIT_GUIDE.md)
- **贡献指南（分支策略 / PR 流程 / 提交规范）** - [CONTRIBUTING.md](CONTRIBUTING.md)
- **变更日志（版本历史）** - [CHANGELOG.md](CHANGELOG.md)
- **GitHub Issues 模板** - [.github/ISSUE_TEMPLATE/](.github/ISSUE_TEMPLATE/)
- **GitHub PR 模板** - [.github/PULL_REQUEST_TEMPLATE.md](.github/PULL_REQUEST_TEMPLATE.md)
- **微信云托管部署运维手册** - [backend/docs/微信云托管部署运维手册.md](backend/docs/微信云托管部署运维手册.md)

---

## 📦 发布流程

1. **开发** → 在 `feature/*` 分支开发
2. **自测** → 本地启动验证功能
3. **PR** → 创建 Pull Request 合并到 `develop`
4. **CI** → GitHub Actions 自动运行编译/测试
5. **发布** → 合并到 `main`，打 tag `v1.x.x`
6. **部署** → 自动触发微信云托管部署

---

## 🐛 问题反馈

遇到问题请提交 [Issue](https://github.com/<your-org>/rent1/issues)，描述：
- 环境（操作系统、JDK 版本、MySQL 版本）
- 复现步骤
- 预期结果
- 实际结果
- 控制台日志（脱敏后）

---

## 📄 License

MIT License

---

**Made with ❤️ by Rent1 Team**
