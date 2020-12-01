package core.implementation.pixiv

import core.implementation.pixiv.response.PixivAuthorization
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import java.io.BufferedReader
import java.io.InputStreamReader

operator fun HttpRequestBase.get(Key:String):String
        = this.getFirstHeader(Key).value

operator fun HttpRequestBase.set(Key:String, Value:String)
        = if(!this.containsHeader(Key)) this.addHeader(Key,Value) else Unit

infix fun String.to(right:String): BasicNameValuePair = BasicNameValuePair(this,right)

val HttpResponse.ContentOnce get() =
    BufferedReader(InputStreamReader(this.entity.content,PixivConstant.charset))
            .use(BufferedReader::readText)

fun<R> request(
        RequestBase: HttpRequestBase,
        Authorization: PixivAuthorization,
        Params:List<NameValuePair>,
        Header:List<Pair<String,String>>,
        Fn:(HttpResponse) ->R):R {
    RequestBase["Authorization"] = "Bearer ${Authorization.token}"
    Header.forEach { RequestBase[it.first] = it.second }
    RequestBase.uri = URIBuilder(RequestBase.uri).addParameters(Params.toMutableList()).build()
    val config = RequestConfig
            .custom()
            .setProxy(PixivConstant.proxy)
            .build()
    return HttpClients.custom().setDefaultRequestConfig(config).build().use {
        it.execute(RequestBase).use(Fn)
    }
}