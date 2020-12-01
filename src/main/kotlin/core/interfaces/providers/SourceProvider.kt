package core.interfaces.providers

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention
@MustBeDocumented
annotation class SourceProvider(val configType: KClass<*>, val name: String)