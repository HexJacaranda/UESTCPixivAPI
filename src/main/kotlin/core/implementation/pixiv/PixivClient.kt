package core.implementation.pixiv

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import core.implementation.pixiv.response.PixivAuthorization
import core.implementation.pixiv.response.PixivSearchResponseJson
import core.interfaces.IImage
import mu.KotlinLogging
import org.apache.commons.codec.digest.DigestUtils
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClients
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import kotlin.math.log

class PixivClient(var authorization: PixivAuthorization) {

    companion object {
        private val logger = KotlinLogging.logger("Pixiv Client")
        private fun getLoginRequestContent(Info: PixivAuthorization): HttpEntity =
                UrlEncodedFormEntity(
                        if (Info.refreshToken == "")
                            listOf(
                                    "get_secure_url" to "1",
                                    "client_id" to PixivConstant.clientID,
                                    "client_secret" to PixivConstant.secret,
                                    "username" to Info.userName,
                                    "password" to Info.password,
                                    "grant_type" to "password")
                        else
                            listOf(
                                    "get_secure_url" to "1",
                                    "client_id" to PixivConstant.clientID,
                                    "client_secret" to PixivConstant.secret,
                                    "refresh_token" to Info.refreshToken,
                                    "grant_type" to "refresh_token")
                        , PixivConstant.charset)

        private fun getUTCNow(): String =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US).format(Date())

        private fun setRequestHeader(Post: HttpPost) {
            Post["App-OS"] = PixivConstant.appOS
            Post["App-OS-Version"] = PixivConstant.appOSVersion
            Post["App-Version"] = PixivConstant.appVersion
            Post["User-Agent"] = PixivConstant.userAgent
            Post["Accept-Language"] = Locale.getDefault().toString()
            Post["Content-Type"] = "application/x-www-form-urlencoded;charset=UTF-8"
            val time = getUTCNow()
            Post["X-Client-Time"] = time
            val hash = DigestUtils.md5Hex(
                    (time + PixivConstant.hashSecret).toByteArray(PixivConstant.charset))
            Post["X-Client-Hash"] = hash
        }

        private fun getLoginRequestPost(Info: PixivAuthorization): HttpPost {
            val post = HttpPost(PixivConstant.loginUrl)
            post.entity = getLoginRequestContent(Info)
            setRequestHeader(post)
            return post
        }

        private fun getAccessToken(Content: String): String {
            val root = Parser.default().parse(Content.reader()) as JsonObject
            return root.obj("response")?.string("access_token") ?: ""
        }

        private fun getRefreshToken(Content: String): String {
            val root = Parser.default().parse(Content.reader()) as JsonObject
            return root.obj("response")?.string("refresh_token") ?: ""
        }

        fun tryGetAuthorization(Info: PixivAuthorization): PixivAuthorization {
            logger.info { "Try getting authorization for client." }
            logger.info { "Building http proxy configuration and TLS." }
            val sslFactory = SSLConnectionSocketFactory(
                    SSLContext.getDefault(),
                    arrayOf("TLSv1.2","TLSv1.3"),
                    null,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier()
            )

            val config = RequestConfig
                    .custom()
                    .setProxy(PixivConstant.proxy)
                    .build()

            logger.info { "Requesting for authorization." }
            return try {
                HttpClients.custom()
                        .setSSLSocketFactory(sslFactory)
                        .setDefaultRequestConfig(config)
                        .build()
                        .use {
                            it.execute(getLoginRequestPost(Info)).use { response ->
                                logger.info { "Got authorization response with status code ${response.statusLine.statusCode}." }
                                when (response.statusLine.statusCode) {
                                    in arrayOf(
                                            HttpStatus.SC_OK,
                                            HttpStatus.SC_MOVED_TEMPORARILY,
                                            HttpStatus.SC_MOVED_PERMANENTLY) -> {
                                        val json = response.ContentOnce
                                        logger.info { "Authorization request content:\n$json ." }
                                        PixivAuthorization(
                                                Info.userName, Info.password,
                                                getAccessToken(json),
                                                getRefreshToken(json),
                                                Info.isPremium
                                        )
                                    }
                                    else -> throw Exception("Authorization failed for code:${response.statusLine.statusCode}")
                                }
                            }
                        }
            } catch (e :Exception) {
                logger.error(e) { "Something went wrong when requesting." }
                Info
            }
        }
    }

    enum class SearchType(val Value: String) {
        PartialMatchForTags("partial_match_for_tags"),
        ExactMatchForTags("exact_match_for_tags"),
        Title("title_and_caption")
    }

    enum class Duration(val Value: String) {
        WithinLastDay("within_last_day"),
        WithinLastWeek("within_last_week"),
        WithinLastMonth("within_last_month"),
        OffLimit("")
    }

    enum class SortType(val Value: String) {
        Ascending("date_asc"),
        Descending("date_desc"),
        PopularDescending("popular_desc")
    }

    fun getAuthorization() {
        authorization = tryGetAuthorization(authorization)
    }

    fun search(
            Key: String,
            Type: SearchType,
            DurationType: Duration = Duration.OffLimit,
            Sort: SortType = SortType.PopularDescending,
            Offset: Int = 0
    ): List<IImage> {
        logger.info { "Preparing search parameter(s)." }
        val url = "${PixivConstant.host}/v1/search/illust"
        val params = mutableListOf(
                "word" to Key,
                "search_target" to Type.Value,
                "sort" to if (authorization.isPremium && SortType.PopularDescending.Value == Sort.Value)
                    Sort.Value else SortType.Descending.Value,
                "filter" to "for_ios")
        if (DurationType != Duration.OffLimit)
            params.add("duration" to DurationType.Value)
        if (Offset > 0)
            params.add("offset" to Offset.toString())
        else
            params.add("offset" to 0.toString())

        logger.info { "Getting response json string." }
        val jsonString = request(
                HttpGet(url),
                authorization,
                params,
                listOf(),
                HttpResponse::ContentOnce)

        logger.info { "Deserialize from json text to response object." }
        val response = PixivConstant
                .moshi
                .adapter(PixivSearchResponseJson::class.java)
                .fromJson(jsonString)
                ?: return listOf()

        logger.info { "Handling response image(s)." }
        return response.illusts
                .asSequence()
                .map { it as IImage }
                .apply {
                    this.forEach {
                        it.context.add(Pair("Authorization", "Bearer ${authorization.token}"))
                        it.context.add(Pair("Referer", PixivConstant.referer))
                    }
                }.toList()
    }
}