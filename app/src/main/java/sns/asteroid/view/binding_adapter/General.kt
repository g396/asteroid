package sns.asteroid.view.binding_adapter

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import jp.wasabeef.glide.transformations.MaskTransformation
import sns.asteroid.R
import sns.asteroid.model.settings.SettingsValues

object General {
    /**
     * 設定でテキストサイズを変更している場合に
     * サイズを適用する(TextView用)
     */
    @BindingAdapter("customTextSize")
    @JvmStatic
    fun setCustomTextSize(view: TextView, customTextSize: String?) {
        val size =
            if(view is EditText) SettingsValues.getInstance().editTextSize
            else SettingsValues.getInstance().textSize
        val weight = when(customTextSize) {
            "default"   -> 0
            "small"     -> -3
            "large"     -> 3
            "largest"   -> 9
            else        -> 0
        }
        view.textSize = size + weight
    }

    /**
     * 設定でテキストサイズを変更している場合に
     * サイズを適用する(Buttonのテキスト用)
     */
    @BindingAdapter("customTextSize")
    @JvmStatic
    fun setCustomTextSize(view: Button, customTextSize: String?) {
        val size = SettingsValues.getInstance().textSize
        val weight = when(customTextSize) {
            "default"   -> 0
            "small"     -> -3
            "large"     -> 3
            "largest"   -> 9
            else        -> 0
        }
        view.textSize = size + weight
    }

    /**
     * 指定した色をImageViewに適用
     */
    @BindingAdapter("accentColor")
    @JvmStatic
    fun setAccentColor(view: ImageView, accentColor: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view.colorFilter = BlendModeColorFilter(accentColor, BlendMode.SRC_IN)
        } else {
            view.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN)
        }
    }

    /**
     * Glideを使用して画像をセット（汎用）
     */
    @BindingAdapter("glide")
    @JvmStatic
    fun setImage(view: ImageView, imageUrl: String?) {
        view.visibility = if(imageUrl.isNullOrBlank()) View.GONE else View.VISIBLE
        if(!imageUrl.isNullOrBlank()) Glide.with(view).load(imageUrl).into(view)
    }

    /**
     * アイコン画像のセット
     * 設定値に合わせて切り抜く形を変更（デフォルトで円形）
     */

    @BindingAdapter("avatarUrl")
    @JvmStatic
    fun applyAvatar(view: ImageView, avatarUrl: String?) {
        val transformation = when(SettingsValues.getInstance().avatarShape) {
            SettingsValues.AvatarShape.CIRCLE -> MaskTransformation(R.drawable.circle)
            SettingsValues.AvatarShape.NFT    -> MaskTransformation(R.drawable.hexagon)
            SettingsValues.AvatarShape.SQUARE -> MaskTransformation(R.drawable.round_square)
        }
        val isAnimationEnable = SettingsValues.getInstance().isEnableAvatarAnimation

        Glide.with(view)
            .load(avatarUrl)
            .transform(transformation)
            .placeholder(R.drawable.sync)
            .error(R.drawable.question)
            .let { if(!isAnimationEnable) it.dontAnimate() else it }
            .into(view)
    }

    /**
     * カラムのヘッダーのアイコンを表示するかどうか設定
     */
    @BindingAdapter("avatarContext")
    @JvmStatic
    fun showAvatar(view: ImageView, avatarContext: String?) {
        val showAvatar = SettingsValues.getInstance().showAvatarInColumnHeader
        if ((avatarContext == "header") and !showAvatar) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }
}