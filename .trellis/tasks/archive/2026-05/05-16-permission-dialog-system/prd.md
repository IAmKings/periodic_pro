# 设计应用权限弹出框系统

## Goal

设计一套统一的应用权限弹出框系统，处理 Android 运行时权限的请求、解释和拒绝场景。

## What I already know

- 当前声明了 4 个权限：INTERNET、ACCESS_NETWORK_STATE、WRITE_EXTERNAL_STORAGE、REQUEST_INSTALL_PACKAGES
- WRITE_EXTERNAL_STORAGE 仅限 API ≤ 28（无需运行时请求）
- INTERNET + ACCESS_NETWORK_STATE 无需运行时请求
- REQUEST_INSTALL_PACKAGES 需要通过 `Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES` 引导
- 项目当前无任何运行时权限请求逻辑
- 关于存储权限：Android 10+ 用 Scoped Storage，不需要 WRITE_EXTERNAL_STORAGE
- ApkInstaller 中已有未知来源安装权限的引导逻辑

## Decision (ADR-lite)

**Context**: 需要统一的权限弹窗系统，Material 3 风格，支持未来扩展。
**Decision**: 三层架构 — `PermissionDef`(数据定义) + `PermissionsManager`(Koin单例) + `PermissionDialog`(Compose组件)。
**Consequences**: 新增权限只需加一个 `PermissionDef` 定义项，Dialog 和 Manager 无需改动。

## Requirements

### PermissionDef — 权限定义
- `permission: String` — Android 权限名
- `title: String` — 弹窗标题
- `description: String` — 为什么需要此权限
- `deniedTitle: String` — 被永久拒绝后的引导标题
- `deniedDescription: String` — 被永久拒绝后的引导文案
- `icon: ImageVector` — 权限图标

### PermissionsManager — Koin 单例
- `check(permission)` — 检查是否已授权
- `request(permission)` — 请求权限（首次直接弹窗，二次显示 rationale）
- `shouldShowRationale(permission)` — 是否需要解释
- `navigateToSettings(context)` — 引导到系统设置页
- 持有 `StateFlow<PermissionRequest?>` 供 UI 观察

### PermissionDialog — Compose 组件
- Material 3 AlertDialog，由 PermissionDef 数据驱动
- 首次弹窗：解释 + 确认/取消
- 被拒后弹窗：引导到设置 + 确认/取消

## Acceptance Criteria

- [ ] PermissionDef 数据类清晰可扩展
- [ ] PermissionsManager 处理 check / request / rationale / settings 完整流程
- [ ] PermissionDialog 首次解释 + 被拒引导 两套文案
- [ ] 新增权限只需加 PermissionDef，无需改 Manager/Dialog

## Out of Scope

- 系统权限底层实现（如 FileProvider 配置）
- 通知权限的 NotificationChannel 创建

## Technical Notes

- AndroidManifest.xml: INTERNET, ACCESS_NETWORK_STATE, WRITE_EXTERNAL_STORAGE(≤28), REQUEST_INSTALL_PACKAGES
- ApkInstaller.kt: 已有 `ACTION_MANAGE_UNKNOWN_APP_SOURCES` 引导
