package sns.asteroid.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.databinding.ActivityManageProfileBinding
import sns.asteroid.databinding.GeneralLoadingBinding
import sns.asteroid.databinding.GeneralTimeoutBinding
import sns.asteroid.db.entities.Credential
import sns.asteroid.view.adapter.profile.ProfileItemAdapter
import sns.asteroid.viewmodel.EditProfileViewModel

class EditProfileActivity : AppCompatActivity() {
    private val viewModel: EditProfileViewModel by viewModels {
        val credential = intent.getSerializableExtra("credential") as Credential
        EditProfileViewModel.Factory(credential)
    }
    private lateinit var binding: ActivityManageProfileBinding

    companion object {
        const val INTENT_AVATAR = 2200
        const val INTENT_HEADER = 2300
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManageProfileBinding.inflate(layoutInflater).also {
            it.lifecycleOwner = this@EditProfileActivity
            it.viewModel = viewModel
            it.appBarLayout.acct = viewModel.credential.acct
            it.appBarLayout.title = getString(R.string.column_profile)
            it.appBarLayout.color = viewModel.credential.accentColor
            it.appBarLayout.avatarUrl = viewModel.credential.avatarStatic
        }

        binding.recyclerView.also {
            it.adapter = ProfileItemAdapter(this)
            it.layoutManager = GridLayoutManager(this,1)
        }
        binding.save.also {
            it.setOnClickListener { save() }
        }
        binding.imageView.also {
            it.setOnClickListener { if(isPermissionGranted()) chooseImage(INTENT_AVATAR) }
        }
        binding.header.also {
            it.setOnClickListener { if(isPermissionGranted()) chooseImage(INTENT_HEADER) }
        }

        viewModel.toastMessage.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })
        viewModel.fields.observe(this, Observer {
            val adapter = binding.recyclerView.adapter as ProfileItemAdapter
            adapter.submitList(it)
        })

        lifecycleScope.launch {
            setLoadingView()

            if (viewModel.getCurrent()) setContentView(binding.root)
            else setFailedView()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val uri = data?.data?: return

        lifecycleScope.launch {
            when(requestCode) {
                INTENT_AVATAR -> viewModel.importAvatar(uri)
                INTENT_HEADER -> viewModel.importHeader(uri)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@EditProfileActivity, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                return
            }
        }

        Toast.makeText(this@EditProfileActivity, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
    }

    private fun setLoadingView() {
        val binding = GeneralLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setFailedView() {
        val binding = GeneralTimeoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun save() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            val result = viewModel.update()
            binding.progressBar.visibility = View.GONE
            if (result) {
                setResult(RESULT_OK, Intent())
                finish()
            }
        }
    }

    private fun chooseImage(requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, requestCode)
    }

    private fun isPermissionGranted(): Boolean {
        val permissions =
            if (Build.VERSION.SDK_INT >= 33) mutableListOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO,
            ) else mutableListOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )

        val requests = mutableListOf<String>().apply {
            permissions.forEach {
                val permission = ContextCompat.checkSelfPermission(this@EditProfileActivity, it)
                val isNotGranted = permission != PackageManager.PERMISSION_GRANTED
                if (isNotGranted) add(it)
            }
        }.toTypedArray()

        return if (requests.isNotEmpty()) {
            ActivityCompat.requestPermissions(this@EditProfileActivity, requests, 0)
            false
        } else
            true
    }
}