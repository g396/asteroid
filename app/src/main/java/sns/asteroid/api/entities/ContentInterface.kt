package sns.asteroid.api.entities

/**
 * DiffUtilで使用する共通の要素を定義
 */
interface ContentInterface {
    val id: String
    override fun equals(other: Any?): Boolean
}