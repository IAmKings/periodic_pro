# Stage 8: 抛光与质量

## Goal

detekt 配置 + 关键单元测试（ElementRepository / FavoritesRepository / TableViewModel / DetailViewModel）+ README 文档。MVP 收尾。

## 父任务

`.trellis/tasks/05-10-compose-app-bootstrap`（Stage 7 已完成：5 屏合龙）

## Requirements

### 1. detekt 配置
- 项目根添加 `detekt-config.yml`（使用 detekt 默认规则 + formatting 插件）
- `app/build.gradle.kts` 添加 detekt plugin
- `./gradlew detekt` 通过（或至少 0 严重错误）

### 2. 单元测试 (`app/src/test/`)

**ElementRepositoryTest:**
- 测试从 assets 加载 118 元素成功
- 测试 `getByNumber(1)` 返回 Hydrogen
- 测试 `search("氢")` 返回氢元素
- 测试 `search("fe")` 返回铁元素
- 使用 MockK mock Context + AssetManager

**FavoritesRepositoryTest:**
- 测试 toggle 添加/移除收藏
- 测试 favoritesFlow 发射正确数据集
- Mock DataStore

**TableViewModelTest:**
- 测试搜索过滤逻辑
- 测试分类筛选逻辑
- 测试多选模式进入/退出

**DetailViewModelTest:**
- 测试加载元素成功
- 测试收藏切换

### 3. README.md

项目根添加 `README.md`：
- 项目简介：周期表 Pro — Android Jetpack Compose 客户端
- 技术栈：Kotlin / Compose / M3 / Koin / DataStore
- 运行方式：`./gradlew assembleDebug`
- 目录结构简要说明
- 设计资产引用：`design/`
- 5 屏功能列表
- CC BY-SA 3.0 数据署名

---

## Acceptance Criteria

- [ ] `./gradlew detekt` 通过
- [ ] 单元测试 ≥ 8 个，全部通过 (`./gradlew test`)
- [ ] `./gradlew assembleDebug lint` 通过
- [ ] `README.md` 完整

## Out of Scope
- Compose UI 测试（androidTest）
- CI/CD 配置
- 覆盖率报告（只跑 test，不做 jacoco）
