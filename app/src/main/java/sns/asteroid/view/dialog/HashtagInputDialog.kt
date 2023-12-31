package sns.asteroid.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import sns.asteroid.R
import sns.asteroid.databinding.DialogHashtagBinding

/**
 * ハッシュタグをこのダイアログで入力させる
 * →そのハッシュタグのカラムを追加する
 */
class HashtagInputDialog: DialogFragment(){
    private var listener: HashtagDialogListener? = null

    private var _binding: DialogHashtagBinding? = null
    val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogHashtagBinding.inflate(requireActivity().layoutInflater)

        return AlertDialog.Builder(activity).apply {
            setTitle(R.string.dialog_enter_hashtag)
            setView(binding.root)
            setPositiveButton(R.string.dialog_add, DialogInterface.OnClickListener { _, _ ->
                listener?.onInputHashtag(binding.editText.text.toString())
            })
            setNeutralButton(R.string.dialog_cancel, DialogInterface.OnClickListener { _, _ ->
                dialog?.cancel()
            })
        }.create()
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
     * FragmentでView-bindingする時は
     * メモリリーク対策を忘れずに
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * ここからインスタンスを作るのだ
     * Fragment系は空のコンストラクタだけを使うのがルールなのだ
     */
    companion object {
        @JvmStatic
        fun newInstance(listener: HashtagDialogListener): HashtagInputDialog {
            return HashtagInputDialog().apply {
                this.listener = listener
            }
        }
    }

    interface HashtagDialogListener {
        fun onInputHashtag(hashtag: String)
    }
}