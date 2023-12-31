package sns.asteroid.view.activity

import android.os.Bundle
import sns.asteroid.databinding.ActivityTrendsBinding
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.pager.TrendsPagerAdapter

/**
 * トレンドの取得
 *  TODO: ハッシュタグが古いエンドポイントの方に対応してない
 */
class TrendsActivity : BaseActivity() {
    val binding: ActivityTrendsBinding by lazy { ActivityTrendsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        (intent.getSerializableExtra("credential") as Credential).also { credential ->
            binding.viewPager.adapter = TrendsPagerAdapter(this, binding.tab, binding.viewPager, credential)
        }
    }
}