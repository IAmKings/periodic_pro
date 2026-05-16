package com.periodic.pro.data.permission

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 权限定义 — 新增权限只需加一个定义项。
 *
 * @param permission Android 权限名字符串
 * @param title 首次请求弹窗标题
 * @param description 为什么需要此权限
 * @param deniedTitle 被永久拒绝后的引导标题
 * @param deniedDescription 被永久拒绝后的引导文案
 * @param icon 权限图标
 */
data class PermissionDef(
    val permission: String,
    val title: String,
    val description: String,
    val deniedTitle: String,
    val deniedDescription: String,
    val icon: ImageVector,
)
