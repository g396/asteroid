package sns.asteroid.view

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import jp.wasabeef.glide.transformations.MaskTransformation
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.entities.Account
import sns.asteroid.api.entities.FilterResult
import sns.asteroid.model.emoji.CustomEmojiParser
import sns.asteroid.model.settings.SettingsValues
import sns.asteroid.model.util.ImageSourceGetter
import sns.asteroid.model.util.TimeFormatter

object BindingAdapter {
    /**
     * ユーザ名とacctを1つのTextViewに併記する
     */
    @BindingAdapter("acct", "displayName")
    @JvmStatic
    fun setName(view: TextView, acct: String?, displayName: String?) {
        val screenName = String.format(getString(R.string.acct), acct)

        val imageGetter = let {
            val size = (view.textSize * 1.0).toInt()
            ImageSourceGetter(view, size)
        }

        view.text = HtmlCompat.fromHtml(
            "$displayName $screenName", HtmlCompat.FROM_HTML_MODE_COMPACT, imageGetter, null).trim()

    }

    /**
     * 絵文字を含むHTMLテキストをセットする
     * フラグに合わせて絵文字の拡大・縮小も行う
     */
    @BindingAdapter("content", "scaleEmojis")
    @JvmStatic
    fun setContent(view: TextView, content: String?, scaleEmojis: Boolean) {
        if(content == null) return

        val imageGetter = run {
            val scale = if (scaleEmojis) SettingsValues.getInstance().emojiSize else 1.0
            val size = (view.textSize * scale).toInt()
            ImageSourceGetter(view, size)
        }
        view.text = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY, imageGetter, null)
            .trimEnd()
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
     * ブーストされた投稿を緑色の枠で囲む
     */
    @BindingAdapter("reblog")
    @JvmStatic
    fun setBackground(view: ConstraintLayout, reblog: Boolean = false) {
        val backgroundResId =
            if (reblog) R.drawable.background_boost
            else R.drawable.background_normal
        view.background = AppCompatResources.getDrawable(view.context, backgroundResId)
    }

    /**
     * 設定から有効にしている場合に
     * 未収載の投稿を緑色、非公開の投稿をオレンジ色、DMを紫色の文字にする
     */
    @BindingAdapter("setTextColorByVisibility")
    @JvmStatic
    fun setTextColorByVisibility(view: TextView, visibility: String?) {
        if (!SettingsValues.getInstance().changeTextColor) return
        val color = when (visibility) {
            "unlisted"  -> view.context.getColor(R.color.textColorUnlisted)
            "private"   -> view.context.getColor(R.color.textColorPrivate)
            "direct"    -> view.context.getColor(R.color.textColorDirect)
            else        -> view.context.getColor(R.color.textColorPublic)
        }
        view.setTextColor(color)
    }

    /**
     * 投稿の公開範囲のアイコンを設定する
     */
    @BindingAdapter("visibility")
    @JvmStatic
    fun setVisibilityIcon(view: ImageView, visibility: String?) {
        view.isVisible = visibility != "public"
        when (visibility) {
            "unlisted" -> view.setImageResource(R.drawable.visibility_unlisted)
            "private" -> view.setImageResource(R.drawable.visibility_locked)
            "direct" -> view.setImageResource(R.drawable.visibility_direct)
        }
    }

    /**
     * 投稿時刻をセット
     * 時刻のフォーマットは設定値に合わせて変える
     */
    @BindingAdapter("date")
    @JvmStatic
    fun convertDate(view: TextView, date: String?) {
        view.text = date?.let {
            when (SettingsValues.getInstance().timeFormat) {
                SettingsValues.TIME_FORMAT_AUTO -> TimeFormatter.formatAuto(it)
                SettingsValues.TIME_FORMAT_RELATIVE -> TimeFormatter.formatRelative(it)
                SettingsValues.TIME_FORMAT_ABSOLUTE -> TimeFormatter.formatAbsolute(it)
                SettingsValues.TIME_FORMAT_ABSOLUTE_BY_SECONDS -> TimeFormatter.formatAbsolute(it, useSeconds = true)
                else -> TimeFormatter.formatAuto(it)
            }
        }
    }

    /**
     * 投票の終了時刻をセット
     */
    @BindingAdapter("expireAt")
    @JvmStatic
    fun setExpireAt(view:TextView, expireAt: String?) {
        view.text = TimeFormatter.formatExpire(expireAt)
    }

    /**
     * フィルターによって非表示となった投稿に対し
     * どのフィルターが適用されているか表示する
     */
    @BindingAdapter("filter_subject")
    @JvmStatic
    fun setContentFiltering(view: TextView, filter: List<FilterResult>?) {
        val filterTitle = filter?.firstOrNull()?.filter?.title ?: "Unknown"
        view.text = String.format("Filtered: %s", filterTitle)
    }

    /**
     * 通知のタイトルをセットする
     * (例:Fooさんと他n人がふぁぼりました)
     */
    @BindingAdapter("reactedUser", "notificationType", "peoplesCount")
    @JvmStatic
    fun setNotificationTitle(view: TextView, reactedUser: Account?, notificationType: String?, peoplesCount: Int?) {
        if(reactedUser == null) return

        val name = let {
            val user = CustomEmojiParser.parse(reactedUser.display_name, reactedUser.emojis).ifEmpty { reactedUser.acct }
            if ((peoplesCount ?: 0) > 1) {
                String.format(getString(R.string.notification_multi), user, peoplesCount!!-1)
            } else {
                String.format(getString(R.string.notification_single), user)
            }
        }

        val message = when(notificationType) {
            "favourite"         -> "$name${getString(R.string.notification_favourited)}"
            "reblog"            -> "$name${getString(R.string.notification_reblogged)}"
            "mention"           -> "$name${getString(R.string.notification_mention)}"
            "follow"            -> "$name${getString(R.string.notification_follow)}"
            "follow_request"    -> "$name${getString(R.string.notification_follow_request)}"
            "poll"              -> "$name${getString(R.string.notification_poll)}"
            "emoji_reaction"    -> "$name${getString(R.string.notification_emoji)}" // use in fedibird.com
            "status"            -> "$name${getString(R.string.notification_status)}"
            else                -> getString(R.string.notification_others)
        }

        val imageGetter = ImageSourceGetter(view, view.textSize.toInt())
        view.text = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_COMPACT, imageGetter, null)
    }

    /**
     * 通知画面の画像をセット
     * ユーザのアイコン画像・ふぁぼやBTのアイコン・リアクションの絵文字を種類問わずAdapterに突っ込んでいるので
     * それぞれ場合分けをする
     */
    @BindingAdapter("imageUrl", "imageType")
    @JvmStatic
    fun setNotificationImage(view: ImageView, imageUrl: String?, imageType: String?) {
        val isAnimationEnable = SettingsValues.getInstance().isEnableEmojiAnimation
        val applyImage: () -> Unit = {
            Glide.with(view)
                .load(imageUrl)
                .dontTransform()
                .let { if(!isAnimationEnable) it.dontAnimate() else it }
                .into(view)
        }

        when (imageType) {
            "favourite" -> view.setImageResource(R.drawable.star)
            "reblog"    -> view.setImageResource(R.drawable.boost)
            "mention"     -> view.setImageResource(R.drawable.button_reply)
            "user"      -> if(imageUrl?.isNotEmpty() == true) applyAvatar(view, imageUrl)
            else        -> if(imageUrl?.isNotEmpty() == true) { applyImage() }
        }

        view.scaleType = when (imageType) {
            "favourite" -> ImageView.ScaleType.CENTER
            "reblog"    -> ImageView.ScaleType.CENTER
            else        -> ImageView.ScaleType.FIT_CENTER
        }
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

    fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }
}