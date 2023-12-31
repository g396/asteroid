package sns.asteroid.model.util

import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition

/**
 * TextView内のimgタグの画像を取得してDrawableのオブジェクトにするやつ
 * (これがないとHtmlCompat.fromHtml()しても絵文字が表示されない)
 */
class ImageSourceGetter(val view: TextView, val textSize: Int): Html.ImageGetter {

    override fun getDrawable(source: String?): Drawable {
        return GlideDrawable().apply {
            Glide.with(view.context).asDrawable().load(source).into(this.target)
        }
    }

    inner class GlideDrawable: BitmapDrawable() {
        private var drawable: Drawable? = null

        val target = object: SimpleTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                setDrawable(resource)
            }
        }

        override fun draw(canvas: Canvas) {
            drawable?.run { draw(canvas) }
        }

        private fun setDrawable(drawable: Drawable) {
            this.drawable = drawable
            val height = textSize
            val ratio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
            val width = (height * ratio).toInt()
            drawable.setBounds(0,0, width, height)
            setBounds(0, 0, width, height)

            //絵文字画像取得後に、Viewの再描画を行わないとしっかり反映されない
            view.text = view.text

            // 絵文字のDrawableが大きすぎるとTextViewからはみ出るときの対策(死ぬほど横に長い絵文字とか)
            view.viewTreeObserver.addOnDrawListener {
                val newWidth = view.measuredWidth
                if (width > newWidth) {
                    val newHeight = (newWidth / ratio).toInt()
                    drawable.setBounds(0, 0, newWidth, newHeight)
                    setBounds(0,0, newWidth, newHeight)
                }
            }
        }
    }
}