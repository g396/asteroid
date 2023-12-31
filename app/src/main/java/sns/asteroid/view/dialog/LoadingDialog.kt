package sns.asteroid.view.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import sns.asteroid.databinding.DialogLoadingBinding

class LoadingDialog : DialogFragment() {
    private var _binding: DialogLoadingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogLoadingBinding.inflate(layoutInflater)
        isCancelable = false // バックキー無効

        return  Dialog(requireContext()).apply {
            setContentView(binding.root)
            setCanceledOnTouchOutside(false) // 外周部のタッチ無効

            window?.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}