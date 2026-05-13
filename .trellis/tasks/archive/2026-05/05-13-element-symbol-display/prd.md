# fix: 元素序号→符号显示

## Goal

Learn / Discover / Lab 屏列表中将 `#${atomicNumber}` 改为显示元素符号（如 H、He、Li），一目了然。

## 修改范围

| 文件 | 位置 | 当前 | 目标 |
|------|------|------|------|
| LearnViewModel.kt | 构造函数 + loadData | 无 ElementRepository | 注入 + 构建 symbolMap |
| LearnUiState.kt | 状态 | 无 symbolMap | 新增 `symbolMap: Map<Int, String>` |
| LearnScreen.kt | L313 | `"#${item.atomicNumber}"` | `symbolMap[item.atomicNumber] ?: "?"` |
| DiscoverViewModel.kt | 同上 | 同上 | 同上 |
| DiscoverUiState.kt | 同上 | 同上 | 同上 |
| DiscoverScreen.kt | L229 | `"#${item.atomicNumber}"` | `symbolMap[item.atomicNumber] ?: "?"` |
| LabViewModel.kt | 同上 | 同上 | 同上 |
| LabUiState.kt | 同上 | 同上 | 同上 |
| LabScreen.kt | L719 | `"#$atomicNumber"` | `symbolMap[atomicNumber] ?: "?"` |
| AppModule.kt | Koin注册 | 不注入 ElementRepo | 注入 |

## Acceptance Criteria

- [ ] Learn/Discover/Lab 列表项显示元素符号而非序号
- [ ] 编译/lint/detekt 通过
