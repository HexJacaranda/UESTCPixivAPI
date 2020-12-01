package uestc.services.implementation

import core.interfaces.FetchContext
import core.interfaces.ISourceProvider
import org.koin.core.KoinComponent
import org.koin.core.inject
import uestc.services.response.ImageResponse
import javax.inject.Singleton

@Singleton
class FetchService : KoinComponent {
    private val provider: ISourceProvider by inject()
    fun fetch(tags: List<String>, requires: Int, position: Int): List<ImageResponse> {
        val provider = provider.sources.first()
        val result = provider.fetch(tags, FetchContext(requires, position, 0))
        return result.map { ImageResponse(it) }.toList()
    }
}