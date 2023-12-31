package sns.asteroid.view.adapter.emoji

import sns.asteroid.api.entities.CustomEmoji

interface EmojiAdapterCallback {
    fun onCustomEmojiSelect(customEmoji: CustomEmoji)
    fun onUnicodeEmojiSelect(unicodeString: String)
}