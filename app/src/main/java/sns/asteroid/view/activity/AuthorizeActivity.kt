package sns.asteroid.view.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import sns.asteroid.R
import sns.asteroid.databinding.ActivityAuthorizeBinding
import sns.asteroid.viewmodel.AuthorizeViewModel

class AuthorizeActivity : AppCompatActivity() {
    private val isFirst by lazy { intent.getBooleanExtra("isFirst", false) }
    private lateinit var binding: ActivityAuthorizeBinding
    private val viewModel: AuthorizeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthorizeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTitle(R.string.title_login_mastodon)

        binding.apply {
            lifecycleOwner = this@AuthorizeActivity
            viewModel = this@AuthorizeActivity.viewModel
            buttonLogin.setOnClickListener {
                if (binding.checkBox.isChecked) verifyAccessToken()
                else createApps()
            }
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                materialTextfield2.visibility =
                    if (isChecked) View.VISIBLE
                    else View.GONE
                checkBox2.isEnabled = !isChecked
            }
            checkBox2.setOnCheckedChangeListener { _, isChecked ->
                materialTextfield3.visibility =
                    if (isChecked) View.VISIBLE
                    else View.GONE
                checkBox.isEnabled = !isChecked
            }
        }

        viewModel.also {
            it.toastMessage.observe(this, Observer { msg ->
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            })
            it.authorizeUri.observe(this, Observer { uri ->
                openBrowser(uri)
            })
        }

        showKeyboard()
    }

    /**
     * When returned from browser
     * post authentication-code to server
     * to get access token
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val code = intent?.data?.getQueryParameter("code") ?: return

        lifecycleScope.launch {
            binding.buttonLogin.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            val result = viewModel.obtainToken(code)
            binding.progressBar.visibility = View.INVISIBLE
            binding.buttonLogin.isEnabled = true

            if (!result) return@launch

            if(isFirst){
                val nextIntent = Intent(this@AuthorizeActivity, TimelineActivity::class.java)
                startActivity(nextIntent)
            }
            finish()
        }
    }

    private fun createApps() {
        lifecycleScope.launch {
            binding.buttonLogin.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            viewModel.createApps(binding.checkBox2.isChecked)
            binding.progressBar.visibility = View.INVISIBLE
            binding.buttonLogin.isEnabled = true
        }
    }

    private fun openBrowser(uri: Uri) {
        val webIntent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(webIntent)
    }

    private fun showKeyboard() {
        binding.editTextServer.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.editTextServer, 0)

    }

    /**
     * OAuth認証せずにアクセストークンを直接入力する場合
     */
    private fun verifyAccessToken() {
        lifecycleScope.launch {
            binding.buttonLogin.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            val result = viewModel.verifyAccessToken()
            binding.progressBar.visibility = View.INVISIBLE
            binding.buttonLogin.isEnabled = true

            if(!result) return@launch

            if(isFirst){
                val nextIntent = Intent(this@AuthorizeActivity, TimelineActivity::class.java)
                startActivity(nextIntent)
            }
            finish()
        }
    }

}