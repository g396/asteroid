package sns.asteroid.model.settings

import sns.asteroid.CustomApplication
import sns.asteroid.R

class ColorPreset {
    companion object {
        private val colors = listOf(
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

        fun getAll(): Set<Int> {
            val resources = CustomApplication.getApplicationContext().resources
            return colors.associateBy { resources.getColor(it) }.keys
        }
    }
}