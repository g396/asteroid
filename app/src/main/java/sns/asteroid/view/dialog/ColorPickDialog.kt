package sns.asteroid.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sns.asteroid.R
import sns.asteroid.databinding.RowColorBinding

/**
 * カラーピッカー
 */
class ColorPickDialog: DialogFragment() {
    private var callback: ColorSelectCallback? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity).apply {
            val recyclerView = RecyclerView(context)

            val dialogCallback: (Int) -> Unit = {
                dismiss()
                callback?.onColorSelect(it)
            }
            recyclerView.adapter = ColorSelectorAdapter(requireContext(), dialogCallback)
            recyclerView.layoutManager = GridLayoutManager(context, 2)

            setView(recyclerView)

        }.create()
    }

    /**
     * 親Activityが再生成されると、空のコンストラクタでインスタンス再生成される為listenerがnullになる
     * →従ってコールバックできなくなる
     * そのような場合はダイアログ自体を閉じてしまう事にする
     */
    override fun onStart() {
        super.onStart()
        if(callback == null) dialog?.cancel()
    }

    /**
     * ここからインスタンスを作るのだ
     * Fragment系は空のコンストラクタだけを使うのがルールなのだ
     */
    companion object {
         @JvmStatic
         fun newInstance(callback: ColorSelectCallback): ColorPickDialog {
             return ColorPickDialog().apply {
                 this.callback = callback
             }
         }
    }

    interface ColorSelectCallback {
        fun onColorSelect(colorCode: Int)
    }

    class ColorSelectorAdapter(val context: Context, val callback: (Int) -> Unit): RecyclerView.Adapter<ColorSelectorAdapter.ViewHolder>() {
        val list = listOf(
            R.color.gray0,
            R.color.gray1,
            R.color.gray2,
            R.color.gray3,
            R.color.red,
            R.color.pink,
            R.color.purple,
            R.color.deep_purple,
            R.color.indigo,
            R.color.blue,
            R.color.light_blue,
            R.color.cyan,
            R.color.teal,
            R.color.green,
            R.color.light_green,
            R.color.lime,
            R.color.yellow,
            R.color.amber,
            R.color.orange,
            R.color.deep_orange,
        )

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(context)
            val binding = RowColorBinding.inflate(inflater)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding

            val color = context.resources.getColor(list[position])

            binding.root.setOnClickListener {
                val colorId = list[position]
                callback(context.resources.getColor(colorId))
            }
            binding.color.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    colorFilter = BlendModeColorFilter(color, BlendMode.SRC_IN)
                } else {
                    setColorFilter(color, PorterDuff.Mode.SRC_IN)
                }
            }

            binding.text.apply {
                val hex = String.format("%x", color).uppercase().substring(2)
                text = String.format("#%s", hex)
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }

        class ViewHolder(val binding: RowColorBinding): RecyclerView.ViewHolder(binding.root)
    }
}