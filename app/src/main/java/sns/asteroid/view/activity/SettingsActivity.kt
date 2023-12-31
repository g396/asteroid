package sns.asteroid.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sns.asteroid.BuildConfig
import sns.asteroid.R
import sns.asteroid.databinding.ActivitySettingsBinding
import sns.asteroid.databinding.RowAboutAppsBinding
import sns.asteroid.databinding.RowMenuBinding
import sns.asteroid.model.user.AccountsModel
import sns.asteroid.model.settings.SettingsManageAccountsModel

class SettingsActivity : BaseActivity(){

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTitle(R.string.title_settings)

        binding.listView.apply {
            val concatAdapter = ConcatAdapter().apply {
                addAdapter(SettingsAdapter())
                addAdapter(SettingsFooterAdapter())
            }
            adapter = concatAdapter
            layoutManager = LinearLayoutManager(this@SettingsActivity)
        }
    }

    inner class SettingsAdapter: RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(this@SettingsActivity)
            val binding = RowMenuBinding.inflate(inflater, parent, false).apply { root.tag = this }
            return ViewHolder(binding)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding.apply {
                root.setOnClickListener {
                    val activity = when(SettingsMenu.values()[position]) {
                        SettingsMenu.GENERAL -> SettingsGeneralActivity::class.java
                        SettingsMenu.OSS_LICENSE -> OSSLicenseActivity::class.java
                    }
                    Intent(this@SettingsActivity, activity).also {startActivity(it)}
                }
            }
            val row = SettingsMenu.values()[position]

            binding.rowTitle.apply {
                text = when (row) {
                    SettingsMenu.GENERAL -> context.getString(R.string.settings_general)
                    SettingsMenu.OSS_LICENSE -> context.getString(R.string.title_oss_license)
                }
            }
            binding.rowIcon.apply {
                val icon = when (row) {
                    SettingsMenu.GENERAL -> R.drawable.settings_general
                    SettingsMenu.OSS_LICENSE -> R.drawable.oss_license
                }
                setImageResource(icon)
            }
        }

        override fun getItemCount(): Int {
            return SettingsMenu.values().size
        }

        inner class ViewHolder(val binding: RowMenuBinding): RecyclerView.ViewHolder(binding.root)
    }

    inner class SettingsFooterAdapter: RecyclerView.Adapter<SettingsFooterAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(this@SettingsActivity)
            val binding = RowAboutAppsBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return 1
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding
            binding.version.apply { text = BuildConfig.VERSION_NAME }
            binding.icon.apply {
                setOnClickListener { openDeveloperAccount() }
                setImageResource(R.drawable.sync)
                lifecycleScope.launch {
                    val account = withContext(Dispatchers.IO) {
                        AccountsModel("social.vivaldi.net").getAccountByAcct("396@vivaldi.net").account
                    }
                    if (account != null) binding.developer = account
                    else setImageResource(R.drawable.question)
                }
            }
            binding.iconOfficial.apply {
                setOnClickListener { openOfficialAccount() }
                setImageResource(R.drawable.sync)
                lifecycleScope.launch {
                    val account = withContext(Dispatchers.IO) {
                        AccountsModel("social.vivaldi.net").getAccountByAcct("AsteroidApp@vivaldi.net").account
                    }
                    if (account != null) binding.official = account
                    else setImageResource(R.drawable.question)
                }
            }
        }

        private fun openDeveloperAccount() {
            lifecycleScope.launch {
                val credential = withContext(Dispatchers.IO) {
                    SettingsManageAccountsModel().getCredentials().firstOrNull()
                }

                if (credential != null) {
                    openAccount(credential, "396@vivaldi.net", null)
                } else {
                    val uri = "https://social.vivaldi.net/@396".toUri()
                    val webIntent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(webIntent)
                }
            }
        }

        private fun openOfficialAccount() {
            lifecycleScope.launch {
                val credential = withContext(Dispatchers.IO) {
                    SettingsManageAccountsModel().getCredentials().firstOrNull()
                }

                if (credential != null) {
                    openAccount(credential, "AsteroidApp@vivaldi.net", null)
                } else {
                    val uri = "https://social.vivaldi.net/@AstroidApp".toUri()
                    val webIntent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(webIntent)
                }
            }
        }
        
        inner class ViewHolder(val binding: RowAboutAppsBinding) : RecyclerView.ViewHolder(binding.root)
    }

    enum class SettingsMenu {
        GENERAL,
        OSS_LICENSE,
    }
}