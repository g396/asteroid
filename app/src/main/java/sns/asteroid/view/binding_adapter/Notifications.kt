package sns.asteroid.view.binding_adapter

import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import sns.asteroid.R
import sns.asteroid.api.entities.Account
import sns.asteroid.model.emoji.CustomEmojiParser
import sns.asteroid.model.settings.SettingsValues
import sns.asteroid.model.util.ImageSourceGetter

object Notifications {
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
                String.format(view.context.getString(R.string.notification_multi), user, peoplesCount!!-1)
            } else {
                String.format(view.context.getString(R.string.notification_single), user)
            }
        }

        val message = when(notificationType) {
            "favourite"         -> "$name${view.context.getString(R.string.notification_favourited)}"
            "reblog"            -> "$name${view.context.getString(R.string.notification_reblogged)}"
            "mention"           -> "$name${view.context.getString(R.string.notification_mention)}"
            "follow"            -> "$name${view.context.getString(R.string.notification_follow)}"
            "follow_request"    -> "$name${view.context.getString(R.string.notification_follow_request)}"
            "poll"              -> "$name${view.context.getString(R.string.notification_poll)}"
            "emoji_reaction"    -> "$name${view.context.getString(R.string.notification_emoji)}" // use in fedibird.com
            "status"            -> "$name${view.context.getString(R.string.notification_status)}"
            else                -> view.context.getString(R.string.notification_others)
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
            "user"      -> if(imageUrl?.isNotEmpty() == true) General.applyAvatar(view, imageUrl)
            else        -> if(imageUrl?.isNotEmpty() == true) { applyImage() }
        }

        view.scaleType = when (imageType) {
            "favourite" -> ImageView.ScaleType.CENTER
            "reblog"    -> ImageView.ScaleType.CENTER
            else        -> ImageView.ScaleType.FIT_CENTER
        }
    }
}