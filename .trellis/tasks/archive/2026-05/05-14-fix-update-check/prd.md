# 修复更新检查功能报错

## Goal

修复 Profile 页"检查更新"按钮点击后直接报错、无法获取 GitHub Release 信息的问题。

## Root Cause

`UpdateRepository.kt` 使用 `java.net.URL(urlString).readText()` 发起 HTTP GET 请求：
1. **未设置 User-Agent header** — GitHub API 强制要求，无 UA 返回 403 Forbidden
2. **无超时配置** — 网络慢时可能卡死
3. **未处理非 200 状态码** — `readText()` 读到的可能是错误响应体，JSON 解析抛异常但错误信息不友好

## Requirements

- 使用 `HttpURLConnection` 替换 `readText()`，手动设置：
  - `User-Agent: PeriodicPro/<version>`
  - `Accept: application/vnd.github+json`
  - `connectTimeout = 10000`、`readTimeout = 10000`
- 检查 HTTP 响应码，非 200 时读取错误体并返回明确的 `UpdateResult.Error`
- 保留 `Dispatchers.IO` 线程调度
- 保留现有错误处理和日志

## Acceptance Criteria

- [ ] 点击"检查更新"按钮能正常获取 GitHub Release 信息
- [ ] 无新版本时显示"已是最新版本" Snackbar
- [ ] 有新版本时弹出 UpdateDialog
- [ ] 网络不可用时显示友好错误提示
- [ ] GitHub API 限流时显示明确错误信息（403 响应码提示）

## Definition of Done

- `UpdateRepository.kt` 使用 `HttpURLConnection` + 正确 headers
- 编译通过
- 功能验证正常

## Out of Scope

- OkHttp/Retrofit 引入（保持零依赖）
- 自动重试机制
- GitHub Token 认证（增加 API 限额）

## Technical Notes

- 文件：`app/src/main/java/com/periodic/pro/data/update/UpdateRepository.kt`
- GitHub API URL：`https://api.github.com/repos/IAmKings/periodic_pro/releases/latest`
- 当前 `readText()` 在 `Dispatchers.IO` 中运行（NetworkOnMainThread 已修复）
- GitHub API 未认证限流：60 req/hour
