package com.nh.cowauction.extension

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.nh.cowauction.contants.Type
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order
import okio.IOException
import java.io.File
import java.lang.reflect.Field
import java.security.MessageDigest
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

val osCode: String = "ANDROID"


// MultiPle Null Check.
inline fun <A, B, R> multiNullCheck(a: A?, b: B?, function: (A, B) -> R): R? {
    return if (a != null && b != null) {
        function(a, b)
    } else {
        null
    }
}

// MultiPle Null Check.
inline fun <A, B, C, R> multiNullCheck(a: A?, b: B?, c: C?, function: (A, B, C) -> R): R? {
    return if (a != null && b != null && c != null) {
        function(a, b, c)
    } else {
        null
    }
}

inline fun <A, B> multiCompareLoop(aList: List<A>, bList: List<B>, function: (A, B) -> Unit) {
    val size = aList.size.coerceAtMost(bList.size)
    for (index in 0 until size) {
        function.invoke(aList[index], bList[index])
    }
}

/**
 * 날짜 노출 확장 함수
 * ex.) 2021.02.21
 * @param isTime true -> 시:분 노출, false -> 날짜만 노출.
 */
@SuppressLint("SimpleDateFormat")
fun Long.simpleDtm(isTime: Boolean = false): String {
    val sdf = if (isTime) {
        SimpleDateFormat("yyyy.MM.dd HH:mm")
    } else {
        SimpleDateFormat("yyyy.MM.dd")
    }
    return sdf.format(this)
}

@SuppressLint("SimpleDateFormat")
fun Long.simpleDate(isTime: Boolean = false): Date? {
    val sdf = if (isTime) {
        SimpleDateFormat("yyyy.MM.dd HH:mm")
    } else {
        SimpleDateFormat("yyyy.MM.dd")
    }
    return sdf.parse(sdf.format(this))
}

/**
 * 날짜 노출 확장 함수.
 * ex.) 2021-02-21
 * @param isTime true -> 시:분 노출, false -> 날짜만 노출
 */
@SuppressLint("SimpleDateFormat")
fun Long.simpleDtmHalf(isTime: Boolean = false): String {
    val sdf = if (isTime) {
        SimpleDateFormat("yyyy-MM-dd HH:mm")
    } else {
        SimpleDateFormat("yyyy-MM-dd")
    }
    return sdf.format(this)
}

fun Context.getFragmentAct(): FragmentActivity? {
    if (this is FragmentActivity) {
        return this
    } else {
        var ctx = this
        while (ctx is ContextWrapper) {
            if (ctx is FragmentActivity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }
}

@Suppress("DEPRECATION")
@SuppressLint("PackageManagerGetSignatures")
fun Context.getKeyHash(): String? {
    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        for (signature in info.signingInfo.signingCertificateHistory) {
            val md = MessageDigest.getInstance("SHA")
            md.update(signature.toByteArray())
            return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
        }
        return null
    } else {*/
    val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
    for (signature in info.signatures) {
        val md = MessageDigest.getInstance("SHA")
        md.update(signature.toByteArray())
        return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
    }
    return null
//    }
}

fun Context.isRootingDevice(): Boolean {
    val ROOT_PATH =  /*Environment.getExternalStorageDirectory() + */""
    val ROOTING_PATH_1 = "/system/bin/su"
    val ROOTING_PATH_2 = "/system/xbin/su"
    val ROOTING_PATH_3 = "/system/app/SuperUser.apk"
    val ROOTING_PATH_4 = "/data/data/com.noshufou.android.su"
    val ROOTING_PATH_5 = "/sbin/su"
    val ROOTING_PATH_6 = "/data/local/xbin/su"
    val ROOTING_PATH_7 = "/data/local/bin/su"
    val ROOTING_PATH_8 = "/system/sd/xbin/s"
    val ROOTING_PATH_9 = "/system/bin/failsafe/su"
    val ROOTING_PATH_10 = "/data/local/su"
    val ROOTING_PATH_11 = "/su/bin/su"
    val ROOTING_PATH_12 = "su"
    val ROOTING_PATH_13 = "system/bin/.ext"
    val ROOTING_PATH_14 = "system/xbin/.ext"
    val ROOTING_PATH_15 = "/system/bin/xposed"

    val rootFilePath = arrayOf(
            ROOT_PATH + ROOTING_PATH_1,
            ROOT_PATH + ROOTING_PATH_2, ROOT_PATH + ROOTING_PATH_3,
            ROOT_PATH + ROOTING_PATH_4,
            ROOT_PATH + ROOTING_PATH_5,
            ROOT_PATH + ROOTING_PATH_6,
            ROOT_PATH + ROOTING_PATH_7,
            ROOT_PATH + ROOTING_PATH_8,
            ROOT_PATH + ROOTING_PATH_9,
            ROOT_PATH + ROOTING_PATH_10,
            ROOT_PATH + ROOTING_PATH_11,
            ROOT_PATH + ROOTING_PATH_12,
            ROOT_PATH + ROOTING_PATH_13,
            ROOT_PATH + ROOTING_PATH_14,
            ROOT_PATH + ROOTING_PATH_15
    )
    var checkRooting: Boolean
    checkRooting = try {
        Runtime.getRuntime().exec("su")
        true
    } catch (e: IOException) {
        // Exception 나면 루팅 false
        false
    } catch (e: NullPointerException) {
        false
    } catch (e: IllegalArgumentException) {
        false
    }

    try {
        if (!checkRooting) {
            val files = rootFilePath.map { File(it) }
            files.forEach {
                if (it.exists() && it.isFile) {
                    checkRooting = true
                    return@forEach
                }
            }
        }
        if (!checkRooting) {
            val buildTags = Build.TAGS
            checkRooting = buildTags != null && buildTags.contains("test-keys")
        }
        // Signing Key Check
        if (!checkRooting) {
            val hashKey = getKeyHash()
            // Log.d("DLogger", "Hash $hashKey")
//            if (hashKey != "I+dKng6bjtMfz5PWIP9YHuNHEtw=" && hashKey != "H8skJ2ICIONle8awnVa1xhupzCE=") {
//                checkRooting = true
//            }
        }
    } catch (ex: NullPointerException) {
        checkRooting = false
    } catch (ex: SecurityException) {
        checkRooting = false
    } catch (ex: PackageManager.NameNotFoundException) {
        checkRooting = false
    }

    return checkRooting
}

fun String.toEmptyStr(defValue: String = "") =
        try {
            if (this == "null" || this == "NULL" || this.isEmpty()) {
                defValue
            } else {
                this
            }
        } catch (_: NullPointerException) {
            defValue
        }

fun String.toIntOrDef(defValue: Int = 0) =
        try {
            Integer.parseInt(this)
        } catch (_: NullPointerException) {
            defValue
        } catch (_: NumberFormatException) {
            defValue
        }

fun String.toLongOrDef(defValue: Long = 0L) =
        try {
            java.lang.Long.parseLong(this)
        } catch (_: NullPointerException) {
            defValue
        } catch (_: NumberFormatException) {
            defValue
        }

fun String.toDoubleOrDef(defValue: Double = 0.0) =
        try {
            java.lang.Double.parseDouble(this)
        } catch (_: NullPointerException) {
            defValue
        } catch (_: NumberFormatException) {
            defValue
        }

/**
 * 문자열 -> 데이터 클래스 형변환 처리 함수.
 * @param separator 구분자
 * @param isTypAdded 문자열 앞에 SocketType 구분이 포함되어 있는지 유무
 *
 * @return hmju
 */
inline fun <reified T : Any> String.strToClass(
        separator: Char = '|',
        isTypAdded: Boolean = true
): T {
    val obj = T::class.java.newInstance()
    val fieldMap = hashMapOf<Int, Field>()
    obj.javaClass.declaredFields.forEach {
        if (it.isAnnotationPresent(Order::class.java)) {
            fieldMap[it.getAnnotation(Order::class.java).num] = it
        }
    }
    this.split(separator).forEachIndexed { index, str ->
        // 앞에 타입이 있는 경우 건너뜀
        if (isTypAdded && index == 0) return@forEachIndexed
        val startIndex = if (isTypAdded) index else index + 1
        runCatching {
            val field = fieldMap[startIndex] ?: return@forEachIndexed
            if (!field.isAccessible) field.isAccessible = true

            when {
                field.type.isAssignableFrom(String::class.java) -> field.set(obj, str.toEmptyStr())
                field.type.isAssignableFrom(Int::class.java) -> field.set(obj, str.toIntOrDef())
                field.type.isAssignableFrom(Long::class.java) -> field.set(obj, str.toLongOrDef())
                field.type.isAssignableFrom(Double::class.java) -> field.set(
                        obj,
                        str.toDoubleOrDef()
                )
            }
        }
    }
    return obj
}

/**
 * 데이터 클래스 -> 구분자 포함 된 문자열 처리 함수.
 * @param separator 구분자
 *
 * @return hmju
 */
fun BaseData.classToStr(separator: Char = '|'): String {
    val buffer = StringBuffer()
    if (javaClass.superclass.isAssignableFrom(BaseData::class.java)) {
        val field = javaClass.superclass.declaredFields[0]
        if (!field.isAccessible) field.isAccessible = true
        val type = field.get(this)
        if (type is Type) {
            buffer.append(type.key)
            buffer.append(separator)
        }
    }

    val fieldList = arrayListOf<Pair<Int, Field>>()
    javaClass.declaredFields.forEach {
        if (it.isAnnotationPresent(Order::class.java)) {
            fieldList.add(it.getAnnotation(Order::class.java).num to it)
        }
    }
    fieldList.sortedBy { it.first }.forEach { pair ->
        val field = pair.second
        if (!field.isAccessible) field.isAccessible = true
        buffer.append(field.get(this))
        buffer.append(separator)
    }
    // 마지막 '|' 제거.
    if (buffer.last() == separator) {
        buffer.deleteCharAt(buffer.lastIndexOf(separator))
    }
    return buffer.toString()
}

/**
 * 앱 업데이트 유무 판단
 * @param diffVersion {Major}.{Minor}.{Patch}
 * @return true 현재 버전과 diffVersion 을 비교해서 DiffVersion 이 더 큰 경우
 * false 유효한 버전이 아니거나 현재 버전이 더 큰경우.
 */
fun String.isUpdate(diffVersion: String): Boolean {
    // {Major(최대 2자리 까지)}.{Minor(최대 3자리 까지)}.{patch(최대 3자리까지)}
    val regex = "^(?:\\d{2}|\\d).(?:\\d{3}|\\d{2}|\\d).(?:\\d{3}|\\d{2}|\\d)$".toRegex()
    // 비교하려는 문자열이 {Major}.{Minor}.{patch} 버전 형태인지
    if (regex.matches(this) && regex.matches(diffVersion)) {
        val currSplit = this.split(".")
        val diffSplit = diffVersion.split(".")
        // Major 검사
        var curr = currSplit[0].toInt()
        var diff = diffSplit[0].toInt()
        if (curr < diff) {
            return true
        } else if (curr == diff) {
            curr = currSplit[1].toInt()
            diff = diffSplit[1].toInt()
            if (curr < diff) {
                return true
            } else if (curr == diff) {
                curr = currSplit[2].toInt()
                diff = diffSplit[2].toInt()
                if (curr < diff) {
                    return true
                } else if (curr == diff) {
                    return false
                } else {
                    return false
                }
            } else {
                return false
            }
        } else {
            return false
        }
    } else {
        return false
    }
}

fun Int.comma(): String {
    try {
        return NumberFormat.getInstance().format(this)
    } catch (ex: IllegalArgumentException) {
        return ""
    }
}