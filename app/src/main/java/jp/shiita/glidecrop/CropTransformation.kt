package jp.shiita.glidecrop

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.v4.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.util.Util
import java.security.MessageDigest
import kotlin.math.max

class CropTransformation(@DrawableRes private val resId: Int, private val out: Boolean = false) : Transformation<Bitmap> {
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
        messageDigest.update(resId.toByte())
        messageDigest.update(if (out) Byte.MAX_VALUE else Byte.MIN_VALUE)
    }

    override fun transform(context: Context, resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        if (!Util.isValidDimensions(outWidth, outHeight))
            throw IllegalArgumentException("Cannot apply transformation on width: $outWidth or height: $outHeight less than or equal to zero and not Target.SIZE_ORIGINAL")

        val bitmapPool = Glide.get(context).bitmapPool
        val toTransform = resource.get()
        val targetWidth = if (outWidth == Target.SIZE_ORIGINAL) toTransform.width else outWidth
        val targetHeight = if (outHeight == Target.SIZE_ORIGINAL) toTransform.height else outHeight
        val drawable = ResourcesCompat.getDrawable(context.resources, resId, null)
        val transformed = transform(bitmapPool, toTransform, targetWidth, targetHeight, drawable)

        return if (toTransform == transformed) resource else BitmapResource.obtain(transformed, bitmapPool)!!
    }

    private fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int, drawable: Drawable?): Bitmap {
        drawable ?: return toTransform

        val srcWidth = toTransform.width
        val srcHeight = toTransform.height
        val scaleX = outWidth / srcWidth.toFloat()
        val scaleY = outHeight / srcHeight.toFloat()
        val maxScale = max(scaleX, scaleY)

        val scaledWidth = maxScale * srcWidth
        val scaledHeight = maxScale * srcHeight
        val left = (outWidth - scaledWidth) / 2f
        val top = (outHeight - scaledHeight) / 2f
        val destRect = RectF(left, top, left + scaledWidth, top + scaledHeight)

        val bitmap = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = if (out) SRC_OUT_PAINT else SRC_IN_PAINT

        drawable.bounds = Rect(0, 0, outWidth, outHeight)
        drawable.draw(canvas)
        canvas.drawBitmap(toTransform, null, destRect, paint)
        return bitmap
    }

    override fun equals(other: Any?): Boolean =
            other is CropTransformation && resId == other.resId && out == other.out

    override fun hashCode(): Int =
            Util.hashCode(ID.hashCode(), Util.hashCode(resId, Util.hashCode(out)))

    companion object {
        private val ID = CropTransformation::class.java.name
        private val ID_BYTES = ID.toByteArray(Charsets.UTF_8)
        private const val PAINT_FLAGS = Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG
        private val SRC_OUT_PAINT = Paint(PAINT_FLAGS).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT) }
        private val SRC_IN_PAINT = Paint(PAINT_FLAGS).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN) }
    }
}