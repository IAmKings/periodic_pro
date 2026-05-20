# 更新 README.md 文档

## Goal

README.md 严重过时——缺少半年来新增的学习资料、实验室、测验、自动更新、分类浏览、二级页面导航等核心功能。更新至版本 0.3.1 的实际项目状态。

## What I already know

- 项目版本 `0.3.1`，versionCode 16
- 现有 README 功能列表只列了 7 项，缺少 6+ 个核心功能
- 技术栈部分：手势已从 Telephoto 改为原生 Compose；导航改为双层 NavHost
- 目录结构遗漏了 8 个 feature 包和 4 个 data 子包
- detekt 已从 CI 移除

## Requirements

### 功能列表更新

新增：
- 自动更新 — GitHub Release + 应用内下载安装 + 进度对话框
- 学习资料 — 118 元素学习卡片 + 查看详情 + 搜索
- 实验室 — 化学反应库 + 实验详情 + 涉及元素跳转
- 元素知识测验 — 4 种题型（符号/名称/分类/原子序数）+ 分数统计
- 分类浏览 — 10 种元素分类快捷入口
- 发现页 — 分类/性质/应用多维度探索
- 详情页二级页面 — 全屏滑动动画，支持 Learn/Lab 连续跳转

删除 (手势相关)：
- "支持缩放/平移" → 改为 "支持横向滚动 + 长按多选"

### 技术栈更新

| 项目 | 旧 | 新 |
|------|----|----|
| 手势 | Telephoto Zoomable | 原生 Compose horizontalScroll + combinedClickable |
| 导航 | type-safe routes | 双层 NavHost（root 全屏 + inner Tab）+ type-safe routes |
| 新增 | - | Adaptive Navigation Suite (NavigationSuiteScaffold) |

### 目录结构更新

新增：
- feature/discover/、feature/lab/、feature/learn/、feature/quiz/、feature/category/、feature/profile/
- data/update/、data/discover/、data/lab/、data/learn/、data/theme/、data/permission/

### 版本

在标题下方加版本号：`v0.3.1`

### CI/CD

移除 detekt 引用（已从 CI 流程中移除）

## Acceptance Criteria

- [ ] 功能列表覆盖全部 13 个功能点
- [ ] 技术栈反映当前实际依赖（Telephoto 保留但手势已原生化）
- [ ] 目录结构匹配实际 `find` 输出
- [ ] 版本号准确
- [ ] CI/CD 描述与实际 ci.yml 一致

## Out of Scope

- 不新增 design/ 设计图
- 不修改代码文件
