package sns.asteroid.model.emoji

import com.opencsv.CSVReader
import com.opencsv.bean.CsvToBeanBuilder
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.Instance
import sns.asteroid.api.entities.CustomEmoji
import java.nio.charset.Charset

class EmojiModel(val server: String) {
    companion object {
        private val customEmojisCaches = mutableMapOf<String, List<Result.EmojisList>>()
        private var unicodeEmojisCaches: List<UnicodeEmoji>? = null

        fun getUnicodeEmojis(query: String): List<UnicodeEmoji> {
            val list = getUnicodeEmojis()
            return if (query.isBlank()) list
            else list.filter { it.name.lowercase().contains(query.lowercase()) }
        }

        private fun getUnicodeEmojis(): List<UnicodeEmoji> {
            unicodeEmojisCaches?.let { return it }

            val assetManager = CustomApplication.getApplicationContext().assets

            val files = listOf(
                "emojis_1_people.csv",
                "emojis_2_nature.csv",
                "emojis_3_foods.csv",
                "emojis_4_activities.csv",
                "emojis_5_travel.csv",
                "emojis_6_objects.csv",
                "emojis_7_flags.csv",
            )

            val unicodeEmojis = mutableListOf<UnicodeEmoji>()

            files.forEach { file ->
                val csvReader = let {
                    val inputStream = assetManager.open(file)
                    val reader = inputStream.bufferedReader(Charset.forName("utf-8"))
                    CSVReader(reader)
                }

                val list = CsvToBeanBuilder<UnicodeEmoji>(csvReader)
                    .withType(UnicodeEmoji::class.java)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse()

                unicodeEmojis.addAll(list)
            }

            return unicodeEmojis.also { unicodeEmojisCaches = it }
        }
    }

    fun getCustomEmojis(query: String): Result {
        val cache = getCustomEmojis().emojis

        if (query.isBlank())
            return Result(isSuccess = true, emojis = cache)

        val search = mutableListOf<Result.EmojisList>().also { list ->
            cache?.forEach {
                val server = this.server
                val category = it.category
                val emojis = it.emojisList.filter { customEmoji -> customEmoji.shortcode.lowercase().contains(query.lowercase())}
                list.add(Result.EmojisList(server, category, emojis))
            }
        }

        return Result(isSuccess = true, emojis = search)
    }

    private fun getCustomEmojis(): Result {
        val cache = customEmojisCaches[server]

        if (cache != null)
            return Result(isSuccess = true, emojis = cache)

        val response =  Instance(server).getCustomEmojis()
            ?: return Result(
                isSuccess = false,
                emojis = null,
                toastMessage = getString(R.string.failed)
            )

        if(!response.isSuccessful)
            return Result(
                isSuccess = false,
                emojis = null,
                toastMessage = response.body!!.string()
            )
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val emojis = json.decodeFromString(ListSerializer(CustomEmoji.serializer()), response.body!!.string())

        val emojisList = mutableListOf<Result.EmojisList>().apply {
            val categories =
                emojis.associate { Pair(it.shortcode, it.category) }.values

            if (categories.toSet().size > 1) {
                add(Result.EmojisList(server, "Custom Emojis", emojis))
            }
            categories.sorted().forEach { category ->
                val list = emojis.filter { it.category == category }
                add(Result.EmojisList(server, category.ifEmpty { "Not categorized" }, list))
            }
        }

        return Result(isSuccess = true, emojis = emojisList)
            .also {
                customEmojisCaches[server] = emojisList
                response.close()
            }
    }

    fun getCustomEmojiCategories(): EmojiCategoryList?{
        val emojis = getCustomEmojis().emojis ?: return null
        val list = emojis.associateBy { it.category }.keys.toMutableList()
            .apply {
                val index = if (this.size > 0) 1 else 0
                add(index, "Unicode Emojis")
            }

        return EmojiCategoryList(
            server,
            list,
        )
    }

    fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }

    data class Result(
        val isSuccess: Boolean,
        val emojis: List<EmojisList>?,
        val toastMessage: String? = null,
    ) {
        data class EmojisList(
            val domain: String,
            val category: String,
            val emojisList: List<CustomEmoji>,
        )
    }

    data class EmojiCategoryList(
        val domain: String,
        val categorySet: List<String>,
    )
}