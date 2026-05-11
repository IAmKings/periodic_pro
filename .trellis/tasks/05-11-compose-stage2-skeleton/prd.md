# Stage 2: Android 工程骨架搭建

## Goal

从零搭建可编译的 Android Jetpack Compose 工程骨架：Gradle KTS + Version Catalog + 全部依赖接入 + Theme 系统 + 空 NavHost，`./gradlew assembleDebug` 通过。

## 父任务

`.trellis/tasks/05-10-compose-app-bootstrap` — 周期表 App Compose 客户端规划（Stage 1 已完成：frontend spec 改写）

## Requirements

### 1. Gradle 工程骨架

- 仓库根即 Android 工程根
- Gradle 8.7+ / Kotlin 2.0+ / AGP 8.5+
- Kotlin DSL (`settings.gradle.kts` + `build.gradle.kts`)
- Version Catalog (`gradle/libs.versions.toml`)
- Compose Compiler Plugin（Kotlin 2.0+ 用 `org.jetbrains.kotlin.plugin.compose`，不再用 KSP）
- minSdk 24 / targetSdk 35 / compileSdk 35
- Java 17
- 单 module：`app/`

### 2. 依赖接入（Version Catalog 统一管理）

| 类别 | 库 | 版本 |
|------|---|------|
| Kotlin | stdlib / coroutines / serialization-json | 2.0+ / 1.9+ / 1.7+ |
| Compose | compose-bom | 2024.10+ |
| Material | material3 / material-icons-extended | 1.3+ |
| Adaptive | material3-adaptive-navigation-suite | 1.0+ |
| Navigation | navigation-compose (type-safe) | 2.8+ |
| Lifecycle | lifecycle-runtime-compose / viewmodel-compose | 2.8+ |
| DI | Koin Compose | 4.0+ |
| Persistence | DataStore Preferences | 1.1+ |
| 手势缩放 | me.saket.telephoto:zoomable | 0.13+ |
| 毛玻璃 | dev.chrisbanes.haze:haze | 1.7.2 |
| 测试 | JUnit4 / MockK / turbine / compose-ui-test | latest |

### 3. Theme 系统

基于 `design/design_system.PNG` 和 `.trellis/spec/frontend/component-guidelines.md` 第 4 节（Glass Blur 红线），创建 `app/src/main/java/com/periodic/pro/theme/`：

| 文件 | 内容 |
|------|------|
| `Color.kt` | Primary/Secondary/Tertiary + **CategoryColors data class**（10 分类色板，独立于 M3 ColorScheme）+ 深色降饱和 |
| `Typography.kt` | M3 Typography（displayLarge → 57sp 原子序号 / headlineMedium → 28sp 标题 / titleLarge → 22sp 元素符号 / bodyLarge → 16sp 属性值 / bodyMedium → 14sp 标签 / labelMedium → 12sp Chips） |
| `Shapes.kt` | M3 Shapes（extraSmall=4dp / small=8dp / medium=12dp / large=16dp / extraLarge=24dp） |
| `Dimensions.kt` | 8pt Grid 常量（Dp0/Dp4/Dp8/Dp12/Dp16/Dp20/Dp24/Dp32/Dp40/Dp48/Dp56/Dp64） |
| `Elevation.kt` | 4 层 Elevation token（Shadow1=1dp / Shadow2=3dp / Floating=6dp / Modal=8dp） |
| `PeriodicProTheme.kt` | 深/浅 ColorScheme + `PeriodicProTheme` Composable 包装，`LocalCategoryColors` 通过 `staticCompositionLocalOf` 注入 |

### 4. 入口文件

| 文件 | 内容 |
|------|------|
| `App.kt` | `Application` 子类，Koin `startKoin { modules(appModule) }` |
| `MainActivity.kt` | `setContent { PeriodicProTheme { PeriodicNav() } }`，`enableEdgeToEdge()` |

### 5. 导航骨架

- `ui/navigation/Routes.kt`：`@Serializable` sealed interface 路由定义（Home / Table / Detail / Compare / Favorites 5 个 route + Detail 带 `atomicNumber: Int` 参数）
- `ui/navigation/PeriodicNav.kt`：`NavHost` + 5 个 route 的 `composable()` 占位，每屏一个 `Text("屏面名称")`
- `ui/navigation/PeriodicNavSuite.kt`：`NavigationSuiteScaffold` + `currentWindowAdaptiveInfo().windowSizeClass`，BottomBar/NavRail/NavDrawer 自动切换，5 个 `NavigationSuiteItem`

### 6. GlassSurface 组件

- `ui/components/GlassSurface.kt`：基于 Haze 1.7.2 的毛玻璃包装
- 通过 `LocalHazeState` 获取 HazeState
- API 31+ RenderEffect / API 24-30 半透明 Scrim 降级

---

## Acceptance Criteria

- [ ] `./gradlew assembleDebug` 通过（0 错误 0 警告）
- [ ] Version Catalog 管理全部依赖版本
- [ ] Theme 系统：深/浅色切换生效，`Color.kt` / `Typography.kt` / `Shapes.kt` / `Dimensions.kt` / `Elevation.kt` 5 文件全部落地
- [ ] `CategoryColors` 通过 `staticCompositionLocalOf` 独立注入（不混入 M3 ColorScheme）
- [ ] `PeriodicProTheme` 包装：`MaterialTheme` + `LocalCategoryColors` + 深色自动降饱和
- [ ] Hello Compose：5 屏占位路由可达，BottomBar/NavRail 切换正常
- [ ] `GlassSurface.kt` 组件可渲染（至少一个毛玻璃示例在 Home 屏）
- [ ] App 入口 `startKoin` 初始化通过

## Definition of Done

- `./gradlew lint` 通过
- detekt 可在后续 Stage 接入（本 Stage 不做强制，`quality-guidelines.md` 已写了 detekt 规范）
- 代码遵循 `component-guidelines.md`：modifier 第一参数、State Hoisting、Slot API

## Out of Scope

- 118 元素 JSON 数据集（Stage 3）
- ElementCard/CategoryChip/PeriodicSearchBar 等业务组件（Stage 3）
- 周期表网格布局（Stage 4）
- Element Detail / Compare / Favorites 真实 UI（Stage 5/6/7）
- 单测（Stage 8）
- detekt 正式配置（本 Stage 仅保证 lint 通过）

## Technical Approach

### 工程结构产出目录树

```
.
├── build.gradle.kts              // 根 build（plugins + 全局配置）
├── settings.gradle.kts            // 单 module + version catalog 声明
├── gradle/
│   ├── libs.versions.toml         // Version Catalog（全部依赖版本）
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew / gradlew.bat
├── gradle.properties              // JVM 17 / heap / android.useAndroidX
└── app/
    ├── build.gradle.kts           // plugins + android config + dependencies
    └── src/
        └── main/
            ├── AndroidManifest.xml
            ├── java/com/periodic/pro/
            │   ├── App.kt
            │   ├── MainActivity.kt
            │   ├── theme/
            │   │   ├── Color.kt
            │   │   ├── Typography.kt
            │   │   ├── Shapes.kt
            │   │   ├── Dimensions.kt
            │   │   ├── Elevation.kt
            │   │   └── PeriodicProTheme.kt
            │   ├── ui/
            │   │   ├── components/
            │   │   │   └── GlassSurface.kt
            │   │   └── navigation/
            │   │       ├── Routes.kt
            │   │       ├── PeriodicNav.kt
            │   │       └── PeriodicNavSuite.kt
            │   └── feature/
            │       └── (空目录，仅 package-info)
            └── res/
                ├── values/
                │   ├── strings.xml
                │   └── themes.xml
                └── mipmap-*/ (launcher icons)
```

### package 名

`com.periodic.pro`

### Gradle Wrapper

必须生成 `gradlew` + `gradle-wrapper.properties`（Gradle 8.7），运行 `gradle wrapper` 自动生成。

---

## Decision (ADR-lite)

> 继承父任务 ADR-lite 全部 10 项决策，本 Stage 无新增决策。

## Technical Notes

- 设计资产参考：`design/README.md`
- Spec 参考：`.trellis/spec/frontend/*.md`（已改写为 Compose 规范）
- 研究参考：`.trellis/tasks/05-10-compose-app-bootstrap/research/*.md`
- **关键**：不要用 Android Studio 模板生成，全部手动搭建（KISS：只引真正需要的库）
- `gradle wrapper` 生成后在项目根执行，确保 `gradlew` 存在于仓库根
