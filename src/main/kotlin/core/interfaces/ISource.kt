package core.interfaces

interface ISource {
    fun fetch(tags: List<String>, Context: FetchContext): Sequence<IImage>
}