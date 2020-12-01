package core.implementation.pixiv

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import core.implementation.pixiv.response.PixivAuthorization
import core.interfaces.IConfiguration
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import java.io.File

@JsonClass(generateAdapter = true)
class PixivConfig(@Json(name = "proxy_ip") val proxyIP: String,
                  @Json(name = "proxy_port") val proxyPort: Int,
                  @Json(name = "accounts") val accounts: List<PixivAuthorization>)
    : IConfiguration, KoinComponent {
    override fun save() {
        val json = PixivConstant
                .moshi
                .adapter(PixivConfig::class.java)
                .indent("  ")
                .toJson(this)
        File(getKoin().get<String>(named("pixiv"))).writeText(json)
    }
}