package sns.asteroid.view.adapter.time

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.view.setPadding

class TimeSpinnerAdapter (
    val context: Context,
    val list: List<Int>,
): BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val textView = TextView(context).apply {
            text = String.format("%02d", list[position])
            val density = context.resources.displayMetrics.density
            setPadding((4 * density).toInt())
        }
        return textView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return super.getDropDownView(position, convertView, parent).apply {
            /*
            parent?.layoutParams?.let {
                val density = context.resources.displayMetrics.density
                it.height = (100 * density).toInt()
                parent.layoutParams = it
            }
             */
        }
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
}