package sns.asteroid.view.adapter.spinner

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import sns.asteroid.R
import sns.asteroid.databinding.NavHeaderDrawerBinding
import sns.asteroid.db.entities.Credential

/**
 * 参照中のアカウントを切り替えるSpinner
 * (ドロワー上部にacctを表示)
 */
class DrawerCredentialAdapter(
    context: Context,
    private val binding: NavHeaderDrawerBinding,
) : BaseAdapter(), AdapterView.OnItemSelectedListener {
    private var list: List<Credential> = listOf()
    private val layoutInflater = LayoutInflater.from(context)

    init {
        binding.spinner.adapter = this
        binding.spinner.onItemSelectedListener = this
    }

    companion object {
        const val MIN_TEXT_SIZE = 8
        const val MAX_TEXT_SIZE = 16
        const val STEP_GRANULARITY = 1
        const val UNIT = 1
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)

        view.findViewById<TextView>(android.R.id.text1)?.apply {
            maxLines = 1
            setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            setAutoSizeTextTypeUniformWithConfiguration(MIN_TEXT_SIZE, MAX_TEXT_SIZE, STEP_GRANULARITY, UNIT)
            text = list[position].acct
            setTextColor(list[position].accentColor)
            setTypeface(null, Typeface.BOLD)

        }
        return view
    }
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false).apply {
            setBackgroundColor(resources.getColor(R.color.cardview_background))
        }

        view.findViewById<TextView>(android.R.id.text1)?.apply {
            setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            setAutoSizeTextTypeUniformWithConfiguration(MIN_TEXT_SIZE, MAX_TEXT_SIZE, STEP_GRANULARITY, UNIT)
            text = list[position].acct
            setTextColor(list[position].accentColor)
            setTypeface(null, Typeface.BOLD)

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

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        binding.credential = list[position]
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    fun submitList(credentials: List<Credential>) {
        list = credentials
        notifyDataSetChanged()

        val current = binding.spinner.selectedItem as Credential?
        binding.credential = current
    }

    fun setSelection(credential: Credential) {
        val index = list.indexOf(credential)
        if (index != -1) binding.spinner.setSelection(index)
    }
}