package core.implementation.pixiv

import core.interfaces.FetchContext
import core.interfaces.IImage
import core.interfaces.ISource
import core.interfaces.providers.SourceProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.apache.http.HttpHost
import org.koin.core.KoinComponent

@SourceProvider(configType = PixivConfig::class, name = "pixiv")
class PixivSource(config: PixivConfig) : ISource, KoinComponent {
    private val logger = KotlinLogging.logger("Pixiv Source Object $this")
    private val clients = mutableListOf<PixivClient>()

    init {
        logger.info { "Setting proxy." }
        PixivConstant.proxy = HttpHost(config.proxyIP, config.proxyPort)
        logger.info { "Requesting for authorization(s)." }
        requireAuthorizations(config)
        logger.info { "Writing back to config file." }
        config.save()
    }

    private fun requireAuthorizations(config: PixivConfig) {

        clients += config.accounts
                .asSequence()
                .map { PixivClient(it) }

        runBlocking {
            clients.forEach { launch { it.getAuthorization() } }
        }
    }

    override fun fetch(tags: List<String>, Context: FetchContext): Sequence<IImage> {
        if (Context.requires == 0)
            return sequenceOf()
        if (clients.count() == 0)
            return sequenceOf()
        var pageToLoad = Context.requires / PixivConstant.searchPageCount
        if (pageToLoad * PixivConstant.searchPageCount < Context.requires)
            pageToLoad++
        var eachLoad = pageToLoad / clients.count()
        if (eachLoad * clients.count() < pageToLoad)
            eachLoad++
        val result = arrayOfNulls<IImage?>(pageToLoad * PixivConstant.searchPageCount)
        runBlocking {
            clients.forEachIndexed { index, client ->
                launch {
                    val baseIndex = index * eachLoad * PixivConstant.searchPageCount
                    (0 until eachLoad).forEach {
                        launch {
                            client.search(
                                    tags.firstOrNull() ?: "",
                                    PixivClient.SearchType.PartialMatchForTags,
                                    Offset = baseIndex + Context.current + it * PixivConstant.searchPageCount
                            ).forEachIndexed { imageIndex, image ->
                                result[baseIndex + it * PixivConstant.searchPageCount + imageIndex] = image
                            }
                        }
                    }
                }
            }
        }
        return result.asSequence().filterNotNull()
    }
}