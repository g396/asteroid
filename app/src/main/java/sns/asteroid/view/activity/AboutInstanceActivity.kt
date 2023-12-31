package sns.asteroid.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.databinding.ActivityAboutInstanceBinding
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.util.TextLinkMovementMethod
import sns.asteroid.viewmodel.AboutInstanceViewModel

class AboutInstanceActivity : BaseActivity(), TextLinkMovementMethod.LinkCallback {
    private lateinit var binding: ActivityAboutInstanceBinding

    private val viewModel: AboutInstanceViewModel by viewModels {
        val credential = intent.getSerializableExtra("credential") as Credential
        val target = intent.getStringExtra("target") as String
        AboutInstanceViewModel.Factory(credential, target)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.general_loading)
        setTitle(R.string.title_about_instance)

        binding = ActivityAboutInstanceBinding.inflate(layoutInflater)

        val credential = let {
            intent.getSerializableExtra("credential") ?: return
        } as Credential

        val target = let {
            intent.getStringExtra("target") ?: return
        }

        viewModel.instance.observe(this, Observer {
            binding.account = it.contact.account

            binding.title.apply {
                text = it.title
            }
            binding.background.apply {
                Glide.with(context)
                    .load(it.thumbnail.url)
                    .centerInside()
                    .into(this)
            }
            binding.description.apply {
                text = it.description
            }
            binding.activeUser.apply {
                text = it.usage.users.active_month.toString()
            }
            binding.rulesDesc.apply {
                text = it.rulesToString()
            }

            setContentView(binding.root)
        })

        // V1はJSON構成が異なる
        viewModel.instanceV1.observe(this, Observer {
            binding.account = it.contact_account
            binding.title.apply {
                text = it.title
            }
            binding.background.apply {
                Glide.with(this@AboutInstanceActivity)
                    .load(it.thumbnail)
                    .centerInside()
                    .into(this)
            }
            binding.description.apply {
                text = it.short_description
            }
            binding.labelActiveUser.apply {
                visibility = View.GONE
            }
            binding.activeUser.apply {
                visibility = View.GONE
            }
            binding.rulesDesc.apply {
                visibility = View.GONE
            }
            binding.labelRules.apply {
                visibility = View.GONE
            }

            setContentView(binding.root)
        })

        viewModel.errorStatus.observe(this, Observer {
            val layout = when(it) {
                AboutInstanceViewModel.FailStatus.NOT_FOUND -> R.layout.activity_about_instance_404
                AboutInstanceViewModel.FailStatus.FAILED_TO_PARSE_JSON -> R.layout.general_failed_to_parse_json
                AboutInstanceViewModel.FailStatus.INTERNAL_SERVER_ERROR -> R.layout.general_internal_server_error
                else -> R.layout.general_timeout
            }
            setContentView(layout)
        })

        binding.admin.icon.setOnClickListener {
            val domain = viewModel.instance.value?.domain
                ?: viewModel.instanceV1.value?.uri
                ?: return@setOnClickListener
            val acct = "${binding.account!!.acct}@$domain"
            openAccount(viewModel.credential, acct, null)
        }
        binding.admin.note.movementMethod = TextLinkMovementMethod(this)
        
        lifecycleScope.launch {
            viewModel.getAboutInstance()
        }
    }

    override fun onHashtagClick(hashtag: String) {
        val credential = viewModel.credential
        val column = let {
            val subject = "hashtag"
            ColumnInfo(credential.acct, subject, hashtag, "#${hashtag}", -1)
        }
        val intent = Intent(this, SingleTimelineActivity::class.java).apply {
            putExtra("column", column)
            putExtra("credential", credential)
        }
        startActivity(intent)
    }

    override fun onAccountURLClick(url: String) {
        openAccount(viewModel.credential, null, null, url)
    }

    override fun onWebFingerClick(acct: String) {
        openAccount(viewModel.credential, acct)
    }
}