package core.implementation.pixiv

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import core.implementation.pixiv.response.adapters.PixivIllustJsonAdapter
import org.apache.http.HttpHost
import java.nio.charset.StandardCharsets

object PixivConstant {
    const val host = "https://app-api.pixiv.net"
    const val loginUrl = "https://oauth.secure.pixiv.net/auth/token"
    val charset = StandardCharsets.UTF_8
    const val clientID = "MOBrBDS8blbauoSck0ZfDbtuzpyT"
    const val secret = "lsACyCD94FhDUtGTXi3QzcFE2uU1hqtDaKeqrdwj"
    const val hashSecret = "28c1fdd170a5204386cb1313c7077b34f83e4aaf4aa829ce78c231e05b0bae2c"
    const val appOS = "android"
    const val appOSVersion = "6.0"
    const val appVersion = "5.0.188"
    const val userAgent = "PixivAndroidApp/5.0.188 (Android 6.0)"
    const val referer = "https://app-api.pixiv.net/"
    val moshi = Moshi.Builder()
            .add(PixivIllustJsonAdapter())
            .add(KotlinJsonAdapterFactory()).build()!!
    var proxy = HttpHost("localhost",1080)
    const val searchPageCount = 30
}