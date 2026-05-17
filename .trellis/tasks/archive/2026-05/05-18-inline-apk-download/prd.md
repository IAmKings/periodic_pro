# 应用内下载APK — 实时进度 + 失败重试

## Goal

弃用 DownloadManager，改用应用内 HttpURLConnection 下载 APK，实时回调进度到 UI 层。

## Requirements

- `ApkInstaller.downloadAndInstall()` 改为 suspend 函数，onProgress 回调
- 用 `HttpURLConnection` 流式读取写入文件，每读一块回调进度
- `UpdateState` 加 `downloadProgress: Float`（0f-1f，-1f 表示未下载）
- `UpdateDialog` 下载中显示进度条 + 百分比
- 下载失败显示 Snackbar，不关闭弹窗，用户可重试
- 下载完成后走 FileProvider 安装（保持现有逻辑）

## Acceptance Criteria

- [ ] 下载进度在 UpdateDialog 中实时显示
- [ ] 下载失败弹 Snackbar 提示，可重新点击"立即更新"重试
- [ ] 下载完成后自动调起安装
- [ ] 移除 DownloadManager 相关代码

## Out of Scope

- 断点续传
- 后台下载
- 多任务并行下载

## Technical Notes

- 文件：`ApkInstaller.kt`, `UpdateService.kt`, `UpdateDialog.kt`
- 保持无第三方依赖，用 `HttpURLConnection`
