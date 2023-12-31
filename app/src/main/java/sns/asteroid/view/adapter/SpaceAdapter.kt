package sns.asteroid.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.databinding.SpaceBinding

class SpaceAdapter(val context: Context, val height: Int):
    RecyclerView.Adapter<SpaceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = SpaceBinding.inflate(inflater)
        return ViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return height
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view)
}