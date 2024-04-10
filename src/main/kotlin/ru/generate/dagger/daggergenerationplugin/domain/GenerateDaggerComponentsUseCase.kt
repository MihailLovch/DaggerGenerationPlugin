package ru.generate.dagger.daggergenerationplugin.domain

import com.intellij.openapi.ui.Messages
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty
import ru.generate.dagger.daggergenerationplugin.data.repository.DaggerRepository

class GenerateDaggerComponentsUseCase(
    private val daggerRepository: DaggerRepository
) {

    operator fun invoke(daggerConfig: DaggerConfig) {
        daggerConfig.dependencies.forEach { dependency ->
            daggerRepository.findClasses(
                dependency.classes,
                dependency.module
            ).ifNotEmpty {
                Messages.showMessageDialog(
                    null,
                    "Found class: $this",
                    "Information",
                    Messages.getInformationIcon()
                )
            }
        }
    }
}