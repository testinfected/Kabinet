data class Product(
    val id: Int? = null,
    val number: Int,
    var name: String,
    var description: String? = null
) {
    override fun toString(): String {
        return "$number ($name)"
    }
}
