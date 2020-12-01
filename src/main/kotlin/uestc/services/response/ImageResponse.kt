package uestc.services.response

import com.fasterxml.jackson.annotation.JsonProperty
import core.interfaces.IImage



class ImageResponse(value: IImage) {
    class ContextPair(@JsonProperty("key") val Key: String,
                      @JsonProperty("value") val Val: String)

    @JsonProperty("urls")
    val urls: List<String> = value.urls

    @JsonProperty("title")
    val title: String = value.title

    @JsonProperty("width")
    val width: Int = value.width

    @JsonProperty("height")
    val height: Int = value.height

    @JsonProperty("tags")
    val tags: List<String> = value.tags

    @JsonProperty("context")
    val context: List<ContextPair> = value.context.map { ContextPair(it.first, it.second) }

    @JsonProperty("preview_url")
    val previewUrl: String = value.previewUrl
}