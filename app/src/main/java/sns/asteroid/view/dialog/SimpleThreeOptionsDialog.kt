package sns.asteroid.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import sns.asteroid.R

class SimpleThreeOptionsDialog: DialogFragment() {
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
            setNegativeButton(getString(R.string.dialog_no), DialogInterface.OnClickListener { _, _ ->
                listener?.onDialogDecline()
            })
        }.create()
    }

    override fun onStart() {
        super.onStart()
        if(listener == null) dialog?.cancel()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener?.onDialogCancel()
    }

    companion object {
        @JvmStatic
        fun newInstance(listener: SimpleDialogListener, title: String): SimpleThreeOptionsDialog {
            val arg = Bundle().apply {
                putString("title", title)
            }
            return SimpleThreeOptionsDialog().apply {
                this.arguments = arg
                this.listener = listener
            }
        }
    }

    interface SimpleDialogListener {
        fun onDialogAccept()
        fun onDialogCancel()
        fun onDialogDecline()
    }
}