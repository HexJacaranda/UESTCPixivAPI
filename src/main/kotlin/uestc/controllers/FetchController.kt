package uestc.controllers

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import uestc.services.implementation.FetchService
import uestc.services.response.ImageResponse
import java.lang.Exception
import javax.inject.Inject

@Controller("/fetch")
class FetchController(
        @Inject private val fetchService: FetchService) {
    data class FetchPostBody(@JsonProperty("tags") val tags: List<String>,
                             @JsonProperty("requires") val requires: Int,
                             @JsonProperty("position") val position: Int)

    @Post("/")
    @Consumes(MediaType.APPLICATION_JSON,MediaType.TEXT_JSON)
    @Produces(MediaType.TEXT_JSON)
    fun fetch(@Body body: FetchPostBody): HttpResponse<List<ImageResponse>> {
        return try {
            HttpResponse.ok(fetchService.fetch(body.tags, body.requires, body.position))
        } catch (e: Exception) {
            HttpResponse.serverError()
        }
    }
}