package sns.asteroid.view.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.databinding.ActivityManageColumnsBinding
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.db.ManageColumnsAdapter
import sns.asteroid.view.adapter.SpaceAdapter
import sns.asteroid.view.adapter.sort.ItemDragCallback
import sns.asteroid.view.dialog.AddColumnDialog
import sns.asteroid.viewmodel.ManageColumnsViewModel

/**
 * カラムの一覧を表示
 * 追加・削除や並び替えを行う
 */
class ManageColumnsActivity:
    AppCompatActivity(),
    ItemDragCallback.ItemMoveListener,
    AddColumnDialog.AddColumnDialogCallback,
    ManageColumnsAdapter.AdapterCallback
{
    private val viewModel: ManageColumnsViewModel by viewModels()
    private lateinit var binding: ActivityManageColumnsBinding

    private val columnAdapter by lazy {
        val itemDragCallback = ItemDragCallback(this)
        val helper = ItemTouchHelper(itemDragCallback).also { it.attachToRecyclerView(binding.recyclerView) }
        ManageColumnsAdapter(this, this, helper)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageColumnsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setTitle(R.string.title_manage_columns)

        binding.floatingActionButton.setOnClickListener {
            AddColumnDialog.newInstance(this).show(supportFragmentManager, "add")
        }

        binding.recyclerView.also {
            it.adapter = ConcatAdapter().apply {
                addAdapter(columnAdapter)
                addAdapter(SpaceAdapter(this@ManageColumnsActivity, 1))
            }
            it.layoutManager = LinearLayoutManager(this@ManageColumnsActivity)
        }

        viewModel.columns.observe(this, Observer {
            val currentSize = columnAdapter.currentList.size

            val commitCallback = Runnable {
                if(currentSize == 0) binding.recyclerView.scrollToPosition(0)
                else if(currentSize < it.size) binding.recyclerView.scrollToPosition(it.size-1)
            }
            columnAdapter.submitList(it, commitCallback)
        })
        viewModel.toastMessage.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })
    }

    /**
     * 画面再表示毎にDBから最新の情報を持ってくる
     */
    override fun onStart() {
        super.onStart()
        lifecycleScope.launch { viewModel.getAll() }
    }

    /**
     * リストの要素をドラッグして移動した際に呼び出される
     * カラムの並び替えをDBに反映させる
     */
    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        lifecycleScope.launch { viewModel.move(fromPosition, toPosition) }
    }

    /**
     * 各カラムに対する削除ボタンを押した際に呼び出される
     * カラムの削除をDBに反映させる
     */
    override fun onRemoveButtonClick(item: Pair<ColumnInfo, Credential>) {
        lifecycleScope.launch { viewModel.deleteItem(item) }
    }

    /**
     * ダイアログから追加したいカラムを選択した際に呼び出される
     * カラムの追加をDBに反映させる
     */
    override fun onColumnSelect(credential: Credential, subject: String, optionId: String, optionTitle: String) {
        lifecycleScope.launch {
            val priority = columnAdapter.currentList.size
            val column = ColumnInfo(credential.acct, subject, optionId, optionTitle, priority)
            viewModel.addItem(Pair(column, credential))
        }
    }
}