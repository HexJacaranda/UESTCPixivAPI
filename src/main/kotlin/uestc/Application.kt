package uestc

import com.squareup.moshi.Moshi
import core.interfaces.ISourceProvider
import core.interfaces.providers.SourceProviders
import io.micronaut.runtime.Micronaut.*
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File

fun main(args: Array<String>) {
	val application = startKoin {
		modules(
				module {
					single<ISourceProvider> {
						SourceProviders()
								.apply { this.loadConfigurations() }
					}
					single(named("configuration")) { "./configuration.json" }
					single<Moshi> { Moshi.Builder().build() }
				}
		)
	}

	/**主动加载Source Provider*/
	application.koin.get<ISourceProvider>()

	build()
			.args(*args)
			.packages("uestc")
			.start()
}

