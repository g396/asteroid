package sns.asteroid.model.util

class HtmlParser {
    companion object {
        fun toHtml(string: String): String {
            val str = replaceToLink(string)
            val regex = Regex(".*?(\\n|\$)")
            return str.replace(regex) { "<p>${it.value}</p>" }
        }

        private fun replaceToLink(string: String): String {
            val regex = Regex("http(s*)\\:\\/\\/.*?(\\s|\$)")
            return string.replace(regex) { "<a href = \"${it.value}\">${it.value}</a>" }
        }
    }
}