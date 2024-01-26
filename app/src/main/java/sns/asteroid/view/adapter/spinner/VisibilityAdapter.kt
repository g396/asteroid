package sns.asteroid.view.adapter.spinner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import sns.asteroid.R

class VisibilityAdapter(
    private val context: Context,
    private val spinner: Spinner,
): BaseAdapter() {
    private val layoutInflater: LayoutInflater by lazy { LayoutInflater.from(context) }

    val list = arrayOf(
        Visibility.DEFAULT,
        Visibility.PUBLIC,
        Visibility.UNLISTED,
        Visibility.PRIVATE,
        Visibility.DIRECT
    )

    init {
        spinner.adapter = this
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: layoutInflater.inflate(R.layout.visibility, parent, false)

        view.findViewById<ImageView>(R.id.visibilityIcon).apply {
            val icon = when(list[position]) {
                Visibility.DEFAULT -> R.drawable.visibility
                Visibility.PUBLIC -> R.drawable.visibility_public
                Visibility.UNLISTED -> R.drawable.visibility_unlisted
                Visibility.PRIVATE -> R.drawable.visibility_locked
                Visibility.DIRECT -> R.drawable.visibility_direct
            }
            icon?.let { setImageResource(it) }
        }

        view.findViewById<TextView>(R.id.visibilityText).apply {
            visibility = View.GONE
        }

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: layoutInflater.inflate(R.layout.visibility, parent, false)

        view.findViewById<ImageView>(R.id.visibilityIcon).apply {
            val icon = when(list[position]) {
                Visibility.DEFAULT -> R.drawable.visibility
                Visibility.PUBLIC -> R.drawable.visibility_public
                Visibility.UNLISTED -> R.drawable.visibility_unlisted
                Visibility.PRIVATE -> R.drawable.visibility_locked
                Visibility.DIRECT -> R.drawable.visibility_direct
            }
            icon?.let { setImageResource(it) }
        }

        view.findViewById<TextView>(R.id.visibilityText).apply {
            text = when(list[position]) {
                Visibility.DEFAULT -> context.getString(R.string.visibility_server)
                Visibility.PUBLIC -> context.getString(R.string.visibility_public)
                Visibility.UNLISTED -> context.getString(R.string.visibility_unlisted)
                Visibility.PRIVATE -> context.getString(R.string.visibility_private)
                Visibility.DIRECT -> context.getString(R.string.visibility_direct)
            }
        }

        return view
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getCurrentItem(): String {
        val item = spinner.selectedItem as Visibility
        return item.value
    }

    companion object {
        /**
         * visibilityの文字列に対応する位置(ドロップダウンリスト上の何番目の項目か)を返す
         * spinnerに対してsetSelection()する際に使用
         */
        fun getPosition(string: String?): Int {
            return when(string) {
                "public"    -> 1
                "unlisted"  -> 2
                "private"   -> 3
                "direct"    -> 4
                else        -> 0
            }
        }

        fun getVisibility(position: Int): String {
            return when(position) {
                1 -> "public"
                2 -> "unlisted"
                3 -> "private"
                4 -> "direct"
                else -> ""
            }
        }
    }

    enum class Visibility(val value: String) {
        DEFAULT(""),
        PUBLIC("public"),
        UNLISTED("unlisted"),
        PRIVATE("private"),
        DIRECT("direct"),
    }
}