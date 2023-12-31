package sns.asteroid.view.adapter.db

import android.content.Context
import android.view.*
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.R
import sns.asteroid.databinding.RowCredentialBinding
import sns.asteroid.db.entities.Credential

class CredentialAdapter(
    private val context: Context,
    private val listener: ItemListener,
    private val itemTouchHelper: ItemTouchHelper? = null,
    private val isShowButton: Boolean = false,
): ListAdapter<Credential, CredentialAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = RowCredentialBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val item = getItem(position)
        binding.credential = item

        binding.menu.setOnClickListener(OnMenuClickListener(item))
        binding.icon.setOnClickListener { listener.onAvatarClick(item) }
        binding.root.setOnClickListener { listener.onCredentialSelect(item) }

        val onTouchListener = OnTouchListener { v, event ->
            if(event.actionMasked == MotionEvent.ACTION_DOWN) {
                itemTouchHelper?.startDrag(holder)
                v.performClick()
                true
            } else {
                false
            }
        }

        binding.buttonSort.apply { setOnTouchListener(onTouchListener) }
    }

    fun remove(credential: Credential) {
        val list = currentList.toMutableList().apply {
            removeIf { it.acct == credential.acct }
        }
        submitList(list)
    }

    fun moveItem(from: Int, to: Int) {
        val list = currentList.toMutableList().also {
            val moving = it[from]
            it.remove(moving)
            it.add(to, moving)
        }
        for((index, item) in list.withIndex()) {
            item.priority = index
        }
        submitList(list)
    }

    inner class OnMenuClickListener(private val credential: Credential): OnClickListener {
        override fun onClick(v: View?) {
            val popupMenu = PopupMenu(context, v)

            popupMenu.menu.apply {
                add(Menu.NONE , Menu.FIRST + 0, Menu.NONE + 0, context.getString(R.string.menu_manage_profile))
                add(Menu.NONE , Menu.FIRST + 1, Menu.NONE + 1, context.getString(R.string.menu_change_color))
                add(Menu.NONE , Menu.FIRST + 2, Menu.NONE + 2, context.getString(R.string.menu_remove))
            }
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId) {
                    Menu.FIRST + 0 -> listener.onMenuItemClick(credential, Item.MANAGE_PROFILE)
                    Menu.FIRST + 1 -> listener.onMenuItemClick(credential, Item.CHANGE_COLOR)
                    Menu.FIRST + 2 -> listener.onMenuItemClick(credential, Item.REMOVE)
                }
                false
            }
            popupMenu.show()
        }
    }

    inner class ViewHolder(val binding: RowCredentialBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.buttonSort.visibility = if(isShowButton) View.VISIBLE else View.GONE
            binding.menu.visibility = if (isShowButton) View.VISIBLE else View.GONE
        }
    }

    class Diff: DiffUtil.ItemCallback<Credential>() {
        override fun areItemsTheSame(oldItem: Credential, newItem: Credential): Boolean {
            return oldItem.acct == newItem.acct
        }
        override fun areContentsTheSame(oldItem: Credential, newItem: Credential): Boolean {
            return oldItem == newItem
        }
    }

    interface ItemListener {
        fun onCredentialSelect(credential: Credential)
        fun onAvatarClick(credential: Credential)
        fun onMenuItemClick(credential: Credential, item: Item)
    }

    enum class Item(val value: Int) {
        MANAGE_PROFILE(0),
        CHANGE_COLOR(1),
        REMOVE(2),
    }
}