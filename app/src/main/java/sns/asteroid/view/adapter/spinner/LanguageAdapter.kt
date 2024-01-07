package sns.asteroid.view.adapter.spinner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Spinner
import android.widget.TextView
import sns.asteroid.R
import sns.asteroid.model.util.ISO639Lang

class LanguageAdapter(
    private val context: Context,
    private val spinner: Spinner,
    private val list: List<ISO639Lang>,
): BaseAdapter() {
    private val layoutInflater: LayoutInflater by lazy { LayoutInflater.from(context) }

    init {
        spinner.adapter = this
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: layoutInflater.inflate(R.layout.row_language, parent, false)

        view.findViewById<TextView>(R.id.text).apply {
            text = list[position].code.uppercase().ifEmpty { "Lang" }
        }

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)

        view.findViewById<TextView>(android.R.id.text1).apply {
            text = list[position].text
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

    fun getCurrentItem(): ISO639Lang {
        return spinner.selectedItem as ISO639Lang
    }
}