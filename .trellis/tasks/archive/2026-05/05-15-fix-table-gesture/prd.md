# 修复周期表长按误触发收藏及滑动误入多选模式

## Goal

修复周期表页面两个严重交互bug：
1. 滑动时误触发长按 → 进入多选模式
2. 长按自动收藏元素 → 收藏页面堆满误触元素

## Root Cause

`TableViewModel.onElementLongClick()` 同时做了两件事：
- 进入多选模式 + 选中元素 ✅
- 调用 `favoritesRepo.toggle()` 收藏元素 ❌（不应在此处）

Telephoto zoomable 在平移过程中可能触发 `onLongClick`，导致误进入多选模式并错误收藏。

## Fix

1. `TableViewModel.onElementLongClick` — 移除 `favoritesRepo.toggle()` 调用。长按仅做：进入多选模式 + 选中元素
2. `PeriodicTableGrid` — 在 zoomable 的 onLongClick 中加移动阈值保护，平移距离超过阈值时不触发

## Acceptance Criteria

- [ ] 长按元素进入多选模式，不自动收藏
- [ ] 滑动平移周期表不误触发长按
- [ ] 收藏仅通过用户手动操作达成
- [ ] 现有正常长按→多选→比较流程不受影响

## Out of Scope

- 收藏功能本身改造

## Technical Notes

- 文件：`TableViewModel.kt` (line 84-96)、`PeriodicTableGrid.kt`
- 收藏入口应仅在元素详情页的收藏按钮
