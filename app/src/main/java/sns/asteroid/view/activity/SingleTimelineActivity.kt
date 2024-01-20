package sns.asteroid.view.activity

import android.os.Bundle
import androidx.core.view.isVisible
import sns.asteroid.R
import sns.asteroid.databinding.ActivitySingleTimelineBinding
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.pager.TimelinePagerAdapter

class SingleTimelineActivity : BaseActivity() {
    private val binding by lazy { ActivitySingleTimelineBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val credential = intent.getSerializableExtra("credential") as Credential
        val columnInfo = intent.getSerializableExtra("column") as ColumnInfo

        TimelinePagerAdapter(this, binding.viewPager, enableAddMenu = true)
            .update(listOf(Pair(columnInfo, credential)))

        binding.viewPager.also {
            it.isUserInputEnabled = false
        }

        binding.floatingActionButton.apply {
            isVisible = !listOf(
                "favourited_by",
                "reblogged_by",
                "block",
                "mute",
            ).contains(columnInfo.subject)

            if (columnInfo.subject == "hashtag") {
                setImageResource(R.drawable.hashtag)
                setOnClickListener { openCreatePostsActivity(credential, "#${columnInfo.option_id}", "")}
            } else {
                setOnClickListener { openCreatePostsActivity(credential) }
            }
        }
    }
}