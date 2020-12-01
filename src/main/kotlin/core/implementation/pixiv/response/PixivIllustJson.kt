package core.implementation.pixiv.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import core.implementation.pixiv.response.adapters.PixivIllustJsonAdapter
import core.interfaces.IImage

@JsonClass(generateAdapter = true)
data class Tag(@Json(name = "name") val name: String)

@JsonClass(generateAdapter = true)
class PixivIllustJson(
        @Json(name = "title")
        override val title: String,
        @Json(name = "width")
        override val width: Int,
        @Json(name = "height")
        override val height: Int,
        @Json(name = "meta_single_page")
        val meta: PixivIllustJsonAdapter.MetaSinglePage,
        @Json(name = "meta_pages")
        val metaPages: List<PixivIllustJsonAdapter.MetaSinglePage>,
        @Json(name = "tags")
        val underlyingTags: List<Tag>,
        @Json(name = "image_urls")
        val imageUrls: Map<String, String>
) : IImage {
    @Transient
    override val urls: List<String> =
            if (meta.imageUrl == "")
                metaPages.map { it.imageUrl }
            else listOf(meta.imageUrl)

    @Transient
    override val tags: List<String> =
            underlyingTags.map { it.name }

    @Transient
    override val context: MutableList<Pair<String, String>> =
            mutableListOf()

    @Transient
    override val previewUrl: String = imageUrls.values.firstOrNull() ?: ""
}