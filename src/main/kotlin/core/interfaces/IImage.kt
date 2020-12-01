package core.interfaces

interface IImage {
    val previewUrl: String
    val urls: List<String>
    val title: String
    val width: Int
    val height: Int
    val tags: List<String>
    val context: MutableList<Pair<String, String>>
}