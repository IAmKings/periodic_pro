# 自动更新: App内下载 + 自动安装

## Goal

1. App 启动时 / Settings 手动 → 检测 GitHub Release 最新版本
2. 有新版本 → App 内下载 APK → 自动调起系统安装
3. Android 7+ FileProvider 适配 + Android 8+ 未知来源安装权限

## Decision (ADR-lite)

**下载方式**：Android `DownloadManager` 下载 APK 到公共 Downloads 目录
**安装方式**：`FileProvider` 提供 content URI → `Intent.ACTION_VIEW` 调起系统安装器
**版本比较**：`BuildConfig.VERSION_NAME` vs `release.tag_name`（去 `v` 前缀，语义化比较）

## Requirements

### 1. Android 配置

**AndroidManifest.xml 新增权限**：
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
```

**AndroidManifest.xml 新增 FileProvider**：
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

**新增 `res/xml/file_paths.xml`**：
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-path name="downloads" path="Download/" />
    <cache-path name="cache" path="/" />
</paths>
```

### 2. 数据层 (`data/update/`)

**GitHubRelease 模型**：
```kotlin
@Serializable
data class GitHubRelease(
    val tag_name: String,      // "v0.1.1"
    val name: String,          // "Periodic Pro v0.1.1"
    val html_url: String,      // Release 页面 URL
    val body: String,          // Release 说明
    val prerelease: Boolean,
    val assets: List<Asset>,   // APK 文件列表
)
@Serializable
data class Asset(
    val name: String,          // "PeriodicPro-v0.1.1.apk"
    val browser_download_url: String,
    val size: Long,
)
```

**UpdateRepository**：
```kotlin
class UpdateRepository {
    suspend fun checkUpdate(): UpdateResult  // HTTP GET + JSON parse + 版本比较
}
```

### 3. 下载安装器 (`data/update/ApkInstaller.kt`)

```kotlin
class ApkInstaller(private val context: Context) {
    fun downloadAndInstall(release: GitHubRelease)
    // ① DownloadManager.enqueue() 下载 APK
    // ② BroadcastReceiver 监听下载完成
    // ③ FileProvider.getUriForFile() 获取 content URI
    // ④ Intent.ACTION_VIEW + FLAG_GRANT_READ_URI_PERMISSION 调安装
}
```

### 4. Feature 层

**Profile 屏改造**：
- 版本号 `BuildConfig.VERSION_NAME` 替代硬编码 `"0.1.0"`
- "检查更新" 按钮：检测中显示 `CircularProgressIndicator`，结果反馈
- 已最新 → Snackbar "已是最新版本"
- 有新版本 → UpdateDialog 弹窗
- 网络错误 → Snackbar 提示

**启动自动检测**：`MainActivity` LaunchedEffect 启动后 2 秒检测（一天内不重复弹窗，`SharedPreferences` 存上次检测日期）

### 5. UI 组件 (`ui/components/UpdateDialog.kt`)

```
┌──────────────────────────────────┐
│  🎉 发现新版本                    │
│                                  │
│  最新版本：v0.2.0                │
│  当前版本：v0.1.1                │
│  文件大小：3.2 MB                │
│                                  │
│  更新内容：                      │
│  - 修复了 xxx                    │
│  - 新增了 yyy                    │
│                                  │
│  [稍后再说]      [立即更新]      │
└──────────────────────────────────┘
```

- `立即更新` → `ApkInstaller.downloadAndInstall()`
- `稍后再说` → dismiss

## Acceptance Criteria

- [ ] `AndroidManifest.xml` 权限 + FileProvider 配置正确
- [ ] `res/xml/file_paths.xml` 存在
- [ ] `data/update/` UpdateRepository + ApkInstaller 实现
- [ ] `ui/components/UpdateDialog.kt` 弹窗组件
- [ ] Profile 版本号 `BuildConfig.VERSION_NAME`（修复硬编码）
- [ ] Profile "检查更新" 按钮 + 三种状态（检测中/已最新/有新版本）
- [ ] App 启动 2 秒后自动检测（一天最多弹一次）
- [ ] Android 8+ 安装权限引导
- [ ] `./gradlew assembleDebug lint` 通过

## Out of Scope

- 下载进度条（DownloadManager 自带通知栏进度）
- 增量更新 / 差分包
- 后台静默安装（需 ROOT）
- Play Store 跳转

## Technical Notes

- `INTERNET` 权限：AndroidManifest 新增
- `REQUEST_INSTALL_PACKAGES`：Android 8+ 安装未知来源 APK 必须
- `WRITE_EXTERNAL_STORAGE` maxSdkVersion=28：Android 10+ Scoped Storage 不需要
- 下载路径：`Environment.DIRECTORY_DOWNLOADS / PeriodicPro-vX.X.X.apk`
- 版本比较：拆 `major.minor.patch` 三段比较
