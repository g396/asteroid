package sns.asteroid.model.emoji

import com.opencsv.bean.CsvBindByName

class UnicodeEmoji(
    @CsvBindByName(column = "Representation", required = true)
    val unicodeString: String = "",
    @CsvBindByName(column = "Name", required = true)
    val name: String = "",
)