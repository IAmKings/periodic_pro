package com.periodic.pro.data.permission

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 当前待展示的权限弹窗请求。
 */
data class PermissionRequest(
    val def: PermissionDef,
    /** true=被永久拒绝后的引导弹窗，false=首次请求解释弹窗 */
    val isPermanentlyDenied: Boolean,
)

/**
 * 权限管理器 — Koin 单例。
 *
 * 统一管理运行时权限的检查、请求、解释弹窗和设置引导。
 */
class PermissionsManager(
    private val context: Context,
) {

    private val _request = MutableStateFlow<PermissionRequest?>(null)
    val request: StateFlow<PermissionRequest?> = _request.asStateFlow()

    /** 检查权限是否已授予 */
    fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    /** 发起权限请求流程。已授权则直接回调，否则弹出解释弹窗 */
    fun request(def: PermissionDef, onGranted: () -> Unit) {
        if (isGranted(def.permission)) {
            onGranted()
            return
        }
        _request.update { PermissionRequest(def, isPermanentlyDenied = false) }
    }

    /** 用户点击"确认"后的实际系统权限请求回调 */
    fun onConfirm(def: PermissionDef, onGranted: () -> Unit, onDenied: () -> Unit) {
        _request.update { null }
        if (isGranted(def.permission)) {
            onGranted()
        } else {
            // 引导到系统设置
            openAppSettings()
            onDenied()
        }
    }

    /** 用户取消权限请求 */
    fun onDismiss() {
        _request.update { null }
    }

    /** 弹出"已永久拒绝"引导弹窗 */
    fun showDeniedGuide(def: PermissionDef) {
        _request.update { PermissionRequest(def, isPermanentlyDenied = true) }
    }

    /** 引导到应用设置页 */
    fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}"),
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(intent)
    }
}
