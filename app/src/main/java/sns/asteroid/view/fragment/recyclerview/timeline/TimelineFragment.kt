package sns.asteroid.view.fragment.recyclerview.timeline

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.ListAdapter
import sns.asteroid.R
import sns.asteroid.api.entities.Account
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.timeline.EventsListener
import sns.asteroid.view.adapter.timeline.TimelineAdapter
import sns.asteroid.view.fragment.recyclerview.RecyclerViewFragment
import sns.asteroid.viewmodel.recyclerview.timeline.TimelineViewModel


/**
 * タイムラインを表示するためのFragment
 * リストで並べたいエンティティがStatusの場合はこれを使い回す
 * (参照するViewModelを変えて取得する項目を変える)
 *
 * ストリーミングAPI対応の場合はこれを継承した別クラスを使用する
 *
 * また、通知画面では扱うエンティティが異なるので別クラスを使用する
 */
open class TimelineFragment: RecyclerViewFragment<Status>(), EventsListener {
    override val viewModel: TimelineViewModel by viewModels {
        val column = requireArguments().get("column") as ColumnInfo
        val credential = requireArguments().get("credential") as Credential
        TimelineViewModel.Factory(column, credential)
    }
    override val recyclerViewAdapter: ListAdapter<Status, *> by lazy {
        val column = requireArguments().get("column") as ColumnInfo

        // use for filter
        val columnContext = when(column.subject) {
            "home"          -> "home"
            "local"         -> "public"
            "public"        -> "public"
            "mix"           -> "public"
            "local_media"   -> "public"
            "public_media"  -> "public"
            "user_pin"      -> "account"
            "user_posts"    -> "account"
            "user_media"    -> "account"
            else            -> "public"
        }

        TimelineAdapter(
            context =       requireContext(),
            myAccountId =   viewModel.credential.value!!.account_id,
            listener =      this@TimelineFragment,
            columnContext = columnContext,
        )
    }
    override val title by lazy {
        val column = requireArguments().get("column") as ColumnInfo?
        when (column?.subject) {
            "local"         -> getString(R.string.column_local)
            "home"          -> getString(R.string.column_home)
            "public"        -> getString(R.string.column_public)
            "mix"           -> getString(R.string.column_mix)
            "local_media"   -> getString(R.string.column_local_media)
            "public_media"  -> getString(R.string.column_public_media)
            "user_pin"      -> column.option_title.ifBlank { column.option_id }
            "user_posts"    -> column.option_title.ifBlank { column.option_id }
            "user_media"    -> column.option_title.ifBlank { column.option_id }
            "list"          -> column.option_title.ifBlank { getString(R.string.column_list) }
            "hashtag"       -> column.option_title
            "favourites"    -> getString(R.string.column_favourites)
            "bookmarks"     -> getString(R.string.column_bookmarks)
            "mention"       -> getString(R.string.column_mention)
            else            -> column?.option_title?.ifBlank { "Unknown" } ?: "null"
        }
    }

    override val lifecycleScope: LifecycleCoroutineScope
        get() = lifecycle.coroutineScope

    override fun onAccountClick(acct: String) {
        super<EventsListener>.onAccountClick(acct)
    }

    override fun onAccountClick(account: Account) {
        super<EventsListener>.onAccountClick(account)
    }

    companion object {
        @JvmStatic
        fun newInstance(data: Pair<ColumnInfo, Credential>, showAddMenu: Boolean = false, hideHeader: Boolean = false): TimelineFragment {
            val bundle = Bundle().apply {
                putSerializable("column", data.first)
                putSerializable("credential", data.second)
                putBoolean("show_add_menu", showAddMenu)
                putSerializable("hide_header", hideHeader)
            }
            return TimelineFragment().apply {
                arguments = bundle
            }
        }
    }
}
