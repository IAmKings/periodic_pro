# 元素详情关联实验室化学反应

## Goal

元素详情页底部增加"相关化学反应"区，展示涉及当前元素的反应列表，点击跳转到 Lab 详情。

## Requirements

- LabRepository 加 `getByElement(atomicNumber)` → 筛选 `involvedElements` 包含目标序号的反应
- DetailScreen ExtraInfoSection 下加"相关化学反应"区域
- 显示反应方程 + 名称，最多 5 条
- 点击某条反应 → 导航到 Lab 详情（LabScreen 加 `initialReactionId` 参数）
- 底部"查看更多反应"按钮 → 跳转 Lab 列表页

## Technical Notes

- reactions.json 已有 `involvedElements: [Int]` 字段
- LabScreen 当前无 `initialReactionId` 参数
- 导航需新增 lab detail 路由
