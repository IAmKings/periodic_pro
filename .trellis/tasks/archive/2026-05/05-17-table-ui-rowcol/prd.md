# 优化元素周期表UI — 行列号 + 镧锕系间隔

## Goal

周期表顶部加列号(1-18)，左侧加行号(1-7+La/Ac)，镧系锕系行加间隔区分。

## Requirements

- 顶部列号：1-18，居中对齐每个单元格
- 左侧行号：1-7 行 + "La" 行 + "Ac" 行
- 列号/行号用 `labelSmall` 字体，`onSurfaceVariant` 颜色
- 镧系(第8行)上方 + 锕系(第9行)上方各加 `8.dp` 间隔
- 行列号不参与水平滚动（固定在左侧）
- 元素区域保持 `horizontalScroll`

## Acceptance Criteria

- [ ] 顶部显示 1-18 列号
- [ ] 左侧显示行标签
- [ ] 镧系/锕系行有明显间隔
- [ ] 行列号与元素对齐
- [ ] 水平滚动时元素滚动、行列号固定

## Out of Scope

- 缩放功能
- 列号/行号可点击交互

## Technical Notes

- 文件：`PeriodicTableGrid.kt`
- 布局：Row(固定行号列 + horizontalScroll(元素区域))
- 顶部列号放在元素区域上方
