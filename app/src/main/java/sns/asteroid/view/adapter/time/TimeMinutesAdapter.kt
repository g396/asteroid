package sns.asteroid.view.adapter.time

import android.content.Context

class TimeMinutesAdapter (
    context: Context,
): TimeSpinnerAdapter(context) {
    override val list = listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)
}