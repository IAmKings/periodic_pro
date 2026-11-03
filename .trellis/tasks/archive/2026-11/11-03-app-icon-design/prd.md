# 周期表 Pro 应用图标概念设计

## Goal
为元素周期表 Android 应用设计三个可辨识的图标方向，展示概念后由用户选定，用户已选择 B「元素 P」，本轮完成正式资源并接入应用。

## Facts and assumptions
- 名称：周期表 Pro；包含 118 元素查询、学习、测试、实验室和元素对比。
- 现有界面以 Material 紫色 #6650A4、浅底、圆角元素卡片为主。
- 旧图标为 P + 15，对应磷元素，也关联 Periodic / Pro。
- 假设用户偏好清晰、理性、现代、有探索感；目标用户为学生及化学爱好者。

## Requirements
- 三个不同方向：周期表结构抽象、P 字母符号、周期循环抽象。
- 先黑白 SVG，全部使用几何形状和路径，不依赖字体、位图、滤镜。
- 至少两轮渲染检查，检查 16 / 32 / 64 px、反白、镜像、旋转和相近标志对比。
- 提供中文概念总览和推荐理由，概念 SVG 保存在本任务下。

## Acceptance criteria
- [x] 三个方向可见且风格互异。
- [x] SVG 审计、渲染及小尺寸视觉检查完成。
- [x] 展示总览并等待用户选择方向。
- [x] 正式 SVG、单色/反白、小尺寸、横竖组合与图标导出完成。
- [x] Android adaptive / monochrome / API24–25 fallback 同步，浅深色一致。
- [x] 资源构建、图标验证、展示及交付说明完成。

## Scope
用户选择 B 后，完成 icon/ 下正式资源、使用说明、场景展示，并接入现有 drawable/mipmap。保留已批准紫色 #6650A4 与白色。
不修改其他会话正在调整的 Compose 界面、主题代码或 DESIGN.md。
范围包括既有 icon/ 导出副本及浅深色资源，补齐 minSdk24–25 launcher fallback；无业务逻辑和依赖变化。

## Research and strategy
见 research/design-notes.md。

## Open question
无。2026-11-03 用户明确选择 B。
