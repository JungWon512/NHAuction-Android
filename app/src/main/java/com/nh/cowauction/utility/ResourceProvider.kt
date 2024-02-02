package com.nh.cowauction.utility

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import okio.IOException
import java.io.InputStream
import javax.inject.Inject

/**
 * Description : Resource Provider Class
 *
 * Created by hmju on 2021-05-18
 */
interface ResourceProvider {
    fun getContext(): Context
    fun getRes(): Resources
    fun getDrawable(@DrawableRes resId: Int): Drawable?
    fun getDimen(@DimenRes resId: Int): Int
    fun getDimenFloat(@DimenRes resId: Int): Float
    fun getColor(@ColorRes color: Int): Int
    fun getString(@StringRes resId: Int): String
    fun getStringArray(@ArrayRes resId: Int): Array<String>
    fun getAsset(fileName: String): InputStream?
}

class ResourceProviderImpl @Inject constructor(
    @ApplicationContext private val ctx: Context
) : ResourceProvider {

    override fun getRes(): Resources = ctx.resources

    override fun getContext() = ctx

    override fun getDrawable(resId: Int) = AppCompatResources.getDrawable(ctx, resId)

    override fun getDimen(resId: Int) = getRes().getDimensionPixelSize(resId)

    override fun getDimenFloat(resId: Int) = getRes().getDimension(resId)

    override fun getColor(color: Int) = ContextCompat.getColor(ctx, color)

    override fun getString(resId: Int) = getRes().getString(resId)

    override fun getStringArray(resId: Int): Array<String> = getRes().getStringArray(resId)

    override fun getAsset(fileName: String): InputStream? {
        return try {
            getRes().assets.open(fileName)
        } catch (ex: IOException) {
            null
        }
    }
}