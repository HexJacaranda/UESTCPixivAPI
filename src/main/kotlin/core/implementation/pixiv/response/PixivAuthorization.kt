package core.implementation.pixiv.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class PixivAuthorization(@Json(name = "username")val userName:String,
                         @Json(name = "password")val password:String,
                         @Json(name = "token")val token:String,
                         @Json(name = "refresh_token")val refreshToken:String,
                         @Json(name = "is_premium")val isPremium:Boolean)