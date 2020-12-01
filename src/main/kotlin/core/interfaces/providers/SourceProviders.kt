package core.interfaces.providers

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import core.interfaces.ISource
import core.interfaces.ISourceProvider
import core.interfaces.Probing
import core.interfaces.createInstance
import mu.KotlinLogging
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import org.koin.core.qualifier.qualifier
import java.io.File
import java.io.IOException
import java.util.stream.Collectors
import kotlin.NullPointerException
import kotlin.reflect.KClass

class SourceProviders : ISourceProvider, KoinComponent {
    private val storage: MutableMap<KClass<*>, ISource> = mutableMapOf()
    private val configurationPath: String by inject(named("configuration"))
    private val logger = KotlinLogging.logger("Source Provider:$this")
    private val moshi: Moshi by inject()
    fun loadConfigurations() {
        try {
            logger.info { "Resolving configurations." }
            logger.info { "Configuration path is : $configurationPath" }
            val configurationString = File(configurationPath).readText()
            val configuration = moshi
                    .adapter(ProviderConfiguration::class.java)
                    .fromJson(configurationString)!!
            logger.info { "Configuration version: ${configuration.version}" }
            val types = Probing.probeClass("core.implementation") {
                it.annotations.any { annotation -> annotation is SourceProvider }
            }

            logger.info { "Add dependency to koin." }
            configuration.configurations.forEach { (name, path) ->
                getKoin().declare(path, qualifier(name))
            }

            val sourceConfigs = types
                    .asSequence()
                    .map { it to it.java.getAnnotation(SourceProvider::class.java) }
                    .mapNotNull { (type, provider) ->
                        try {
                            type to moshi.adapter(provider.configType.java)
                                    .fromJson(File(configuration.configurations[provider.name]!!)
                                            .readText())!!
                        } catch (e: Exception) {
                            logger.error(e) { "Error when loading configuration(s)." }
                            null
                        }
                    }.toMap()

            val sources = types
                    .parallelStream()
                    .map {
                        try {
                            it to it.createInstance(sourceConfigs[it]!!) as ISource
                        } catch (e: Exception) {
                            logger.error(e) { }
                            null
                        }
                    }
                    .collect(Collectors.toList())
                    .asSequence()
                    .filterNotNull()

            storage.putAll(sources)

        } catch (e: NullPointerException) {
            logger.error(e) { "Error when constructing sources." }
        } catch (e: IOException) {
            logger.error(e) { "Error when reading configuration." }
        } catch (e: JsonDataException) {
            logger.error(e) { "Configuration json is not correct." }
        } catch (e: Exception) {
            logger.error(e) { "Unknown exception occurs" }
        }
    }

    operator fun get(type: KClass<*>) = synchronized(storage) { storage[type] }
    override val sources: Sequence<ISource>
        get() = storage.values.asSequence()
}