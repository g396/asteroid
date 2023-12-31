package sns.asteroid.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import sns.asteroid.api.entities.MediaAttachment
import sns.asteroid.databinding.ActivityImageBinding
import sns.asteroid.view.fragment.media.ImageFragment
import sns.asteroid.view.fragment.media.VideoFragment
import sns.asteroid.view.fragment.media.WebViewFragment


class MediaPreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageBinding

    companion object {
        const val STATUS = "status"
        const val INDEX = "index"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val extra = intent.getSerializableExtra(STATUS) as Array<MediaAttachment>? ?: return
        val mediaAttachments = extra.toList()

        val index = intent.getIntExtra(INDEX, 0)

        binding.viewPager.also {
            it.adapter = ImageFragmentPagerAdapter(mediaAttachments)
            it.offscreenPageLimit = mediaAttachments.size
            it.setCurrentItem(index, false)
        }
    }

    /**
     * ページ管理
     * 本当はViewPagerではなくViewPager2を使用したいけど
     * 何故かTouchImageViewやWebViewのスクロールとページ移動が干渉するので
     * そこが解決しないと移行できにゃい
     */
    inner class ImageFragmentPagerAdapter(
        private val mediaAttachments: List<MediaAttachment>,
    ): FragmentPagerAdapter(supportFragmentManager) {

        /**
         * プレビュー用Fragment生成
         *
         * リモートインスタンスのメディアがブロックされている場合等は
         * typeが"unknown"になり、自鯖へのキャッシュもされないので
         * リモートから優先して読込みを行う
         *
         * gif-videoについては、
         * mastodonの鯖でmp4に変換されるのでvideoと同じ扱いにするのが基本だが、
         * remote_urlではgifそのままの可能性があることに注意する
         */
        override fun getItem(position: Int): Fragment {
            val item = mediaAttachments[position]

            // メディアがブロックされている可能性がある場合は、リモートを優先して読込み
            if(item.type == "unknown") {
                val url = item.remote_url
                    ?: return WebViewFragment.newInstance(item.url)

                //TODO:操作性を統一したい
                // ImageFragment -> webpとかのアニメーションが再生できない
                // WebViewFragment -> ダブルタップで拡大できない
                return when (identifyTheMediaType(url)) {
                    "image" -> ImageFragment.newInstance(url)
                    "video" -> VideoFragment.newInstance(url)
                    else -> WebViewFragment.newInstance(url)
                }
            }

            return when(item.type) {
                "image" -> ImageFragment.newInstance(item.url)
                "video" -> VideoFragment.newInstance(item.url)
                "gifv"  -> VideoFragment.newInstance(item.url)
                else    -> WebViewFragment.newInstance(item.url)
            }
        }

        override fun getCount(): Int {
            return mediaAttachments.size
        }

        /**
         * メディアのタイプを拡張子から推定する
         * pawooからの投稿で
         * メディアの取得がブロックされているとメディアのタイプが分からないケースがあったので
         * その対策
         */
        private fun identifyTheMediaType(url: String): String {
            val fileExtension = Regex("(.*)\\.(.*?)").split(url).last()
                .lowercase()

            val images = listOf("jpg", "jpeg", "png", "gif", "heic", "tiff", "bmp")
            val videos = listOf("mp4", "mkv", "avi")

            if (images.contains(fileExtension)) return "image"
            if (videos.contains(fileExtension)) return "video"
            return "webview"
        }
    }
}