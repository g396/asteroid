package sns.asteroid.view.adapter.sort

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.view.adapter.SpaceAdapter

/**
 * 並び替え可能なRecyclerViewで並び替え検知するやつ
 */
class ItemDragCallback(val callback: ItemMoveListener): ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    ItemTouchHelper.ACTION_STATE_IDLE,
) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // ConcatAdapterを使用している場合は
        // 結合している２つ以上のAdapterに対して作用するので注意
        if(target is SpaceAdapter.ViewHolder) return true

        val fromPos = viewHolder.bindingAdapterPosition
        val toPos = target.bindingAdapterPosition

        callback.onItemMoved(fromPos, toPos)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    interface ItemMoveListener {
        fun onItemMoved(fromPosition: Int, toPosition: Int)
    }
}