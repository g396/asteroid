package sns.asteroid.view.binding_adapter

import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import sns.asteroid.R
import sns.asteroid.api.entities.FilterResult
import sns.asteroid.model.settings.SettingsValues
import sns.asteroid.model.util.ImageSourceGetter
import sns.asteroid.model.util.TimeFormatter

object Statuses {
    /**
     * ユーザ名とacctを1つのTextViewに併記する
     */
    @BindingAdapter("acct", "displayName")
    @JvmStatic
    fun setName(view: TextView, acct: String?, displayName: String?) {
        val screenName = String.format(view.context.getString(R.string.acct), acct)

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
}