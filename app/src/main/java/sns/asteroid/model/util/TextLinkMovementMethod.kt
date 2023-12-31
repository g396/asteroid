package sns.asteroid.model.util

import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.MotionEvent
import android.widget.TextView

class TextLinkMovementMethod(val callback: LinkCallback): LinkMovementMethod() {
    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {}
            else -> return super.onTouchEvent(widget, buffer, event)
        }

        val x = event.x.toInt() - widget.totalPaddingLeft + widget.scrollX
        val y = event.y.toInt() - widget.totalPaddingTop + widget.scrollY

        val index = let {
            val line = widget.layout.getLineForVertical(y)
            widget.layout.getOffsetForHorizontal(line, x.toFloat())
        }

        val link = buffer.getSpans(index, index, ClickableSpan::class.java).firstOrNull()
            ?: return super.onTouchEvent(widget, buffer, event)

        val linkString = let {
            val start = buffer.getSpanStart(link)
            val end = buffer.getSpanEnd(link)
            buffer.substring(start, end)
        }

        val regExWebFinger = Regex("(@(\\w+)@([^/\\n]+))(?!(/|.)+)")

        return if(regExWebFinger.matches(linkString)) {
            val acctStartsWithAtMark = regExWebFinger.find(linkString)!!.value
            callback.onWebFingerClick(acctStartsWithAtMark.substring(1))
            true
        } else if(Regex("(.+?)/@([^/\\n]+)").matches(linkString)) {
            (link as? URLSpan)?.url?.let { callback.onAccountURLClick(it) }
            true
        } else if(linkString.startsWith("@")) {
            (link as? URLSpan)?.url?.let { callback.onAccountURLClick(it) }
            true
        } else if(linkString.startsWith("#")) {
            callback.onHashtagClick(linkString.substring(1))
            true
        } else {
            super.onTouchEvent(widget, buffer, event)
        }
    }

    interface LinkCallback {
        fun onHashtagClick(hashtag: String)
        fun onWebFingerClick(acct: String)
        fun onAccountURLClick(url: String)
    }
}