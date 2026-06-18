# Plan: H5 前端页面完善与设计一致性优化

**Source PRD**: `C:\Users\Alex\Documents\Obsidian Vault\1-Projects\房东租赁管理系统\PRD.md`
**Project**: `C:\Users\Alex\rent1\mobile-app`
**Complexity**: Large

## Summary

基于 PRD V1.0 对 H5 移动端前端进行全面完善，核心目标：
1. **统一设计风格**：消除 COLOR 对象重复定义，统一颜色/间距/圆角系统
2. **补全导航断裂点**：修复详情页入口缺失、菜单重复跳转
3. **增强表单交互**：新增/编辑功能从 alert 占位升级为真实表单页面
4. **PRD 功能对齐**：确保所有 PRD 要求的前端展示元素完整覆盖
5. **代码质量提升**：修复模块级可变状态反模式、修复已知 bug
6. **租客视图**：补充租客角色的独立页面和路由

## Patterns to Mirror

| Category | Source | Pattern |
|---|---|---|
| 颜色系统 | `tailwind.config.js:9-35` | brand/ink/surface 三级色板，Tailwind 扩展配置 |
| 卡片样式 | `src/styles/index.css:27-29` | `.card` = bg-white + rounded-[20px] + shadow-card |
| 组件样式 | `src/components/BottomNav.jsx:10-44` | NavLink + isActive + SVG 图标内联 |
| 布局模式 | `src/components/AppLayout.jsx:4-11` | Outlet + BottomNav 固定底部 + safe-area |
| 路由结构 | `src/App.jsx:22-33` | AppLayout 嵌套路由 + index redirect |
| Mock 数据 | `src/data/mock.js:271-378` | 派生统计函数 + 辅助查询函数 |
| 脱敏规则 | `src/data/mock.js:2-12` | maskPhone(mask 4位) + maskName(保留1位) |
| 渐变头部 | `src/pages/Home.jsx:63` | linear-gradient brand 蓝 + 白色文字 |
| Tab 切换 | `src/pages/Archive.jsx:55-63` | 双色胶囊按钮 + style 背景色 |

## Files to Change

| File | Action | Why |
|---|---|---|
| `src/data/colors.js` | CREATE | 提取全局 COLOR 常量，消除重复定义 |
| `src/data/mock.js` | UPDATE | 补充 diffDays 负数处理、修复 paidDate 硬编码、补充 tenant 详情查询函数 |
| `src/App.jsx` | UPDATE | 补充路由守卫、新增租客路由、新增编辑/新增表单页面路由 |
| `src/components/AuthGuard.jsx` | CREATE | 路由认证守卫组件 |
| `src/components/BottomNav.jsx` | UPDATE | 修复 Tailwind 自定义类名兼容、补充合约/消息 badge |
| `src/pages/Login.jsx` | UPDATE | 补充密码登录入口（PRD 3.2.2 PC后台规则延伸到H5）、增加协议勾选 |
| `src/pages/RoleSelect.jsx` | UPDATE | 补充角色持久化（localStorage）、记住上次选择 |
| `src/pages/Home.jsx` | UPDATE | 统一 COLOR 引用、修复盈利计算、补充提醒点击跳转 |
| `src/pages/Archive.jsx` | UPDATE | 修复模块级变量、补充房间/租客详情跳转、统一 COLOR |
| `src/pages/Bill.jsx` | UPDATE | 修复 paidDate 硬编码、补充账单详情入口、统一 COLOR |
| `src/pages/Profile.jsx` | UPDATE | 修复菜单重复导航、补充个人信息编辑入口、统一 COLOR |
| `src/pages/Contracts.jsx` | UPDATE | 修复模块级变量、补充合约详情跳转、统一 COLOR |
| `src/pages/Messages.jsx` | UPDATE | 补充消息已读标记、消息详情展开、统一 COLOR |
| `src/pages/HouseDetail.jsx` | UPDATE | 补充房间列表点击跳转、补充编辑/删除按钮、统一 COLOR |
| `src/pages/RoomDetail.jsx` | UPDATE | 补充租客联系按钮、合约到期倒计时、统一 COLOR |
| `src/pages/BillDetail.jsx` | UPDATE | 补充操作按钮（标记已支付/编辑/删除）、统一 COLOR |
| `src/pages/CreateContract.jsx` | CREATE | 新建合约表单页面（PRD 4.4） |
| `src/pages/TenantDetail.jsx` | CREATE | 租客详情页面（PRD 4.3.5 联系人管理） |
| `src/pages/ContractDetail.jsx` | CREATE | 合约详情页面（PRD 4.4.3） |
| `src/pages/EditRoom.jsx` | CREATE | 房间编辑/新增表单 |
| `src/pages/ExpenseForm.jsx` | CREATE | 支出账单新增/编辑表单（PRD 4.5） |

## Tasks

### Task 1: 统一设计系统基础

**Action**: 创建全局颜色常量文件，统一所有页面的 COLOR 引用

- 创建 `src/data/colors.js`，导出统一的 COLOR 对象
- 修改所有页面组件，从 `colors.js` 导入 COLOR
- 修复 BottomNav 中 Tailwind 自定义类名兼容性问题
- 统一卡片间距（p-4）、按钮圆角（rounded-[14px]）、标签圆角（rounded-[10px]）

**Mirror**: `tailwind.config.js:9-35` 颜色定义
**Validate**: 所有页面颜色一致，BottomNav 图标颜色正确

### Task 2: 修复已知 Bug

**Action**: 修复探索阶段发现的所有已知 bug

- 修复 `Archive.jsx` / `Bill.jsx` / `Contracts.jsx` 中模块级可变状态反模式
  - 改用 `useState` + Context 或 `useReducer` 管理本地操作状态
- 修复 `Bill.jsx` 中 `paidDate: 'today'` 硬编码为 `paidDate: todayISO()`
- 修复 `diffDays` 函数对过期日期返回负数的问题
- 修复 `RoomDetail.jsx` 紧急联系人姓名脱敏不一致
- 修复 `HouseDetail.jsx` 房间列表不可点击跳转
- 修复 `App.jsx` 未知路由重定向到 `/home` 而非 `/login`
- 修复 `Profile.jsx` 菜单项重复跳转 `/archive`

**Mirror**: `src/data/mock.js:14-26` 工具函数模式
**Validate**: `npm run build` 无错误，各页面交互正常

### Task 3: 补全导航断裂点

**Action**: 修复所有详情页入口缺失问题

- HouseDetail 房间列表添加 `onClick={() => nav('/room/' + r.id)}`
- Bill 列表账单卡片添加 `onClick={() => nav('/bill/' + b.id)}`
- Contracts 列表合约卡片添加 `onClick={() => nav('/contract-detail/' + c.id)}`
- Messages 消息卡片添加点击展开详情
- Profile 菜单项区分不同入口（房源管理->/archive?tab=house, 租客档案->/archive?tab=tenant）
- 新增路由 `/contract-detail/:id` 显示合约详情（含计费规则）
- 新增路由 `/tenant/:id` 显示租客详情（含联系人信息）

**Mirror**: `src/App.jsx:22-33` 路由嵌套模式
**Validate**: 每个详情页都有至少一个可点击入口

### Task 4: 新增表单页面

**Action**: 将 alert 占位升级为真实表单页面

- `CreateContract.jsx`: 新建合约表单
  - 租客选择（下拉/搜索）
  - 房间选择
  - 计费规则 JSON 配置（PRD 4.6 固定枚举选择）
  - 租期选择（起止日期）
  - 纸质合约图片上传占位
- `EditRoom.jsx`: 房间编辑/新增表单
  - 房间号、面积、月租、押金、缴费周期、支付方式
  - 房间状态选择
  - 多图上传占位
- `ExpenseForm.jsx`: 支出账单新增/编辑表单（PRD 4.5）
  - 房源选择
  - 支出类型下拉（10类固定枚举）
  - 金额输入
  - 支出日期
  - 备注
- `TenantDetail.jsx`: 租客详情页面
  - 基本信息（脱敏）
  - 紧急联系人（PRD 4.3.5）
  - 关联房间/合约/账单

**Mirror**: `src/pages/Archive.jsx:55-63` Tab 切换模式
**Validate**: 表单字段完整覆盖 PRD 要求，必填项有校验提示

### Task 5: 补充租客视图

**Action**: 为租客角色提供独立的功能页面

- 租客首页 `/tenant-home`: 展示个人房间、合约、待缴账单
- 租客个人中心 `/tenant-profile`: 个人信息、押金总额、紧急联系人
- 租客账单列表 `/tenant-bills`: 待缴/已缴/逾期账单
- 租客合约详情 `/tenant-contract`: 合约期、计费规则、缴费记录
- 修改 `RoleSelect.jsx` 使租客角色登录后进入租客视图
- 角色状态持久化到 localStorage

**Mirror**: `src/pages/Home.jsx:46-226` 房东首页模式，适配租客视角
**Validate**: 房东/租客视图数据隔离，权限清晰

### Task 6: 消息中心增强

**Action**: 完善消息交互和状态管理

- 消息卡片点击标记为已读
- 消息详情展开（原 content 展示）
- 未读消息 badge 实时更新
- 消息分类筛选（账单/合约/系统）
- 首页提醒与消息中心数据统一

**Mirror**: `src/pages/Messages.jsx:20-24` TYPE_MAP 模式
**Validate**: 点击消息后红点消失，badge 数字减少

### Task 7: 数据看板完善

**Action**: 确保首页和 Profile 的统计数据与 PRD 4.11 完全对齐

- 首页账单数据：待支付账单数+金额、逾期账单数+金额、有效押金总额
- 首页房间概况：总房间数、空置、已出租、即将到期（<=30天）
- 首页经营盈利：总收入、总支出、净盈利、盈利率
- 支持按时间维度筛选（今日/本月/本季/本年/自定义）
- Profile 补充经营数据卡片
- 修复 `getProfitStats` 中押金重复计算问题

**Mirror**: `src/data/mock.js:271-332` 派生统计函数
**Validate**: 统计数据与 PRD 4.11.1 要求一致

### Task 8: 最终验证

**Action**: 使用 Playwright 打开 Chrome 预览，逐项验证

- 启动 `vite` dev server
- 打开 Chrome 查看 H5 预览
- 逐页面验证：
  1. 登录流程（登录 -> 角色选择 -> 首页）
  2. 首页（问候、盈利卡、房间概况、账单数据、提醒）
  3. 房源管理（列表、房间展开、编辑/删除）
  4. 租客档案（列表、欠费标签）
  5. 收入账单（Tab切换、统计、标记已支付）
  6. 支出账单（10类枚举、统计）
  7. 合约管理（列表、状态操作）
  8. 消息中心（分类、未读标记）
  9. 详情页（房源、房间、账单）
  10. 个人中心（菜单导航）
  11. 租客视图（角色切换后）
- 检查 UI 一致性（颜色、间距、圆角、字体）
- 检查响应式（480px 容器内无溢出）

**Validate**: `npm run build` 成功，所有页面可正常导航

## Risks

| Risk | Likelihood | Mitigation |
|---|---|---|
| 模块级变量重构影响现有交互 | High | 先用 useState 替换，逐步迁移 |
| 新增表单页面增加路由复杂度 | Medium | 保持路由命名一致，使用懒加载 |
| 租客视图与房东视图代码重复 | Medium | 提取共享组件（StatCard, BillCard, ContractCard） |
| BottomNav Tailwind 自定义类名兼容 | Low | 降级为内联 style 兜底 |
| PRD 统计口径与 mock 函数不一致 | Medium | 以 PRD 6.2 验收标准为最终依据 |

## Acceptance

- [ ] Task 1: 设计系统统一（所有页面 COLOR 一致）
- [ ] Task 2: 所有已知 Bug 修复
- [ ] Task 3: 导航断裂点全部修复
- [ ] Task 4: 新增表单页面可用
- [ ] Task 5: 租客视图完整
- [ ] Task 6: 消息中心交互完善
- [ ] Task 7: 数据看板与 PRD 对齐
- [ ] Task 8: Playwright 全页面验证通过
- [ ] `npm run build` 成功无错误
- [ ] 所有页面在 480px 容器内无溢出
- [ ] 敏感信息脱敏展示（手机号、姓名、身份证）
