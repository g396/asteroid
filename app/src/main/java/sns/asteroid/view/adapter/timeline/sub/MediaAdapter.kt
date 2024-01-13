package sns.asteroid.view.adapter.timeline.sub

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wolt.blurhashkt.BlurHashDecoder
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.entities.MediaAttachment
import sns.asteroid.databinding.RowMediaBinding
import sns.asteroid.view.adapter.ContentDiffUtil
import sns.asteroid.view.adapter.timeline.EventsListener

class MediaAdapter (
    private val context: Context,
    private val listener: EventsListener,
    private val spanSize: Int,
): ListAdapter<MediaAttachment, MediaAdapter.ViewHolder>(ContentDiffUtil()) {
    private var isSensitive = true

    companion object {
        private val blurHashImages = mutableMapOf<String, Bitmap>()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = GridLayoutManager(context, spanSize).also {
            it.spanSizeLookup = MediaSpanSizeLookUp()
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = RowMediaBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageView = holder.binding.image

        if ((spanSize == 2) and (itemCount % 2 == 1)) {
            if (position == 0) {
                holder.binding.enableMarginStart = false
                holder.binding.enableMarginEnd = false
            } else if (position % 2 == 0) {
                holder.binding.enableMarginStart = true
                holder.binding.enableMarginEnd = false
            } else {
                holder.binding.enableMarginStart = false
                holder.binding.enableMarginEnd = true
            }
        } else {
            if (position % spanSize == 0) {
                holder.binding.enableMarginStart = false
                holder.binding.enableMarginEnd = true
            } else if (position % spanSize == (spanSize-1)) {
                holder.binding.enableMarginStart = true
                holder.binding.enableMarginEnd = false
            } else {
                holder.binding.enableMarginStart = true
                holder.binding.enableMarginEnd = true
            }
        }

        val media = getItem(position).also { media ->
            imageView.setOnClickListener { listener.onMediaClick(currentList, position) }
            holder.binding.description = media.description
        }

        if (media.type == "audio") {
            imageView.setImageResource(R.drawable.audiofile)
            return
        }

        val ratio = media.meta?.original?.aspect ?: 1.0
        val blurHashImg = blurHashImages[media.id]
            ?: BlurHashDecoder.decode(media.blurhash, (108 * ratio).toInt(), 108).also {
                if (it != null) { blurHashImages[media.id] = it }
            } ?: return

        if(isSensitive) {
            imageView.setImageBitmap(blurHashImg)
        } else {
            val blurHashDrawable = blurHashImg.toDrawable(CustomApplication.getApplicationContext().resources)
            Glide.with(context)
                .load(media.preview_url)
                .placeholder(blurHashDrawable)
                .fitCenter()
                .into(imageView)
        }
    }

    fun submitList(list: List<MediaAttachment>, isSensitive: Boolean) {
        this.isSensitive = isSensitive
        submitList(list)
    }

    class ViewHolder(val binding: RowMediaBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * 画像の枚数が奇数のときは1枚目を横幅いっぱいに表示する
     */
    inner class MediaSpanSizeLookUp: SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return if ((spanSize == 2) and (currentList.size % 2 == 1) and (position == 0))
                2
            else
                1
        }
    }
}