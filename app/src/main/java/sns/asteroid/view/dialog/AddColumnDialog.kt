package sns.asteroid.view.dialog


import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sns.asteroid.R
import sns.asteroid.api.entities.ListTimeline
import sns.asteroid.databinding.DialogAddColumnBinding
import sns.asteroid.databinding.RowAddColumnBinding
import sns.asteroid.databinding.RowCredentialBinding
import sns.asteroid.db.AppDatabase
import sns.asteroid.db.entities.Credential

/**
 * カラムの一覧(LTL、ホーム、連合等）をダイアログで表示
 * →選択した項目をコールバック
 */
class AddColumnDialog:
    DialogFragment(),
    AdapterView.OnItemClickListener,
    AdapterView.OnItemSelectedListener,
    HashtagInputDialog.HashtagDialogListener,
    AddListDialog.ListSelectCallback
{
    private var _binding: DialogAddColumnBinding? = null
    private val binding get() = _binding!!

    private val columnAdapter by lazy { ColumnAdapter(requireContext()) }
    private val credentialAdapter by lazy { AccountAdapter(requireContext()) }

    private var callback: AddColumnDialogCallback? = null

    /**
     * ダイアログを生成する
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        _binding = DialogAddColumnBinding.inflate(activity.layoutInflater)

        binding.listView.also {
            it.adapter = columnAdapter
            it.onItemClickListener = this
        }
        binding.cancel.also {
            it.setOnClickListener { dismiss() }
        }
        binding.spinner.also {
            it.onItemSelectedListener = this
            it.adapter = credentialAdapter
        }

        return Dialog(requireContext()).apply {
            setContentView(binding.root)

            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    /**
     * DBから認証情報（アカウント）を読込
     * →Spinnerで選択可能にする
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val dao = db.credentialDao()

            val accounts = withContext(Dispatchers.IO) { dao.getAll() }
            credentialAdapter.submit(accounts)
            db.close()
        }
    }

    /**
     * 親Activityが再生成されると、空のコンストラクタでインスタンス再生成される為listenerがnullになる
     * →従ってコールバックできなくなる
     * そのような場合はダイアログ自体を閉じてしまう事にする
     */
    override fun onStart() {
        super.onStart()
        if(callback == null) dialog?.cancel()
    }

    /**
     * FragmentでView-bindingする時は
     * メモリリーク対策を忘れずに
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * AdapterView.OnItemClickListener
     * 追加したいカラムを選択した際に呼び出される
     */
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val subject = columnAdapter.getItem(position) as String
        val credential = binding.spinner.selectedItem as Credential

        when(subject) {
            "list" ->
                AddListDialog.newInstance(this, credential).show(requireActivity().supportFragmentManager, "tag")
            "hashtag" ->
                HashtagInputDialog.newInstance(this).show(requireActivity().supportFragmentManager, "tag")
            else ->
                callback?.onColumnSelect(credential, subject).also { dismiss() }
        }
    }

    /**
     * AdapterView.OnItemSelectedListener
     * アカウントを選択した際に呼び出される
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val credential = credentialAdapter.getItem(position) as Credential
        columnAdapter.setColor(credential.accentColor)

        // ちらつき防止の為に、アイコンへ色をセットしてからカラムのリストを表示する
        binding.listView.visibility = View.VISIBLE
    }

    /**
     * AdapterView.OnItemSelectedListener
     * 未使用
     */
    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    /**
     * 別ダイアログでハッシュタグを入力した際に呼び出される
     */
    override fun onInputHashtag(hashtag: String) {
        if(hashtag.isBlank()) return
        val credential = binding.spinner.selectedItem as Credential
        callback?.onColumnSelect(credential, "hashtag", hashtag, "#${hashtag}")
        dismiss()
    }

    /**
     * 別ダイアログでユーザの作成したリストを選択した際に呼び出される
     */
    override fun onListSelect(list: ListTimeline) {
        val credential = binding.spinner.selectedItem as Credential
        callback?.onColumnSelect(credential, "list", list.id, list.title)
        dismiss()
    }

    /**
     * ここからインスタンスを作るのだ
     * Fragment系は空のコンストラクタだけを使うのがルールなのだ
     */
    companion object {
        @JvmStatic
        fun newInstance(callback: AddColumnDialogCallback): AddColumnDialog {
            return AddColumnDialog().apply {
                this.callback = callback
            }
        }
    }

    interface AddColumnDialogCallback {
        fun onColumnSelect(credential: Credential, subject: String, optionId: String = "", optionTitle: String = "")
    }

    class AccountAdapter(val context: Context): BaseAdapter() {
        private var accounts: List<Credential> = listOf()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val binding = if(convertView != null) {
                convertView.tag as RowCredentialBinding
            } else {
                val inflater = LayoutInflater.from(context)
                RowCredentialBinding.inflate(inflater, parent, false).apply { root.tag = this }
            }

            binding.credential = accounts[position]
            binding.menu.visibility = View.GONE
            binding.buttonSort.visibility = View.GONE

            return binding.root
        }

        override fun getCount(): Int {
            return accounts.size
        }

        override fun getItem(position: Int): Any {
            return accounts[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        fun submit(list: List<Credential>) {
            accounts = list
            notifyDataSetChanged()
        }
    }

    class ColumnAdapter(val context: Context): BaseAdapter() {
        private var colorStateList: ColorStateList? = null
        private val list = listOf(
            "local",
            "home",
            "mix",
            "public",
            "local_media",
            "public_media",
            "notification",
            "mention",
            "favourites",
            "bookmarks",
            "list",
            "hashtag"
        )

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val binding = if(convertView != null) {
                convertView.tag as RowAddColumnBinding
            } else {
                val inflater = LayoutInflater.from(context)
                RowAddColumnBinding.inflate(inflater, parent, false).apply { root.tag = this }
            }

            val column = list[position]

            binding.iconSubject.apply {
                val resource = when(column) {
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
                    "hashtag" -> R.drawable.hashtag
                    else -> return@apply
                }
                setImageResource(resource)
                colorStateList?.let { imageTintList = colorStateList}
            }
            binding.subject.apply {
                text = when(column) {
                    "local"         -> context.getString(R.string.column_local)
                    "home"          -> context.getString(R.string.column_home)
                    "public"        -> context.getString(R.string.column_public)
                    "mix"           -> context.getString(R.string.column_mix)
                    "local_media"   -> context.getString(R.string.column_local_media)
                    "public_media"  -> context.getString(R.string.column_public_media)
                    "list"          -> context.getString(R.string.column_list)
                    "favourites"    -> context.getString(R.string.column_favourites)
                    "bookmarks"     -> context.getString(R.string.column_bookmarks)
                    "notification"  -> context.getString(R.string.column_notifications)
                    "mention"       -> context.getString(R.string.column_mention)
                    "hashtag"       -> context.getString(R.string.column_hashtag)
                    else            -> "Unknown"
                }
            }

            return binding.root
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

        fun setColor(color: Int) {
            colorStateList = let {
                val selectorArray = arrayOf(intArrayOf(0))
                val colorArray = intArrayOf(color)
                ColorStateList(selectorArray, colorArray)
            }
            notifyDataSetChanged()
        }
    }
}