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

    @BindingAdapter("displayName")
    @JvmStatic
    fun setDisplayName(view: TextView, displayName: String?) {
        val imageGetter = let {
            val size = (view.textSize * 1.0).toInt()
            ImageSourceGetter(view, size)
        }

        view.text = displayName?.let {
            HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_COMPACT, imageGetter, null)
                .trim()
        }
    }

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
    @BindingAdapter("imageUrl", "imageType")
    @JvmStatic
    fun applyImage(view: ImageView, imageUrl: String?, imageType: String?) {
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

    @BindingAdapter("content")
    @JvmStatic
    fun setContent(view: TextView, content: String?) {
        setCustomTextSize(view, "default")

        val imageGetter = let {
            val scale = SettingsValues.getInstance().emojiSize
            val size = (view.textSize * scale).toInt()
            ImageSourceGetter(view, size)
        }

        view.text = content?.let {
            HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY, imageGetter, null)
                .trimEnd()
        }
    }

    @BindingAdapter("reblog")
    @JvmStatic
    fun setBackground(view: ConstraintLayout, reblog: Boolean = false) {
        val backgroundResId =
            if (reblog) R.drawable.background_boost
            else R.drawable.background_normal
        view.background = AppCompatResources.getDrawable(view.context, backgroundResId)
    }

    @BindingAdapter("boostedBy")
    @JvmStatic
    fun setBoostedBy(view: TextView, boostedBy: String?) {
        val imageGetter = let {
            val size = (view.textSize * 1.2).toInt()
            ImageSourceGetter(view, size)
        }
        val formatted = String.format(getString(R.string.boosted_by), boostedBy)
        view.text = HtmlCompat.fromHtml(formatted, HtmlCompat.FROM_HTML_MODE_COMPACT, imageGetter, null).trim()
    }

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

    @BindingAdapter("glide")
    @JvmStatic
    fun setImage(view: ImageView, imageUrl: String?) {
        view.visibility = if(imageUrl.isNullOrBlank()) View.GONE else View.VISIBLE
        if(!imageUrl.isNullOrBlank()) Glide.with(view).load(imageUrl).into(view)
    }

    @BindingAdapter("accentColor")
    @JvmStatic
    fun setAccentColor(view: ImageView, accentColor: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view.colorFilter = BlendModeColorFilter(accentColor, BlendMode.SRC_IN)
        } else {
            view.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN)
        }
    }

    @BindingAdapter("filter_subject")
    @JvmStatic
    fun setContentFiltering(view: TextView, filter: List<FilterResult>?) {
        val filterTitle = filter?.firstOrNull()?.filter?.title ?: "Unknown"
        view.text = String.format("Filtered: %s", filterTitle)
    }

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
     * BindingAdapterで弄りまわしてない他のTextViewはこれでサイズ調整する
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

    @BindingAdapter("expireAt")
    @JvmStatic
    fun setExpireAt(view:TextView, expireAt: String?) {
        view.text = TimeFormatter.formatExpire(expireAt)
    }

    fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }
}