package sns.asteroid.model.util

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

class TimeFormatter {
    companion object {
        private fun format(input: String, relative: Boolean = false): String {
            val createdAt = try {
                LocalDateTime.parse(input.substring(0, input.length-2))
            } catch (e: DateTimeParseException) {
                return "?"
            }
            val now = LocalDateTime.now(ZoneId.of("UTC"))

            ChronoUnit.MINUTES.between(createdAt, now).let{
                if (it <= 0) return "now"
                if (it <= 1) return "$it min"
                if (it <= 59) return "$it mins"
            }
            ChronoUnit.HOURS.between(createdAt, now).let{
                if(it <= 1) return "$it hour"
                if(it <= 23) return "$it hours"
            }
            ChronoUnit.DAYS.between(createdAt, now).let{
                if(it <= 1) return "$it day"
                if(it <= 30) return "$it days"
            }
            if(relative) ChronoUnit.MONTHS.between(createdAt, now).let{
                if(it <= 1) return "$it month"
                if(it <= 11) return "$it months"
            }
            if(relative) ChronoUnit.YEARS.between(createdAt, now).let{
                if(it <= 1) return "$it year"
                if(it <= 30) return "$it years"
            }

            val converted = createdAt.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime()
            val pattern =
                if(converted.year == now.year) "MM/dd"
                else "yyyy/MM/dd"
            val formatter = DateTimeFormatter.ofPattern(pattern)
            return converted.format(formatter)
        }

        fun formatAuto(input: String): String {
            return format(input, relative = false)
        }

        fun formatRelative(input: String): String {
            return format(input, relative = true)
        }
        fun formatAbsolute(input: String, useSeconds: Boolean = false): String {
            val createdAt = try {
                LocalDateTime.parse(input.substring(0, input.length-2))
            } catch (e: DateTimeParseException) {
                return "?"
            }
            val converted = createdAt.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime()

            val now = LocalDateTime.now(ZoneId.systemDefault())
            val pattern = when {
                (now.year != converted.year) -> if(useSeconds) "yyyy/MM/dd HH:mm:ss" else "yyyy/MM/dd HH:mm"
                (now.month != converted.month) -> if(useSeconds)  "MM/dd HH:mm:ss" else "MM/dd HH:mm"
                (now.dayOfMonth != converted.dayOfMonth) -> if(useSeconds)  "MM/dd HH:mm:ss" else "MM/dd HH:mm"
                else -> if(useSeconds)  "HH:mm:ss" else "HH:mm"
            }
            val formatter = DateTimeFormatter.ofPattern(pattern)
            return converted.format(formatter)
        }

        fun formatExpire(input: String?): String {
            if(input == null) return "⌛ ∞ days"

            val expireAt = LocalDateTime.parse(input.substring(0, input.length-2))
            val now = LocalDateTime.now(ZoneId.of("UTC"))

            ChronoUnit.MINUTES.between(now, expireAt).let{
                if (it <= 0) return "⌛ finished"
                if (it <= 1) return "⌛ $it min"
                if (it <= 59) return "⌛ $it mins"
            }
            ChronoUnit.HOURS.between(now, expireAt).let{
                if(it <= 1) return "⌛ $it hour"
                if(it <= 23) return "⌛ $it hours"
            }
            return ChronoUnit.DAYS.between(now, expireAt).let{
                if(it <= 1) "⌛ $it day"
                else "⌛ $it days"
            }
        }
    }
}