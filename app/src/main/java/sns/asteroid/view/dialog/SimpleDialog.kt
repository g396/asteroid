package sns.asteroid.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import sns.asteroid.R

/**
 * 一文を表示させるだけの汎用ダイアログ
 */
class SimpleDialog: DialogFragment() {
    private var listener: SimpleDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity).apply {
            val title = requireArguments().getString("title", "empty")
            setTitle(title)

            setPositiveButton(getString(R.string.dialog_yes), DialogInterface.OnClickListener { _, _ ->
                listener?.onDialogAccept()
            })
            setNeutralButton(getString(R.string.dialog_cancel), DialogInterface.OnClickListener { _, _ ->
                listener?.onDialogCancel()
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
        fun newInstance(listener: SimpleDialogListener, title: String): SimpleDialog {
            val arg = Bundle().apply {
                putString("title", title)
            }
            return SimpleDialog().apply {
                this.arguments = arg
                this.listener = listener
            }
        }
    }

    interface SimpleDialogListener {
        fun onDialogAccept()
        fun onDialogCancel()
    }
}