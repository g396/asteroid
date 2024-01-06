package sns.asteroid.model.settings

import android.preference.PreferenceManager
import sns.asteroid.CustomApplication

/**
 * 設定画面以外でSharedPreferencesを読み書きするときに使う
 */
class SettingsValues private constructor() {
    val emojiSize: Double
    val textSize = getTextSize()
    val editTextSize = getEditTextSize()
    val imageSize = getResizeImageSize()
    val timeFormat = getTimeFormat()
    val changeTextColor = getChangeTextColorByVisibility()
    val avatarShape = getAvatarShape()
    val showAvatarInColumnHeader = getIsShowAvatarInColumnHeader()
    var isDialogEnableOnFollow = getIsDialogEnableOnFollow()
        set(value) {
            field = value
            setIsDialogEnableOnFollow(value)
        }
    var isDialogEnableOnFavourite = getIsDialogEnableOnFavourite()
        set(value) {
            field = value
            setIsDialogEnableOnFavourite(value)
        }
    var isDialogEnableOnBoost = getIsDialogEnableOnBoost()
        set(value) {
            field = value
            setIsDialogEnableOnBoost(value)
        }
    var isDialogEnableOnBookmark = getIsDialogEnableOnBookmark()
        set(value) {
            field = value
            setIsDialogEnableOnBookmark(value)
        }
    var isDialogEnableOnEmojiReaction = getIsDialogEnableOnEmojiReaction()
        set(value) {
            field = value
            setIsDialogEnableOnEmojiReaction(value)
        }
    var isDialogEnableOnUnFollow = getIsDialogEnableOnUnFollow()
        set(value) {
            field = value
            setIsDialogEnableOnUnFollow(value)
        }
    var isDialogEnableOnUndoFavourite = getIsDialogEnableOnUndoFavourite()
        set(value) {
            field = value
            setIsDialogEnableOnUndoFavourite(value)
        }
    var isDialogEnableOnUndoBoost = getIsDialogEnableOnUndoBoost()
        set(value) {
            field = value
            setIsDialogEnableOnUndoBoost(value)
        }
    var isDialogEnableOnUndoBookmark = getIsDialogEnableOnUndoBookmark()
        set(value) {
            field = value
            setIsDialogEnableOnUndoBookmark(value)
        }
    var isDialogEnableOnUndoEmojiReaction = getIsDialogEnableOnUndoEmojiReaction()
        set(value) {
            field = value
            setIsDialogEnableOnUndoEmojiReaction(value)
        }
    var isDialogEnableOnPostStatus = getIsDialogEnableOnPostStatus()
        set(value) {
            field = value
            setIsDialogEnableOnPostStatus(value)
        }
    var isDialogEnableOnDeleteStatus = getIsDialogEnableOnDeleteStatus()
        set(value) {
            field = value
            setIsDialogEnableOnDeleteStatus(value)
        }
    val isShowCard = getIsShowCard()
    val applyBackgroundColor = getApplyBackgroundColorBoostedPosts()
    val isShowQuickPostArea = getIsShowQuickPostArea()
    val isShowQuickPostVisibility = getIsShowQuickPostVisibility()
    val isShowQuickPostEmoji = getIsShowQuickPostEmoji()
    val isShowQuickPostHashtag = getIsShowQuickPostHashtag()
    val isHideActionButtons = getIsHideActionButtons()
    val isShowVia = getIsShowVia()
    val isStaticTabsWidth = getIsStaticTabsWidth()
    val tabsWidth = getTabsWidth()
    val sendWithEnterKey = getSendWithEnterKey()
    val isShowFollowersCount = getShowFollowersCount()
    val isShowReactionsCount = getShowReactionsCount()
    val isDisableSleep = getDisableSleep()
    val isEnableEmojiAnimation = getIsEnableEmojiAnimation()
    val isEnableAvatarAnimation = getIsEnableAvatarAnimation()

    init {
        instance = this
        emojiSize = getEmojiSize()
    }

    companion object {
        private var instance: SettingsValues? = null

        /**
         * getInstance()後に設定画面(SettingsActivity)で値を書き換えた場合、
         * その値がSettingValuesのインスタンスに反映されないのでnewInstance()する必要有り
         */
        fun getInstance(): SettingsValues {
            return instance ?: SettingsValues()
        }

        fun newInstance(): SettingsValues {
            return SettingsValues().also { instance = it }
        }

        // TODO: 直接DoubleやIntで取得できないものか・・・
        private fun getEmojiSize(): Double {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getString("emoji_size", "1.2")!!.toDouble()
        }

        private fun getTextSize(): Float {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_TEXT_SIZE, "15")!!.toFloat()
        }

        private fun getEditTextSize(): Float {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_EDITTEXT_SIZE, "18")!!.toFloat()
        }

        private fun getResizeImageSize(): Int {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_RESIZE_IMAGE_SIZE, "1920")!!.toInt()
        }

        private fun getTimeFormat(): String {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_TIME_FORMAT, TIME_FORMAT_ABSOLUTE).toString()
        }

        private fun getChangeTextColorByVisibility(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_CHANGE_TEXT_COLOR_VISIBILITY, true)
        }

        private fun getIsDialogEnableOnFollow(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_DIALOG_FOLLOW, true)
        }

        private fun setIsDialogEnableOnFollow(isEnable: Boolean) {
            val context = CustomApplication.getApplicationContext()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            with(sharedPreferences.edit()) {
                putBoolean(KEY_DIALOG_FOLLOW, isEnable)
                apply()
            }
        }

        private fun getIsDialogEnableOnFavourite(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_DIALOG_FAVOURITE, true)
        }

        private fun setIsDialogEnableOnFavourite(isEnable: Boolean) {
            val context = CustomApplication.getApplicationContext()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            with(sharedPreferences.edit()) {
                putBoolean(KEY_DIALOG_FAVOURITE, isEnable)
                apply()
            }
        }

        private fun getIsDialogEnableOnBoost(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_DIALOG_BOOST, true)
        }

        private fun setIsDialogEnableOnBoost(isEnable: Boolean) {
            val context = CustomApplication.getApplicationContext()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            with(sharedPreferences.edit()) {
                putBoolean(KEY_DIALOG_BOOST, isEnable)
                apply()
            }
        }

        private fun getIsDialogEnableOnBookmark(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_DIALOG_BOOKMARK, true)
        }

        private fun setIsDialogEnableOnBookmark(isEnable: Boolean) {
            val context = CustomApplication.getApplicationContext()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            with(sharedPreferences.edit()) {
                putBoolean(KEY_DIALOG_BOOKMARK, isEnable)
                apply()
            }
        }

        private fun getIsDialogEnableOnEmojiReaction(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_DIALOG_EMOJI, true)
        }

        private fun setIsDialogEnableOnEmojiReaction(isEnable: Boolean) {
            val context = CustomApplication.getApplicationContext()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            with(sharedPreferences.edit()) {
                putBoolean(KEY_DIALOG_EMOJI, isEnable)
                apply()
            }
        }

        private fun getIsDialogEnableOnUnFollow(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_DIALOG_UNFOLLOW, true)
        }

        private fun setIsDialogEnableOnUnFollow(isEnable: Boolean) {
            val context = CustomApplication.getApplicationContext()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            with(sharedPreferences.edit()) {
                putBoolean(KEY_DIALOG_UNFOLLOW, isEnable)
                apply()
            }
        }

        private fun getIsDialogEnableOnUndoFavourite(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_DIALOG_UNDO_FAVOURITE, true)
        }

        private fun setIsDialogEnableOnUndoFavourite(isEnable: Boolean) {
            val context = CustomApplication.getApplicationContext()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            with(sharedPreferences.edit()) {
                putBoolean(KEY_DIALOG_UNDO_FAVOURITE, isEnable)
                apply()
            }
        }

        private fun getIsDialogEnableOnUndoBoost(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_DIALOG_UNDO_BOOST, true)
        }

        private fun setIsDialogEnableOnUndoBoost(isEnable: Boolean) {
            val context = CustomApplication.getApplicationContext()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            with(sharedPreferences.edit()) {
                putBoolean(KEY_DIALOG_UNDO_BOOST, isEnable)
                apply()
            }
        }

        private fun getIsDialogEnableOnUndoBookmark(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_DIALOG_UNDO_BOOKMARK, true)
        }

        private fun setIsDialogEnableOnUndoBookmark(isEnable: Boolean) {
            val context = CustomApplication.getApplicationContext()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            with(sharedPreferences.edit()) {
                putBoolean(KEY_DIALOG_UNDO_BOOKMARK, isEnable)
                apply()
            }
        }

        private fun getIsDialogEnableOnUndoEmojiReaction(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_DIALOG_UNDO_EMOJI, true)
        }

        private fun setIsDialogEnableOnUndoEmojiReaction(isEnable: Boolean) {
            val context = CustomApplication.getApplicationContext()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            with(sharedPreferences.edit()) {
                putBoolean(KEY_DIALOG_UNDO_EMOJI, isEnable)
                apply()
            }
        }

        private fun getIsDialogEnableOnPostStatus(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_DIALOG_POST_STATUS, true)
        }

        private fun setIsDialogEnableOnPostStatus(isEnable: Boolean) {
            val context = CustomApplication.getApplicationContext()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            with(sharedPreferences.edit()) {
                putBoolean(KEY_DIALOG_POST_STATUS, isEnable)
                apply()
            }
        }

        private fun getIsDialogEnableOnDeleteStatus(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_DIALOG_DELETE_STATUS, true)
        }

        private fun setIsDialogEnableOnDeleteStatus(isEnable: Boolean) {
            val context = CustomApplication.getApplicationContext()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            with(sharedPreferences.edit()) {
                putBoolean(KEY_DIALOG_DELETE_STATUS, isEnable)
                apply()
            }
        }

        private fun getIsShowCard(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_SHOW_CARD, true)
        }

        private fun getApplyBackgroundColorBoostedPosts(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_BOOST_BACKGROUND, true)
        }

        private fun getIsShowQuickPostArea(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_SHOW_QUICK_POST_AREA, false)
        }

        private fun getIsShowQuickPostVisibility(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_SHOW_QUICK_POST_VISIBILITY, true)
        }

        private fun getIsShowQuickPostEmoji(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_SHOW_QUICK_POST_EMOJI, true)
        }

        private fun getIsShowQuickPostHashtag(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_SHOW_QUICK_POST_HASHTAG, true)
        }

        private fun getIsHideActionButtons(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_HIDE_ACTION_BUTTON, false)
        }

        private fun getIsShowVia(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_SHOW_VIA, true)
        }

        private fun getIsStaticTabsWidth(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_TABS_WIDTH_STATIC, false)
        }

        private fun getTabsWidth(): Int {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_TABS_WIDTH, "64")!!.toInt()
        }

        private fun getSendWithEnterKey(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_SEND_ENTER_KEY, false)
        }

        private fun getShowFollowersCount(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_SHOW_FOLLOWERS_COUNT, false)
        }

        private fun getShowReactionsCount(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_SHOW_REACTIONS_COUNT, false)
        }

        private fun getDisableSleep(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_DISABLE_SLEEP, false)
        }

        private fun getIsEnableEmojiAnimation(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_ENABLE_EMOJI_ANIMATION, true)
        }

        private fun getIsEnableAvatarAnimation(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_ENABLE_AVATAR_ANIMATION, true)
        }

        private fun getAvatarShape(): AvatarShape {
            val context = CustomApplication.getApplicationContext()
            val value = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_AVATAR_SHAPE, AvatarShape.CIRCLE.value)!!
            return AvatarShape.valueOf(value.uppercase())
        }

        private fun getIsShowAvatarInColumnHeader(): Boolean {
            val context = CustomApplication.getApplicationContext()
            return androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_AVATAR_HEADER, true)
        }

        private const val KEY_DIALOG_FOLLOW         = "dialog_follow"
        private const val KEY_DIALOG_FAVOURITE      = "dialog_favourite"
        private const val KEY_DIALOG_BOOST          = "dialog_reblog"
        private const val KEY_DIALOG_BOOKMARK       = "dialog_bookmark"
        private const val KEY_DIALOG_EMOJI          = "dialog_emoji"
        private const val KEY_DIALOG_UNFOLLOW       = "dialog_unfollow"
        private const val KEY_DIALOG_UNDO_FAVOURITE = "dialog_undo_favourite"
        private const val KEY_DIALOG_UNDO_BOOST     = "dialog_undo_reblog"
        private const val KEY_DIALOG_UNDO_BOOKMARK  = "dialog_undo_bookmark"
        private const val KEY_DIALOG_UNDO_EMOJI     = "dialog_undo_emoji"
        private const val KEY_DIALOG_POST_STATUS    = "dialog_post"
        private const val KEY_DIALOG_DELETE_STATUS  = "dialog_delete_status"

        private const val KEY_TEXT_SIZE = "font_size"
        private const val KEY_EDITTEXT_SIZE = "edittext_size"
        private const val KEY_CHANGE_TEXT_COLOR_VISIBILITY = "change_text_color_visibility"
        private const val KEY_ENABLE_EMOJI_ANIMATION = "enable_emoji_animation"
        private const val KEY_ENABLE_AVATAR_ANIMATION = "enable_avatar_animation"
        private const val KEY_SHOW_CARD = "show_card"
        private const val KEY_BOOST_BACKGROUND = "boost_background"
        private const val KEY_SHOW_QUICK_POST_AREA = "show_quick_post_area"
        private const val KEY_SHOW_QUICK_POST_VISIBILITY = "show_quick_post_visibility"
        private const val KEY_SHOW_QUICK_POST_EMOJI = "show_quick_post_emoji"
        private const val KEY_SHOW_QUICK_POST_HASHTAG = "show_quick_post_hashtag"
        private const val KEY_HIDE_ACTION_BUTTON = "hide_action_buttons"
        private const val KEY_SHOW_VIA = "show_via"
        private const val KEY_TABS_WIDTH_STATIC = "tabs_width_static"
        private const val KEY_TABS_WIDTH = "tabs_width"
        private const val KEY_SEND_ENTER_KEY = "send_enter_key"
        private const val KEY_SHOW_FOLLOWERS_COUNT = "show_followers_count"
        private const val KEY_SHOW_REACTIONS_COUNT = "show_reactions_count"

        private const val KEY_DISABLE_SLEEP = "disable_sleep"
        private const val KEY_RESIZE_IMAGE_SIZE = "image_size"
        private const val KEY_TIME_FORMAT = "time_format"

        const val TIME_FORMAT_AUTO = "auto"
        const val TIME_FORMAT_RELATIVE = "relative"
        const val TIME_FORMAT_ABSOLUTE = "absolute"
        const val TIME_FORMAT_ABSOLUTE_BY_SECONDS = "absolute_by_seconds"

        private const val KEY_AVATAR_SHAPE ="avatar_shape"
        private const val KEY_AVATAR_HEADER = "show_avatar_header"
    }

    enum class AvatarShape(val value: String) {
        CIRCLE("circle"),
        SQUARE("square"),
        NFT("nft"),
    }
}