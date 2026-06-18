# Git 初始化与首次推送操作指南

本指南详细说明如何从零开始，将 Rent1 项目的代码推送到 GitHub，并建立起完整的基于 GitHub 的代码版本管理与协作能力。

> 适用环境：Windows 10/11、PowerShell 5.x、Git 2.30+

---

## 📋 目录

1. [环境准备](#1-环境准备)
2. [GitHub 仓库创建](#2-github-仓库创建)
3. [本地 Git 初始化](#3-本地-git-初始化)
4. [首次提交与推送](#4-首次提交与推送)
5. [分支策略设置](#5-分支策略设置)
6. [GitHub 仓库设置](#6-github-仓库设置)
7. [日常开发工作流](#7-日常开发工作流)
8. [常见问题排查](#8-常见问题排查)

---

## 1. 环境准备

### 1.1 安装 Git

**检查是否已安装：**

```powershell
git --version
```

如果输出 `git version 2.x.x` 说明已安装。

**如果未安装，按以下步骤操作：**

1. 访问 [Git 官网](https://git-scm.com/download/win) 下载 Windows 版本安装包
2. 运行安装程序，**全部选项保持默认即可**，一路点击 Next
3. 安装完成后，重新打开 PowerShell，执行 `git --version` 验证

### 1.2 配置 Git 用户信息

> ⚠️ 此步骤为 **必须**，否则提交时会报错

```powershell
# 配置用户名（建议使用 GitHub 用户名）
git config --global user.name "Your GitHub Username"

# 配置邮箱（建议使用 GitHub 注册邮箱，或 GitHub 提供的 noreply 邮箱）
git config --global user.email "your.email@example.com"

# 验证配置
git config --list
```

**查看当前配置：**

```powershell
git config --global --list
```

### 1.3 配置行结束符

解决 Windows（CRLF）与 Linux/Mac（LF）的跨平台差异：

```powershell
# 提交时转换为 LF，检出时保持原样（推荐）
git config --global core.autocrlf input

# 禁止在检出时自动转换为 CRLF
git config --global core.eol lf
```

### 1.4 GitHub 账号准备

1. 注册 [GitHub 账号](https://github.com/)（如已有可跳过）
2. 设置好个人头像和邮箱
3. （推荐）开启 [Two-Factor Authentication](https://github.com/settings/security)

### 1.5 配置 SSH 或 HTTPS 认证

**推荐方案 A：使用 SSH（无需每次输入密码）**

```powershell
# 1. 生成 SSH Key（一路回车即可，使用空密码）
ssh-keygen -t ed25519 -C "your.email@example.com"

# 2. 启动 ssh-agent
Start-Service ssh-agent
ssh-add $env:USERPROFILE\.ssh\id_ed25519

# 3. 复制公钥内容到剪贴板
Get-Content $env:USERPROFILE\.ssh\id_ed25519.pub | Set-Clipboard
```

然后：
- 访问 [GitHub SSH Keys 设置](https://github.com/settings/keys)
- 点击 **New SSH key**
- Title 填写：`Rent1-Work-PC`（便于识别）
- Key 类型：Authentication Key
- 粘贴刚才复制的公钥内容
- 点击 **Add SSH key**

**验证 SSH 连接：**

```powershell
ssh -T git@github.com
```

看到 `Hi username! You've successfully authenticated` 即成功。

**方案 B：使用 HTTPS + Personal Access Token（备用）**

如果 SSH 配置有问题，可以使用 HTTPS + Token：
- 访问 [GitHub Token 设置](https://github.com/settings/tokens)
- Generate new token → 勾选 `repo`、`workflow`、`admin:repo_hook` 权限
- 保存生成的 Token（只显示一次）
- 推送时输入 GitHub 用户名 + Token 作为密码

---

## 2. GitHub 仓库创建

### 2.1 创建新仓库

1. 登录 GitHub，点击右上角 `+` → **New repository**
2. 填写仓库信息：
   - **Repository name**：`rent1`
   - **Description**：房东租赁管理系统（Rent1）- 房源、合约、账单、押金一体化管理
   - **Public / Private**：按需选择（生产环境建议 Private）
   - **Initialize with**：全部 **不要勾选**（README、.gitignore、License 都在本地已有）
3. 点击 **Create repository**

### 2.2 记录仓库地址

创建成功后，复制仓库地址：
- SSH 格式：`git@github.com:your-username/rent1.git`
- HTTPS 格式：`https://github.com/your-username/rent1.git`

---

## 3. 本地 Git 初始化

在项目根目录（`C:\Users\Alex\rent1`）执行以下操作。

### 3.1 检查是否已初始化

```powershell
cd C:\Users\Alex\rent1
git status
```

如果看到 `fatal: not a git repository` 说明未初始化，继续下一步。

如果已初始化但想重新开始：

```powershell
# ⚠️ 谨慎使用：这会删除所有历史记录！
Remove-Item -Recurse -Force .git
```

### 3.2 初始化仓库

```powershell
cd C:\Users\Alex\rent1
git init
git branch -M main
```

### 3.3 添加远程仓库

```powershell
# 使用 SSH（推荐）
git remote add origin git@github.com:your-username/rent1.git

# 或者使用 HTTPS
# git remote add origin https://github.com/your-username/rent1.git

# 验证
git remote -v
```

看到 `origin  git@github.com:your-username/rent1.git (fetch/push)` 即成功。

---

## 4. 首次提交与推送

### 4.1 检查 .gitignore 是否生效

先查看哪些文件会被忽略：

```powershell
# 查看将要被忽略的文件列表
git status --ignored
```

确认以下目录被正确忽略（不会被提交）：
- `backend/target/`（Maven 编译产物）
- `admin-web/node_modules/`、`admin-web/dist/`
- `mobile-app/node_modules/`、`mobile-app/dist/`
- `.idea/`、`.vscode/`（IDE 配置）
- `*.log`（日志文件）

### 4.2 添加文件到暂存区

```powershell
# 添加所有文件（.gitignore 中定义的文件会自动排除）
git add .

# 查看将要提交的文件清单
git status
```

**检查是否有敏感文件：**
- ❌ 不要提交任何包含密钥、密码、Token 的文件
- ❌ 不要提交 `.env.local`、`.env.production` 等环境配置文件
- ✅ `application.yml` 中应使用 `${DB_HOST}` 等环境变量占位符

### 4.3 创建首次提交

```powershell
git commit -m "feat: 初始化项目 - 房东租赁管理系统 Rent1 v1.0.0"
```

### 4.4 推送到 GitHub

```powershell
git push -u origin main
```

`-u` 表示设置默认上游分支，后续只需 `git push` 即可。

### 4.5 验证推送成功

浏览器访问：`https://github.com/your-username/rent1`

应该能看到：
- README.md 正确显示
- 目录结构完整（backend、admin-web、mobile-app、.github 等）
- 刚刚的 commit 信息

---

## 5. 分支策略设置

### 5.1 创建开发分支 develop

```powershell
# 从 main 创建 develop 分支
git checkout -b develop main
git push -u origin develop
```

### 5.2 分支模型说明

```
main  ← 生产分支（受保护，仅允许通过 PR 合并）
 ↑
develop  ← 开发集成分支（受保护，团队日常开发合并目标）
 ↑
feature/*  bugfix/*  hotfix/*  ← 个人开发分支
```

### 5.3 分支命名规范速查

| 场景 | 命名示例 | 来源分支 | 合并目标 |
|------|---------|---------|---------|
| 新功能开发 | `feature/dashboard-enhance` | develop | develop |
| Bug 修复 | `bugfix/contract-end-date` | develop | develop |
| 线上紧急修复 | `hotfix/login-error` | main | main + develop |
| 版本发布准备 | `release/v1.1.0` | develop | main + develop |

### 5.4 日常开发分支操作示例

```powershell
# 1. 切换到 develop，确保是最新代码
git checkout develop
git pull origin develop

# 2. 创建新功能分支
git checkout -b feature/my-new-feature

# 3. 编写代码...
# 4. 提交代码
git add .
git commit -m "feat: 新增某个功能"

# 5. 推送到远程
git push -u origin feature/my-new-feature

# 6. 在 GitHub 页面发起 Pull Request（develop ← feature/my-new-feature）
```

---

## 6. GitHub 仓库设置

### 6.1 分支保护规则（重要！）

目的：防止直接 push 到 main/develop，确保代码经过 PR 审查。

操作路径：**Settings → Branches → Branch protection rule → Add rule**

**规则 1：保护 `main` 分支**
- Branch name pattern：`main`
- ☑️ Require a pull request before resolution
- ☑️ Require approvals（设置为 1 或更多）
- ☑️ Dismiss stale pull request approvals when new commits are pushed
- ☑️ Require status checks to pass before merging
- ☑️ Require branches to be up to date before merging
- ☑️ Include administrators（强制管理员也遵守规则）

**规则 2：保护 `develop` 分支**
- Branch name pattern：`develop`
- ☑️ Require a pull request before resolution
- ☑️ Require approvals（设置为 1）
- ☑️ Require status checks to pass before merging

### 6.2 启用 GitHub Actions

操作路径：**Settings → Actions → General**
- Actions permissions：Allow all actions and reusable workflows
- Fork pull request workflows from outside collaborators：Require approval
- Workflow permissions：Read and write permissions
- ☑️ Allow GitHub Actions to create and approve pull requests

### 6.3 设置 Issue 标签（Labels）

操作路径：**Issues → Labels**

推荐创建以下标签：
- `bug` — Bug 报告
- `enhancement` — 功能请求
- `documentation` — 文档相关
- `backend` — 后端相关
- `frontend` — 前端相关
- `mini-program` — 微信小程序相关
- `security` — 安全相关
- `needs-triage` — 需要归类
- `good first issue` — 适合新人

---

## 7. 日常开发工作流

### 7.1 标准开发流程

```
┌──────────────────────────────────────────────────────────┐
│  1. 同步 develop                                         │
│     git checkout develop                                 │
│     git pull origin develop                              │
├──────────────────────────────────────────────────────────┤
│  2. 创建分支                                             │
│     git checkout -b feature/xxx                          │
├──────────────────────────────────────────────────────────┤
│  3. 编写代码 & 本地测试                                  │
│     mvn clean test            (后端)                     │
│     npm run build               (前端)                   │
├──────────────────────────────────────────────────────────┤
│  4. 提交代码                                             │
│     git add .                                            │
│     git commit -m "feat(scope): 描述变更"                │
├──────────────────────────────────────────────────────────┤
│  5. 推送分支                                             │
│     git push -u origin feature/xxx                       │
├──────────────────────────────────────────────────────────┤
│  6. 发起 Pull Request                                    │
│     GitHub 页面 → Compare & pull request                 │
│     base: develop  ←  compare: feature/xxx               │
│     填写 PR 模板内容                                     │
├──────────────────────────────────────────────────────────┤
│  7. 等待 Code Review & CI 通过                           │
├──────────────────────────────────────────────────────────┤
│  8. 合并到 develop（Squash and merge）                   │
├──────────────────────────────────────────────────────────┤
│  9. 删除本地分支                                         │
│     git checkout develop                                 │
│     git branch -d feature/xxx                            │
└──────────────────────────────────────────────────────────┘
```

### 7.2 Commit Message 示例

```
feat(backend): 新增合约到期自动生成账单
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

### 7.3 Pull Request 合并策略推荐

- **功能分支 → develop**：使用 **Squash and merge**（将多个提交压缩为一个，保持历史简洁）
- **develop → main / release → main**：使用 **Merge commit**（保留完整历史，便于版本回溯）

---

## 8. 常见问题排查

### 8.1 `git push` 报错 Permission denied

**症状：**
```
git@github.com: Permission denied (publickey).
fatal: Could not read from remote repository.
```

**解决方案：**
```powershell
# 1. 检查 ssh-agent 是否运行
Get-Service ssh-agent
Start-Service ssh-agent

# 2. 重新添加 key
ssh-add $env:USERPROFILE\.ssh\id_ed25519

# 3. 测试连接
ssh -T git@github.com

# 4. 如果仍失败，检查 GitHub SSH Keys 页面是否已添加公钥
```

### 8.2 Git 显示所有文件为 modified（行结束符问题）

**症状：** `git status` 显示大量文件已修改，但实际内容未变。

**解决方案：**
```powershell
# 删除 Git 缓存
git rm -rf --cached .

# 重新添加（会应用 .gitattributes 规则）
git add .

# 重置为 HEAD（恢复暂存区与工作区一致）
git reset HEAD -- .
git checkout -- .
```

### 8.3 本地分支落后远程，无法 push

**症状：**
```
error: failed to push some refs to 'git@github.com:xxx/rent1.git'
hint: Updates were rejected because the tip of your current branch is behind
```

**解决方案：**
```powershell
# 先拉取远程最新代码
git pull origin <当前分支名>

# 如果有冲突，解决冲突后
git add <解决冲突的文件>
git commit -m "resolve: 合并冲突"
git push origin <当前分支名>
```

### 8.4 不小心提交了敏感文件（密钥等）

**紧急处理方案：**

```powershell
# 1. 立即在 GitHub 上重置密钥/Token
# 2. 从 Git 历史中清除敏感文件
# 方法 A：使用 git-filter-repo（推荐）
# 方法 B：联系项目管理员协助

# 预防：使用 .gitignore 排除敏感文件，使用环境变量注入配置
```

### 8.5 Windows PowerShell 中文乱码

**解决方案：**
```powershell
# 设置 UTF-8 编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$env:LC_ALL = 'C.UTF-8'
```

或者在 PowerShell 配置文件中永久设置：
```powershell
# 在 $PROFILE 中添加
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
```

### 8.6 查看提交历史

```powershell
# 简洁版
git log --oneline -10

# 图形版
git log --graph --oneline --all

# 详细版
git log --stat -5
```

### 8.7 撤销尚未推送的提交

```powershell
# 撤销最近一次提交，但保留修改内容
git reset --soft HEAD~1

# 撤销最近一次提交，并丢弃修改内容（⚠️ 小心使用）
git reset --hard HEAD~1
```

---

## 📚 参考资源

- [Git 官方文档](https://git-scm.com/docs)
- [GitHub 文档](https://docs.github.com/)
- [Pro Git 电子书（中文版）](https://git-scm.com/book/zh/v2)
- [Conventional Commits 规范](https://www.conventionalcommits.org/zh-cn/v1.0.0/)
- [Semantic Versioning 2.0.0](https://semver.org/lang/zh-CN/)

---

**遇到其他问题？** 请提交 [GitHub Issue](https://github.com/your-username/rent1/issues) 或参考项目 [CONTRIBUTING.md](CONTRIBUTING.md) 贡献指南。
