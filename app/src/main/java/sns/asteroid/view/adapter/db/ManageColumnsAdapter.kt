package sns.asteroid.view.adapter.db

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.R
import sns.asteroid.databinding.RowManageColumnsBinding
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential

class ManageColumnsAdapter(
    private val context: Context,
    private val callback: AdapterCallback,
    private val itemTouchHelper: ItemTouchHelper,
): ListAdapter<Pair<ColumnInfo, Credential>, ManageColumnsAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = RowManageColumnsBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val item = getItem(position)

        binding.columnTitle.apply {
            text = when(item.first.subject) {
                "local"         -> context.getString(R.string.column_local)
                "home"          -> context.getString(R.string.column_home)
                "public"        -> context.getString(R.string.column_public)
                "mix"           -> context.getString(R.string.column_mix)
                "local_media"   -> context.getString(R.string.column_local_media)
                "public_media"  -> context.getString(R.string.column_public_media)
                "list"          -> item.first.option_title.ifBlank { context.getString(R.string.column_list) }
                "favourites"    -> context.getString(R.string.column_favourites)
                "bookmarks"     -> context.getString(R.string.column_bookmarks)
                "notification"  -> context.getString(R.string.column_notifications)
                "mention"       -> context.getString(R.string.column_mention)
                "hashtag"       -> item.first.option_title
                "trends_hashtags" -> context.getString(R.string.column_trends)
                "trends_statuses" -> context.getString(R.string.column_trends)
                else            -> item.first.option_title.ifBlank { "Unknown" }
            }
        }

        binding.acct.apply {
            text = item.first.acct
        }

        binding.iconSubject.apply {
            val resource = when(item.first.subject) {
                "local" -> R.drawable.column_local
                "home" -> R.drawable.column_home
                "public" -> R.drawable.column_public
                "mix" -> R.drawable.column_mix
                "local_media" -> R.drawable.image
                "public_media" -> R.drawable.image
                "notification" -> R.drawable.column_notification
                "mention" -> R.drawable.mention
                "favourites" -> R.drawable.column_favourite
                "bookmarks" -> R.drawable.column_bookmarks
                "list" -> R.drawable.column_list
                "user_posts" -> R.drawable.user
                "user_media" -> R.drawable.image
                "hashtag" -> R.drawable.hashtag
                "trends_hashtags" -> R.drawable.trends
                "trends_statuses" -> R.drawable.trends
                else -> return@apply
            }
            setImageResource(resource)

            imageTintList = let {
                val selectorArray = arrayOf(intArrayOf(0))
                val colorArray = intArrayOf(item.second.accentColor)
                ColorStateList(selectorArray, colorArray)
            }

        }


        binding.buttonSort.apply {
            setOnTouchListener { v, event ->
                if(event.actionMasked == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder)
                    performClick()
                    return@setOnTouchListener true
                }
                return@setOnTouchListener false
            }
        }

        binding.remove.apply {
            setOnClickListener { callback.onRemoveButtonClick(item) }
        }
    }

    interface AdapterCallback {
        fun onRemoveButtonClick(item: Pair<ColumnInfo, Credential>)
    }

    class ViewHolder(val binding: RowManageColumnsBinding) : RecyclerView.ViewHolder(binding.root)

    class Diff: DiffUtil.ItemCallback<Pair<ColumnInfo, Credential>>() {
        override fun areItemsTheSame(oldItem: Pair<ColumnInfo, Credential>, newItem: Pair<ColumnInfo, Credential>): Boolean {
            return oldItem.first.hash == newItem.first.hash
        }

        override fun areContentsTheSame(oldItem: Pair<ColumnInfo, Credential>, newItem: Pair<ColumnInfo, Credential>): Boolean {
            return oldItem == newItem
        }
    }
}