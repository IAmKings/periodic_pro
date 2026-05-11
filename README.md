# 周期表 Pro (Periodic Pro)

一款基于 Jetpack Compose 的交互式化学元素周期表 Android 应用。

## 功能

- **周期表**：标准 18x9 网格布局，支持缩放/平移，分类筛选
- **元素详情**：118 元素完整属性 + Canvas 2D 原子模型动画
- **元素对比**：多选元素横向属性对比表
- **收藏**：DataStore 持久化，Swipe 左滑删除
- **搜索**：按名称/符号/序号/中文/拼音模糊搜索
- **自适应**：手机(BottomBar)/折叠(NavRail)/平板(NavDrawer) 自动切换
- **深色/浅色主题**：一键切换

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin 2.0+ |
| UI | Jetpack Compose + Material 3 |
| 导航 | Navigation Compose + type-safe routes |
| DI | Koin 4.0 |
| 持久化 | DataStore Preferences |
| 手势 | Telephoto Zoomable |
| 毛玻璃 | Haze 1.6 |
| 测试 | JUnit4 + MockK + turbine |

## 运行

```bash
./gradlew assembleDebug
```

最低 Android 7.0 (API 24)，目标 Android 15 (API 35)。

## 目录结构

```
app/src/main/java/com/periodic/pro/
├── App.kt / MainActivity.kt      # 应用入口
├── theme/                         # 主题 (Color/Typography/Shapes/Dimensions/Elevation)
├── data/                          # 数据层
│   ├── element/                   # 元素数据 (Repository + JSON 解析)
│   └── favorites/                 # 收藏 (DataStore)
├── domain/                        # 领域层 (UseCase)
├── di/                            # Koin DI 模块
├── ui/
│   ├── components/                # 全局组件 (ElementCard/PeriodicButton/...)
│   ├── pattern/                   # 页面模式 (AtomCanvas/PropertyGrid/EmptyState)
│   └── navigation/                # 导航 (Routes/NavHost/NavSuite)
├── feature/
│   ├── home/                      # 首页
│   ├── table/                     # 周期表
│   ├── detail/                    # 元素详情
│   ├── compare/                   # 元素对比
│   └── favorites/                 # 收藏
└── assets/
    ├── elements.json              # 118 元素数据 (英文)
    └── elements_zh.json           # 中文名/拼音
```

## 数据来源

元素数据来自 [Bowserinator/Periodic-Table-JSON](https://github.com/Bowserinator/Periodic-Table-JSON)，基于 [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/) 许可。

## 设计资产

`design/` 目录包含 4 张高保真设计图（Design System / Components / Patterns / Screens）和解析文档 `design/README.md`。

## License

元素数据：CC BY-SA 3.0

应用代码：MIT
