package ru.generate.dagger.daggergenerationplugin.domain

import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty
import ru.generate.dagger.daggergenerationplugin.data.repository.DaggerRepository

class GenerateDaggerComponentsUseCase(
    private val daggerRepository: DaggerRepository
) {

    operator fun invoke(daggerConfig: DaggerConfig) = with(daggerConfig) {
        val requiredClasses = mutableListOf<String>()
        dependencies.forEach { dependency ->
            daggerRepository.findClasses(
                dependency.classes,
                dependency.module
            ).ifNotEmpty {
                requiredClasses.addAll(this)
            }
        }
        daggerRepository.generateFeatureClasses(destinationModule, requiredClasses)
        daggerRepository.editAppComponent(destinationModule, appModule)
    }
}