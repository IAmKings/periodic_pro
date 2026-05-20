# 修复首页搜索跳转后输入框光标位置

## Goal

首页搜索"24"跳转周期表后，输入框显示"24"但光标在头部。修复为光标在末尾。

## Root Cause

`key(searchBarRestartKey)` 重建 `OutlinedTextField` 后，Compose 默认 `selection=TextRange(0)`。`key` 方案无法控制光标位置。

## Fix

用 `Modifier.onGloballyPositioned` 获取 `TextInputSession`，在布局完成后通过 `InputConnection.setSelection` 移动光标到末尾。

## Requirements

- 跳转后光标在输入文本末尾
- 正常输入不受影响
- 不引起额外重组或性能问题

## Technical Notes

- 文件：`PeriodicSearchBar.kt`, `TableScreen.kt`
- 移除 `key(searchBarRestartKey)` 和 `searchBarRestartKey` 状态
- 新增 `cursorAtEndTrigger` 参数 + `onGloballyPositioned` 光标定位
