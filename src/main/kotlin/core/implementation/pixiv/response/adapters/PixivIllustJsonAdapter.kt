package core.implementation.pixiv.response.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson

class PixivIllustJsonAdapter {

    class MetaSinglePage(
            @Json(name = "original_image_url")
            val imageUrl:String)

    private val options = JsonReader.Options.of("original")

    @ToJson
    fun toJson(Meta: MetaSinglePage) = ""

    @FromJson
    fun fromJson(jsonReader: JsonReader): MetaSinglePage {
        var meta = MetaSinglePage("")
        jsonReader.beginObject()
        if (jsonReader.peek() == JsonReader.Token.NAME) {
            jsonReader.skipName()
            if (jsonReader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                jsonReader.beginObject()
                loop@ while (jsonReader.hasNext()) {
                    when (jsonReader.selectName(options)) {
                        0 -> {
                            meta = MetaSinglePage(jsonReader.nextString())
                            break@loop
                        }
                        else -> {
                            jsonReader.skipName()
                            jsonReader.skipValue()
                        }
                    }
                }
                jsonReader.endObject()
            } else {
                meta = MetaSinglePage(jsonReader.nextString())
            }
        }
        jsonReader.endObject()
        return meta
    }
}