package com.iflytek.speech.util

import android.app.Activity
import android.app.AlertDialog
import android.app.AlertDialog.Builder
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.net.Uri

import com.iflytek.cloud.SpeechUtility


/**
 * 弹出提示框，下载服务组件
 */
class ApkInstaller(private val mActivity: Activity) {

    fun install() {
        val builder = Builder(mActivity)
        builder.setMessage("检测到您未安装语记！\n是否前往下载语记？")
        builder.setTitle("下载提示")
        builder.setPositiveButton("确认前往") { dialog, which ->
            dialog.dismiss()
            val url = SpeechUtility.getUtility().componentUrl
            val assetsApk = "SpeechService.apk"
            processInstall(mActivity, url, assetsApk)
        }
        builder.setNegativeButton("残忍拒绝") { dialog, which -> dialog.dismiss() }
        builder.create().show()
        return
    }

    /**
     * 如果服务组件没有安装打开语音服务组件下载页面，进行下载后安装。
     */
    private fun processInstall(context: Context, url: String, assetsApk: String): Boolean {
        //直接下载方式
        val uri = Uri.parse(url)
        val it = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(it)
        return true
    }
}
