class Product(
    val id: Int,
    val number: String,
    var name: String,
    var description: String
) {
    override fun toString(): String {
        return "$number ($name)"
    }
}
