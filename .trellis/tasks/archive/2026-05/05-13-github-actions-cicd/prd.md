# GitHub Actions CI/CD: push编译 + tag发布Release

## Goal

实现 GitHub Actions 自动化流水线：
1. **push 触发**：自动编译检查（assembleDebug + lint + detekt）
2. **tag 推送触发**：自动编译 release APK + 签名 + 发布到 GitHub Release

## Decision (ADR-lite)

**两文件方案**：
- `.github/workflows/ci.yml` — push/PR 触发，编译+lint+detekt
- `.github/workflows/release.yml` — tag 触发（`v*`），编译release+发布

**签名策略**：release 用 GitHub Secrets 存储 keystore base64 + 密码

## Requirements

### 1. CI 流水线 (`ci.yml`)

```yaml
触发：push 到任意分支 + PR
Job:
  - 检出代码
  - 设置 JDK 17
  - 缓存 Gradle
  - ./gradlew assembleDebug lint detekt
  - 上传 lint/detekt 报告为 artifact
```

### 2. Release 流水线 (`release.yml`)

```yaml
触发：tag push（匹配 v*，如 v0.1.0）
Job:
  - 检出代码
  - 设置 JDK 17
  - 解码 keystore（从 GitHub Secrets）
  - ./gradlew assembleRelease
  - 创建 GitHub Release + 上传 APK
```

### 3. Release APK 签名配置

修改 `app/build.gradle.kts`，新增 `signingConfigs`：

```kotlin
signingConfigs {
    create("release") {
        storeFile = rootProject.file("release.keystore")
        storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
        keyAlias = System.getenv("KEY_ALIAS") ?: ""
        keyPassword = System.getenv("KEY_PASSWORD") ?: ""
    }
}
buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        // ... existing config
    }
}
```

### 4. GitHub Secrets 清单（用户配置）

| Secret | 说明 |
|--------|------|
| `KEYSTORE_BASE64` | `base64 release.keystore` |
| `KEYSTORE_PASSWORD` | keystore 密码 |
| `KEY_ALIAS` | 密钥别名 |
| `KEY_PASSWORD` | 密钥密码 |

### 5. keystore 生成指南

给用户提供一键命令生成 keystore：
```bash
keytool -genkey -v -keystore release.keystore -alias periodicpro \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass <密码> -keypass <密码> \
  -dname "CN=PeriodicPro, OU=Dev, O=PeriodicPro, L=City, ST=State, C=CN"
```

## Acceptance Criteria

- [ ] `ci.yml` 存在，语法正确
- [ ] `release.yml` 存在，语法正确
- [ ] `build.gradle.kts` 签名配置正确（环境变量注入）
- [ ] `release.keystore` 加入 `.gitignore`
- [ ] `README.md` 或文档说明 GitHub Secrets 配置步骤
- [ ] workflow 文件不含硬编码敏感信息
- [ ] 使用 JDK 17（与项目一致）
- [ ] Gradle 缓存加速构建

## Definition of Done

* 两个 workflow 文件语法正确，可被 GitHub Actions 解析
* 签名配置通过环境变量，不留硬编码
* keystore 已加入 .gitignore
* 文档说明配置步骤清晰

## Out of Scope

* Play Store 发布
* 自动化测试（androidTest — 当前项目未配置）
* 代码覆盖率
* 多环境部署

## Technical Notes

* Gradle JDK：17（与 `compileOptions.jvmTarget = "17"` 一致）
* actions/setup-java@v4
* gradle/actions/setup-gradle@v3（缓存）
* softprops/action-gh-release@v2（创建 Release）
* APK 文件名：`PeriodicPro-v{version}.apk`
