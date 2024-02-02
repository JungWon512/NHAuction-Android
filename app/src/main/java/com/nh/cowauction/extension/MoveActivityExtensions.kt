package com.nh.cowauction.extension

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Process
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.AnimRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.nh.cowauction.R
import kotlin.system.exitProcess

/**
 * ActivityResult 초기화 함수
 * 기존 onActivityResult -> API 변경됨.
 * @link #lifecycle onCreate 에서 해당 함수를 호출해야함.
 */
inline fun FragmentActivity.initActivityResult(
        crossinline callback: (ActivityResult) -> Unit
) = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
) {
    callback.invoke(it)
}

inline fun <reified T : Activity> Activity.startAct(
        @AnimRes enterAni: Int = -1,
        @AnimRes exitAni: Int = -1,
        data: Intent.() -> Unit = {}
) {
    val intent = Intent(this, T::class.java)
    intent.data()
    startActivity(intent)
    if (enterAni != -1 && exitAni != -1) {
        overridePendingTransition(enterAni, exitAni)
    } else {
        // 모든 페이지 공통
        overridePendingTransition(R.anim.in_right_to_left, R.anim.out_right_to_left)
    }
}

inline fun <reified T : Activity> Activity.startActResult(
        @AnimRes enterAni: Int = -1,
        @AnimRes exitAni: Int = -1,
        requestCode: Int,
        data: Intent.() -> Unit = {}
) {
    val intent = Intent(this, T::class.java)
    intent.data()
    startActivityForResult(intent, requestCode)
    if (enterAni != -1 && exitAni != -1) {
        overridePendingTransition(enterAni, exitAni)
    } else {
        // 모든 페이지 공통
        overridePendingTransition(R.anim.in_right_to_left, R.anim.out_right_to_left)
    }
}

fun Activity.exitApp() {
    moveTaskToBack(true)
    finishAffinity()
    finishAndRemoveTask()
    exitProcess(Process.myPid())
}

fun AppCompatActivity.movePlayStore() {
    try {
        Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=${packageName}")
            startActivity(this)
        }
    } catch (ex: ActivityNotFoundException) {
        Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details")
            startActivity(this)
        }
    }

    finishAffinity()
}