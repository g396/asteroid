package sns.asteroid.view.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.api.entities.ListTimeline
import sns.asteroid.databinding.DialogAddColumnBinding
import sns.asteroid.databinding.RowAddColumnBinding
import sns.asteroid.db.entities.Credential
import sns.asteroid.viewmodel.AddListViewModel

/**
 * ユーザが作成したらカスタムのリストを一覧で取得
 * →選択した物をコールバック
 */
class AddListDialog: DialogFragment() {
     private val viewModel: AddListViewModel by viewModels {
        val credential = requireArguments().getSerializable("credential") as Credential
        AddListViewModel.Factory(credential)
    }

    private var _binding: DialogAddColumnBinding? = null
    val binding get() = _binding!!

    private var callback: ListSelectCallback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        _binding = DialogAddColumnBinding.inflate(activity.layoutInflater)

        viewModel.apply {
            lists.observe(activity, Observer {
                val listView = binding.listView
                val adapter = ListColumnAdapter(activity, it)
                listView.adapter = adapter

                listView.visibility = View.VISIBLE
            })
            message.observe(activity, Observer {
                val textView = binding.listIsEmpty
                textView.visibility = View.VISIBLE
                textView.text = it
            })
            }

        binding.spinner.apply {
            visibility = View.GONE
        }

        binding.cancel.apply {
            setOnClickListener { this@AddListDialog.dismiss() }
        }

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            viewModel.getLists()
            binding.progressBar.visibility = View.GONE

        }

        return Dialog(activity).apply {
            setContentView(binding.root)

            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
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
     * ここからインスタンスを作るのだ
     * Fragment系は空のコンストラクタだけを使うのがルールなのだ
     */
    companion object {
        fun newInstance(callback: ListSelectCallback, credential: Credential): AddListDialog {
            val arg = Bundle().apply {
                putSerializable("credential", credential)
            }
            return AddListDialog().apply {
                this.arguments = arg
                this.callback = callback
            }
        }
    }

    interface ListSelectCallback {
        fun onListSelect(list: ListTimeline)
    }

    inner class ListColumnAdapter(val context: Context, val list: List<ListTimeline>) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val binding = if(convertView != null) {
                convertView.tag as RowAddColumnBinding
            } else {
                val inflater = LayoutInflater.from(context)
                RowAddColumnBinding.inflate(inflater, parent, false).apply { root.tag = this }
            }
            val item = list[position]

            binding.iconSubject.apply {
                setImageResource(R.drawable.column_list)
                imageTintList = let {
                    val selectorArray = arrayOf(intArrayOf(0))
                    val colorArray = intArrayOf(viewModel.credential.accentColor)
                    ColorStateList(selectorArray, colorArray)
                }
            }
            binding.subject.apply { text = item.title }
            binding.root.setOnClickListener {
                callback?.onListSelect(item)
                this@AddListDialog.dismiss()
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
    }
}