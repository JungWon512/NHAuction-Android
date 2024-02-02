package com.nh.cowauction.ui.scheme

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.nh.cowauction.R
import com.nh.cowauction.contants.ExtraCode
import com.nh.cowauction.extension.startAct
import com.nh.cowauction.ui.main.MainActivity
import com.nh.cowauction.utility.DLogger


class SchemeActivity : AppCompatActivity() {

    private val DEEPLINK_URL = "https://www.xn--o39an74b9ldx9g.kr/home"
    private val SHORT_DYNAMIC_LINK = "https://nhauction.page.link"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheme)

        /** 다이나믹링크 동적 링크 생성**/
        createDynamicLink()

        try {
            DLogger.d("앱 실행 상태 $isTaskRoot ${intent.data}")
            val targetUrl = intent.dataString?.substringAfter("targetUrl=")
            val dynamicLink = intent.dataString?.substringAfter("dp?urlParam=")
            Log.e("cow manager", dynamicLink.toString())

            // 앱이 실행 안된 상태
            if (isTaskRoot) {
                startAct<MainActivity>(
                    enterAni = android.R.anim.fade_in,
                    exitAni = android.R.anim.fade_out
                ) {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(ExtraCode.DEEP_LINK_TARGET_URL, targetUrl)
                }
            } else {
                startAct<MainActivity>(
                    enterAni = 0,
                    exitAni = 0
                ) {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(ExtraCode.DEEP_LINK_TARGET_URL, targetUrl)
                }
            }
        } catch (ex: ActivityNotFoundException) {
            DLogger.e("Error $ex")
        } finally {
            finish()
        }
    }

    /**
     * 동적 링크 생성
     */
    private fun createDynamicLink() {
        val dynamicLink : DynamicLink = FirebaseDynamicLinks.getInstance()
            .createDynamicLink()
            .setLink(Uri.parse(DEEPLINK_URL))
            .setDomainUriPrefix(SHORT_DYNAMIC_LINK)
            .setAndroidParameters( // 안드로이드 파라미터 추가
                DynamicLink.AndroidParameters.Builder("com.nh.cowauction")
                    .build()
            )
            .buildDynamicLink()

        val longUri : Uri? = dynamicLink.uri
        Log.e("cow manager", "long uri : " + longUri.toString())

        // 짧은 동적링크 생성
        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLongLink(longUri!!)
            .buildShortDynamicLink()
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    val shortLink: Uri? = task.result.shortLink
                    Log.e("cow manager", "short uri : $shortLink") //짧은 URI
                    startAct<MainActivity>(
                        enterAni = 0,
                        exitAni = 0
                    ) {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra(ExtraCode.DYNAMIC_LINK_TARGET_URL, shortLink)
                    }
                } else {
                    Log.e("cow manager", task.toString())
                }
            }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}