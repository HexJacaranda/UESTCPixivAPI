package core.interfaces

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.lang.Exception
import kotlin.reflect.KClass

/**异步map*/
suspend fun <In, Out> Iterable<In>.mapAsync(f: suspend (In) -> Out): List<Out> =
        coroutineScope {
            map { async { f(it) } }.awaitAll()
        }

/**异步map*/
suspend fun <A, B> Sequence<A>.mapAsync(f: suspend (A) -> B): Sequence<B> =
        coroutineScope {
            map { async { f(it) } }.toList().awaitAll().asSequence()
        }

suspend fun <A, B> Sequence<A>.mapNotNullAsync(f: suspend (A) -> B?): Sequence<B> =
        coroutineScope {
            map { async { f(it) } }
                    .toList()
                    .awaitAll()
                    .asSequence()
                    .filter { it != null }
                    .map { it!! }
        }

fun <T : Any> KClass<out T>.createInstance(vararg args:Any): T? =
        try {
            this.constructors.first().call(*args)
        } catch (e: Exception) {
            null
        }
