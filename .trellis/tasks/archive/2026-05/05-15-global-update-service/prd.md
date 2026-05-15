# 更新功能升级为全局服务

## Goal

将现有的"检查更新"功能升级为全局更新服务：应用启动自动检测，弹窗交互增强，设置页联动，红点提示，并发去重。

## Requirements

### 1. 全局更新服务（UpdateService）

- 应用启动时自动检查 GitHub Release
- 全局单例，管理更新状态（检查中 / 有更新 / 无更新 / 出错）
- 同一天最多自动弹窗一次（保留现有逻辑）
- "跳过此版本"后该版本不再弹窗
- "稍后提示"后当天不再弹窗

### 2. 弹窗交互增强

- 外部点击/X按钮可取消关闭
- 底部独立 `TextButton`：**跳过此版本**（永久跳过该版本号）
- `dismissButton`：**稍后提示**（当天不再弹）
- `confirmButton`：**立即更新**（Primary 填充）

布局顺序（自上而下）：
```
      跳过此版本            ← 底部独立 TextButton
   [稍后提示]  [立即更新]   ← dismissButton + confirmButton
```

### 3. 设置页联动

- 有新版本时：
  - Tab 处**红点提示**（Badge）
  - "检查更新"按钮文案变为**"版本升级"**
  - 按钮样式变更（可用 outlined/其他变体）
- 无新版本时：保持"检查更新"按钮

### 4. 并发处理

- 全局检查进行中时，用户切换到设置页点击 → **取消当前请求，重新发起**
- 全局弹窗和设置页手动检查**不会重复弹窗**（已弹窗或已跳过时，设置页只显示结果不弹窗）

## Acceptance Criteria

- [ ] 冷启动 2s 后自动检查更新，有新版弹窗
- [ ] 弹窗：取消 / 跳过此版本 / 稍后提示 / 立即更新 四个操作
- [ ] 外部点击可关闭弹窗
- [ ] "跳过此版本"持久化，该版本不再提示
- [ ] "稍后提示"当天不再弹窗
- [ ] 有新版时设置 Tab 显示红点
- [ ] 有新版时"检查更新"按钮变"版本升级"
- [ ] 并发检查去重：全局检查中 → 设置页点击 → 重新发起
- [ ] 已弹窗后设置页手动检查不重复弹窗

## Definition of Done

- UpdateService 单例 + 全局状态管理
- UpdateDialog 交互增强
- ProfileScreen/ViewModel 联动红点+按钮文案
- 导航 Tab 红点 Badge
- 编译通过

## Decision (ADR-lite)

**Context**: 更新状态需在 MainActivity（弹窗）、ProfileScreen（按钮/红点）、导航 Tab（Badge）间共享。
**Decision**: Koin 单例 `UpdateService` 持有 `StateFlow<UpdateState>`，所有消费者观察同一 flow。并发时 cancel 旧协程重新发起。
**Consequences**: 新增 `UpdateService` 类 + `UpdateState` 数据类；MainActivity/ProfileViewModel 改为观察 flow 而非直接调用 Repository；DI 注册 `single { UpdateService(get(), androidContext()) }`。

## Out of Scope

- 后台定时检查（仅启动时检查）
- GitHub 镜像代理（另一任务）

## Technical Notes

- 现有文件：`UpdateRepository.kt`、`ProfileViewModel.kt`、`ProfileScreen.kt`、`UpdateDialog.kt`、`MainActivity.kt`
- 现有 AutoUpdateHost 在 MainActivity 中，需保留并增强
- 需要 DataStore 持久化 skippedVersion + lastPromptDate
- 需要全局 StateFlow 共享更新状态
- 红点需要导航层支持
