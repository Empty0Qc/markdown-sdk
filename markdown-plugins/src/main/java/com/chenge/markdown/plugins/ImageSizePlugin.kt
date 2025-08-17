package com.chenge.markdown.plugins

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.Target
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.glide.GlideImagesPlugin

/**
 * 图片尺寸解析插件，url 支持参数 w=100&h=200
 */
class ImageSizePlugin(context: Context) : MarkdownPlugin {
    
    // 使用 ApplicationContext 避免 Activity 销毁后的生命周期问题
    private val appContext = context.applicationContext

    override fun apply(builder: io.noties.markwon.Markwon.Builder) {
        builder.usePlugin(
            GlideImagesPlugin.create(
                object : GlideImagesPlugin.GlideStore {
                    override fun load(drawable: AsyncDrawable): RequestBuilder<Drawable> {
                        val destination = drawable.destination
                        val (src, w, h) = parseImageUrl(destination)
                        val req = Glide.with(appContext).asDrawable().load(src)
                        return if (w != null && h != null && w > 0 && h > 0) req.override(w, h) else req
                    }

                    override fun cancel(target: Target<*>) {
                        Glide.with(appContext).clear(target)
                    }
                }
            )
        )
    }

    private fun parseImageUrl(url: String): Triple<String, Int?, Int?> {
        val uri = Uri.parse(url)
        val w = uri.getQueryParameter("w")?.toIntOrNull()
        val h = uri.getQueryParameter("h")?.toIntOrNull()
        val src = uri.buildUpon().clearQuery().build().toString()
        return Triple(src, w, h)
    }
}
