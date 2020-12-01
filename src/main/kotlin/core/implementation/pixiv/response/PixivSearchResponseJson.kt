package core.implementation.pixiv.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class PixivSearchResponseJson(
        @Json(name = "illusts")
        val illusts: List<PixivIllustJson>,
        @Json(name = "next_url")
        val nextUrl: String
)