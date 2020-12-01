package core.interfaces

import io.github.classgraph.ClassGraph
import mu.KotlinLogging
import kotlin.reflect.KClass

object Probing {
    private val logger = KotlinLogging.logger("Probing Module Logger")
    fun probeClass(packageName: String, predicate: (Class<*>) -> Boolean): List<KClass<*>> {
        if (packageName.isEmpty())
            return listOf()

        try {
            val allClasses = ClassGraph()
                    .enableClassInfo()
                    .acceptPackages(packageName)
                    .scan()
                    .allClasses

            return allClasses.map { it.loadClass() }
                    .asSequence()
                    .filter(predicate)
                    .map { it.kotlin }
                    .toList()
        } catch (e: Exception) {
            logger.error(e) { "Unexpected exception occurred." }
            return listOf()
        }
    }

    fun implements(targetInterface: KClass<*>, value: KClass<*>): Boolean = targetInterface.java.isAssignableFrom(value.java) && targetInterface != value
    fun implements(targetInterface: Class<*>, value: Class<*>): Boolean = targetInterface.isAssignableFrom(value) && targetInterface != value
}