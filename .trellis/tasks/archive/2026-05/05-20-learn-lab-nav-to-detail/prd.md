# 学习资料+实验室页面元素跳转到详情二级页面

## Goal

学习资料页（LearnScreen）点击"查看元素"、实验室页（LabScreen）点击反应涉及的元素标签，正确跳转到元素详情二级页面（`detail/{atomicNumber}`），而非回到 Tab 主框架。

## Root Cause

上一轮把 DetailScreen 搬到根 NavHost 二级页面时，**PeriodicNav.kt 中 6 处回调全部正确更新**为 `rootNavController?.navigate("detail/$atomicNumber")`，但 **MainActivity.kt 中 Learn 和 Lab 路由的 `onNavigateToDetail` 回调写错了**——只做了 `popBackStack("main")`，完全忽略了 `atomicNumber` 参数：

```kotlin
// MainActivity.kt learn 路由 — 当前（错误）
onNavigateToDetail = {
    rootNavController.popBackStack("main", inclusive = false)  // ❌ 没跳转详情
}

// MainActivity.kt lab 路由 — 当前（错误）
onNavigateToDetail = {
    rootNavController.popBackStack("main", inclusive = false)  // ❌ 没跳转详情
}
```

## 路由是否支持连续跳转

**支持。** 根 NavHost 已有完整的 detail 路由（带滑动动画），且 `detail/{atomicNumber}` 内 `DetailScreen` 又支持 `onNavigateToLearn` / `onNavigateToLab` / `onNavigateToLabDetail` 等回跳能力。连续跳转链路完全可用：

```
Learn → detail/26 → detail/24 → lab?reactionId=xxx → detail/26 ...
```

只需把回调填对即可。

## Fix

MainActivity.kt 中两处回调改为：

```kotlin
// learn 路由
onNavigateToDetail = { atomicNumber ->
    rootNavController.navigate("detail/$atomicNumber")
}

// lab 路由
onNavigateToDetail = { atomicNumber ->
    rootNavController.navigate("detail/$atomicNumber")
}
```

与 PeriodicNav.kt 中 6 处已正确的回调保持一致。

## 涉及文件

- `MainActivity.kt` — learn 和 lab 路由的 `onNavigateToDetail` 回调（2 行改动）

## Acceptance Criteria

- [ ] 学习资料页点击"查看元素" → 跳转到元素详情二级页面（全屏、带滑动动画）
- [ ] 实验室页点击反应涉及的任意元素标签 → 跳转到元素详情二级页面
- [ ] 详情页中点击"学习资料"能正确跳转到对应元素的学习页
- [ ] 详情页中点击实验室反应能正确跳转到对应反应
- [ ] 返回栈正确：学习→详情→返回学习页，实验室→详情→返回实验室页

## Out of Scope

- Learn/Lab 页面本身的内部导航逻辑不变
- 不涉及新路由添加
