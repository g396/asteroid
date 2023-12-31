package sns.asteroid.model.emoji

import android.widget.EditText
import sns.asteroid.api.entities.CustomEmoji

/**
 * テキストから絵文字のショートコードを正規表現で拾ってimgタグに置き換えるやつ
 */
class CustomEmojiParser(val text: String) {
    companion object {
        fun parse(text: String, emojis: List<CustomEmoji>): String {
            var res = text
            emojis.forEach { emoji -> res = parse(res, emoji) }
            return parseHtmlSpace(res)
        }

        private fun parse(text: String, emoji: CustomEmoji): String {
            val regex = Regex(":${emoji.shortcode}:")
            return regex.replace(text, "<img src=\"${emoji.url}\">").apply {
            }
        }

        private fun parseHtmlSpace(text: String): String {
            val hankakuSpaceReplace = "(>[^<]*?)( +)([^<]*<)" // Htmlタグに囲われた文字列、かつ半角スペースを含むもの

            val transform: (MatchResult) -> CharSequence = {
                val group = it.groupValues


                if (group.size < 4) {
                    it.value
                } else {
                    val spaces = StringBuilder().apply {
                        val spaceLength = group[2].length
                        val space = "&nbsp;"
                        for(i in 1..spaceLength) { append(space) }
                    }

                    val string = group[1] + spaces + group[3]
                    parseHtmlSpace(string)
                }
            }

            return Regex(hankakuSpaceReplace).replace(text,  transform)
        }

        fun inputEmojiToEditText(editText: EditText, emoji: CustomEmoji) {
            editText.apply {
                val start = text.substring(0, selectionStart)
                val end = text.substring(selectionEnd, text.length)

                val shortCode = StringBuilder().let {
                    if(!start.endsWith(" ")) it.append(" ")
                    it.append(":${emoji.shortcode}:")
                    if(!end.startsWith(" ") or end.isEmpty()) it.append(" ")
                    it.toString()
                }

                val str = StringBuilder().apply {
                    append(start)
                    append(shortCode)
                    append(end)
                    toString()
                }
                val current = selectionStart + shortCode.length
                setText(str.toString())

                try {
                    setSelection(current)
                } catch (e: IndexOutOfBoundsException) {
                    setSelection(editText.text.length)
                }
            }
        }

        fun inputEmojiToEditText(editText: EditText, unicodeString: String) {
            editText.apply {
                val start = text.substring(0, selectionStart)
                val end = text.substring(selectionEnd, text.length)

                val str = StringBuilder().apply {
                    append(start)
                    append(unicodeString)
                    append(end)
                    toString()
                }
                val current = selectionStart + unicodeString.length
                setText(str.toString())

                try {
                    setSelection(current)
                } catch (e: IndexOutOfBoundsException) {
                    setSelection(editText.text.length)
                }
            }
        }
    }
}