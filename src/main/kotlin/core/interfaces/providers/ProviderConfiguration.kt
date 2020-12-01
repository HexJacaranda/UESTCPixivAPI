package core.interfaces.providers

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProviderConfiguration(
        @Json(name = "version")
        val version:String,
        @Json(name = "configurations")
        val configurations: Map<String,String>)