package sns.asteroid.view.adapter.poll

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.R
import sns.asteroid.api.entities.Poll
import sns.asteroid.databinding.RowPollBinding

class PollAdapter(
    private val context: Context,
): ListAdapter<Pair<Poll.Option, Boolean>, PollAdapter.ViewHolder>(Diff()) {
    private var ownVotes: List<Int> = listOf()
    private var multiple = false

    private val checked = mutableSetOf<Int>()
    private var checkedRadioButton: RadioButton? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = RowPollBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val item = getItem(position).first
        val votedOrExpired = getItem(position).second

        binding.checkBox.visibility = if(!votedOrExpired and multiple) View.VISIBLE else View.GONE
        binding.radioButton.visibility = if(!votedOrExpired and !multiple) View.VISIBLE else View.GONE

        binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if(checked.contains(position)) checked.remove(position)
            else checked.add(position)
        }
        binding.radioButton.setOnClickListener {
            if (it == checkedRadioButton) return@setOnClickListener
            checked.clear()
            checked.add(position)
            checkedRadioButton?.isChecked = false
            checkedRadioButton = it as RadioButton
        }

        binding.root.setOnClickListener {
            (it.parent as RecyclerView).callOnClick()
        }
        binding.votesCount.setOnClickListener {
            (it.parent as ConstraintLayout).callOnClick()
        }
        binding.votesGraph.setOnClickListener {
            (it.parent as ConstraintLayout).callOnClick()
        }

        binding.content = item.parsedTitle
        binding.votesCount.text = let {
            val value = item.votes_count.toString()
            val percent = (item.votesRatio * 100).toInt()
            "$value ($percent%)"
        }

        // 幅のパーセント指定はConstraintLayout全体に対する比率なので
        // グラフの部分だけ別のConstraintLayoutを用意しないと見切れてしまう
        ConstraintSet().apply {
            val ratio = if (item.votesRatio > 0.01f) item.votesRatio else 0.01f
            clone(binding.graphLayout)
            constrainPercentWidth(R.id.votesGraph, ratio)
            applyTo(binding.graphLayout)
        }

        if (ownVotes.contains(position))
            binding.votesGraph.setBackgroundResource(R.drawable.votes_voted)
        else
            binding.votesGraph.setBackgroundResource(R.drawable.votes)
    }

    fun getCheckedList(): List<Int> {
        return checked.toList()
    }

    fun submitPoll(poll: Poll) {
        ownVotes = poll.own_votes ?: emptyList()
        multiple = poll.multiple

        val option = poll.options
            .associateWith { (poll.voted ?: false) or poll.expired }
            .toList()
        submitList(option)
    }

    inner class ViewHolder(val binding: RowPollBinding): RecyclerView.ViewHolder(binding.root)

    private class Diff: DiffUtil.ItemCallback<Pair<Poll.Option, Boolean>>() {
        override fun areItemsTheSame(oldItem: Pair<Poll.Option, Boolean>, newItem: Pair<Poll.Option, Boolean>): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
        override fun areContentsTheSame(oldItem: Pair<Poll.Option, Boolean>, newItem: Pair<Poll.Option, Boolean>): Boolean {
            return oldItem == newItem
        }
    }
}