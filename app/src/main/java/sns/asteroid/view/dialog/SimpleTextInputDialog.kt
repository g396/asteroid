package sns.asteroid.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import sns.asteroid.R
import sns.asteroid.databinding.DialogSimpleTextInputBinding

/**
 * テキストを入力するダイアログ
 */
class SimpleTextInputDialog: DialogFragment() {
    private var listener: SimpleTextInputDialogListener? = null

    private var _binding: DialogSimpleTextInputBinding? = null
    val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogSimpleTextInputBinding.inflate(layoutInflater)

        return AlertDialog.Builder(activity).apply {
            val title = requireArguments().getString("title", "empty")
            setTitle(title)

            val hint = requireArguments().getString("hint", "hint")
            binding.editText.hint = hint

            val text = requireArguments().getString("text")
            binding.editText.setText(text)

            setPositiveButton(getString(R.string.dialog_add), DialogInterface.OnClickListener { _, _ ->
                listener?.onInputText(binding.editText.text.toString())
            })
            setNeutralButton(getString(R.string.dialog_cancel), DialogInterface.OnClickListener { _, _ ->
                listener?.onDialogCancel()
            })
            setView(binding.root)
        }.create()
    }

    /**
     * FragmentでView-bindingする時は
     * メモリリーク対策を忘れずに
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * 親Activityが再生成されると、空のコンストラクタでインスタンス再生成される為listenerがnullになる
     * →従ってコールバックできなくなる
     * そのような場合はダイアログ自体を閉じてしまう事にする
     */
    override fun onStart() {
        super.onStart()
        if(listener == null) dialog?.cancel()
    }

    /**
     * ダイアログの領域外を押す等によってダイアログが閉じられた際は
     * setNeuralButton()で渡した処理が呼ばれる訳ではないので
     * 別でコールバックする
     */
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener?.onDialogCancel()
    }

    /**
     * ここからインスタンスを作るのだ
     * Fragment系は空のコンストラクタだけを使うのがルールなのだ
     */
    companion object {
        @JvmStatic
        fun newInstance(listener: SimpleTextInputDialogListener, title: String, hint: String, text: String? = null): SimpleTextInputDialog {
            val arg = Bundle().apply {
                putString("title", title)
                putString("hint", hint)
                putString("text", text)
            }
            return SimpleTextInputDialog().apply {
                this.arguments = arg
                this.listener = listener
            }
        }
    }

    interface SimpleTextInputDialogListener {
        fun onInputText(text: String)
        fun onDialogCancel()
    }
}