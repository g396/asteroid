package sns.asteroid.model.util

import com.opencsv.CSVReader
import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvToBeanBuilder
import sns.asteroid.CustomApplication
import java.nio.charset.Charset

data class ISO639Lang(
    @CsvBindByName(column = "ISO639-1 code", required = true)
    val code: String = "",
    @CsvBindByName(column = "Name", required = true)
    val name: String = "",
) {
    val text get() = StringBuilder()
        .append(code.uppercase())
        .append(" ")
        .append("(")
        .append(name)
        .append(")")
        .toString()
        .trimStart()

     companion object {
         private val DEFAULT_LANG = ISO639Lang("", "Default in server")

         fun getLanguageList(): List<ISO639Lang> {
             val assetManager = CustomApplication.getApplicationContext().assets
             val file = "language.csv"

             val csvReader = let {
                 val inputStream = assetManager.open(file)
                 val reader = inputStream.bufferedReader(Charset.forName("utf-8"))
                 CSVReader(reader)
             }

             return CsvToBeanBuilder<ISO639Lang>(csvReader)
                 .withType(ISO639Lang::class.java)
                 .withIgnoreLeadingWhiteSpace(true)
                 .build()
                 .parse()
                 .sortedBy { it.code }
                 .let { listOf(DEFAULT_LANG).plus(it) }
         }
     }
}
