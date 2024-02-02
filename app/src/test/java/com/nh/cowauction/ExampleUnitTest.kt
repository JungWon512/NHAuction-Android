package com.nh.cowauction

import com.nh.cowauction.contants.Config
import com.nh.cowauction.extension.*
import com.nh.cowauction.model.user.UserInfo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.NumberFormat
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun birthDayCheck() {
        val birth = "1992613"

    }

    @Test
    fun ipValidateCheck() {
        val ip = "192.168.177"
    }

    @Test
    fun wonToMan_test() {
        val price = "3800000"
    }

    @Test
    fun cookie_mapping() {
        val cookie = """
            access_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhdWN0aW9uSG91c2VDb2RlIjoiODgwODk5MDY1NjY1NiIsInVzZXJSb2xlIjoiQklEREVSIiwidXNlck1lbU51bSI6IjEwMDAwMDAxIiwiZXhwIjoxNjI4NzgwMzk5fQ.By0QBDTLdEAu2dvQPKcBn7dFhI3oNWS7Q36El8c-c66KZ2RgvN1N8DuVULmdKZ_g1cJCqGCIappGi22S1MjqWw; JSESSIONID=187ADBB14A0A3620A21054B8258587B9; XSRF-TOKEN=4898d4e7-ec76-4bd1-8d0a-18317cf17e5f 
        """.trimIndent()
        val split = cookie.split(";")
        val token = cookie.split(";").map {
            return@map if (it.isNotEmpty() && it.contains("=")) {
                val s = it.split("=")
                s[0].trim() to s[1].trim()
            } else null
        }.find { if (it != null) it.first == "access_token" else false }

        println("Token ${token?.second}")
    }

    @Test
    fun json_mapping() {
        var json = """
           "{\"userNum\":\"10000001\",\"success\":true,\"auctionCode\":\"8808990656656\"}"
        """.trimIndent()

//        var json = """
//            {"userToken":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhdWN0aW9uSG91c2VDb2RlIjoiODgwODk5MDY1NjY1NiIsInVzZXJSb2xlIjoiQklEREVSIiwidXNlck1lbU51bSI6IjEwMDAwMDAxIiwiZXhwIjoxNjI4NzgwMzk5fQ.By0QBDTLdEAu2dvQPKcBn7dFhI3oNWS7Q36El8c-c66KZ2RgvN1N8DuVULmdKZ_g1cJCqGCIappGi22S1MjqWw","auctionCode":"8808990656656","userName":null,"userNum":"10000001"}
//        """.trimIndent()

//        if(json.indexOf('\"') == 0) {
//            println("앞에 큰따옴표 시작함.")
//            json = json.slice(1..json.lastIndex - 1)
//        } else {
//            println("아닙니다.")
//        }

        JSONObject(json).apply { }

        val format = Json {
            isLenient = true // Json 큰따옴표 느슨하게 체크.
            ignoreUnknownKeys = true // Field 값이 없는 경우 무시
            coerceInputValues = true // "null" 이 들어간경우 default Argument 값으로 대체
        }
        format.decodeFromString<UserInfo>(json).run {
            println("Data $this")
        }
    }

    @Test
    fun test_comma() {
//        var price = 4532
//        val plusNum = 4
//        price = (price * 10).plus(plusNum)
//        println("TestComma ${price}")
//        println("MinusComma ${0 / 10}")

        var price = 65121
        var n = Math.floor((price / 1000).toDouble()) * 1000
        val fmt = NumberFormat.getInstance().format(price)
        println("FMFF ${price.comma()}")
    }

    @Test
    fun test_diffList() {
        val newList = arrayListOf<String>("aba", "af", "ac")
        val oldList = arrayListOf<String>("aa", "ab", "ac", "ad", "ae")
//        val oldList = arrayListOf<String>()

        val diffList = newList.filter { newStr ->
            oldList.find { it == newStr } == null
        }
        println("DiffList ${Arrays.deepToString(diffList.toTypedArray())}")

    }

    @Test
    fun test_versionCheck() {
        var currVersion = "0.0.2"
        var maxVersion = "0.0.3"
        var minVersion = "0.0.1"
        if (currVersion.isUpdate(minVersion)) {
            println("최소 버전 보다 낮습니다. 업데이트 해야 합니다.")
        } else {
            if (currVersion.isUpdate(maxVersion)) {
                println("최대 버전보다 낮습니다. 업데이트 해야 합니다.")
            } else {
                println("업데이트 안해도 됩니다.")
            }

        }
//        if(currVersion.isUpdate(diffVersion)) {
//            println("업데이트 해야 합니다.")
//        } else {
//            println("업데이트 안해도 됩니다.")
//        }
    }

    @Test
    fun test_channelId() {
        val resultList = Array(1) { Config.INVALID_CHANNEL_ID }
        val list = arrayListOf<String>()
        list.add("8808990661315_remoteVideo2_1636549379021_GyJLt")
        list.add("8808990661315_remoteVideo1_1636549392882_3ZLUi")
        Collections.sort(list, compareBy {
            val spit = it.split("_")
            spit[1]
        })
        list.forEach {
            val idx = it.split("_")[1].substringAfter("remoteVideo").toIntOrDef(1)
            if(resultList.size > idx - 1) {
                if (resultList[idx - 1] == Config.INVALID_CHANNEL_ID) {
                    resultList[idx - 1] = it
                }
            }
        }
        println("Array ${resultList.contentDeepToString()}")
    }
}