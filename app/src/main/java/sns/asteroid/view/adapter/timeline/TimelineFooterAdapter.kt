package sns.asteroid.view.adapter.timeline

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.databinding.RowReadMoreBinding

/**
 * タイムラインのフッター
 * 過去の投稿を読み込むためのボタンとプログレスバーを表示する
 */
class TimelineFooterAdapter(
    val context: Context,
    val listener: OnClickListener,
): RecyclerView.Adapter<TimelineFooterAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = RowReadMoreBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        binding.buttonReadMore.setOnClickListener {
            listener.onReadMoreClick(binding.progressBarReadMore, binding.buttonReadMore)
        }
    }

    inner class ViewHolder(val binding: RowReadMoreBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnClickListener {
        fun onReadMoreClick(progressBar: ProgressBar, button: Button)
    }
}