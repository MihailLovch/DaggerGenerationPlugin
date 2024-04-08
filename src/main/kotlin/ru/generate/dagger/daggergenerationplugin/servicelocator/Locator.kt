package ru.generate.dagger.daggergenerationplugin.servicelocator

import com.intellij.openapi.project.Project
import ru.generate.dagger.daggergenerationplugin.data.ProjectParser
import ru.generate.dagger.daggergenerationplugin.domain.GenerateDaggerComponentsUseCase
import ru.generate.dagger.daggergenerationplugin.repository.DaggerRepository
import ru.generate.dagger.daggergenerationplugin.repository.DaggerRepositoryImpl
import ru.generate.dagger.daggergenerationplugin.util.NotificationManager

object Locator {

    private lateinit var project: Project

    val projectParser by lazy {
        ProjectParser(
            project = project
        )
    }

    val generateDaggerComponentsUseCase by lazy {
        GenerateDaggerComponentsUseCase(
            daggerRepository = daggerRepository
        )
    }

    val notificationManager by lazy {
        NotificationManager(project)
    }

    val daggerRepository: DaggerRepository by lazy {
        DaggerRepositoryImpl(
            projectParser = projectParser,
            notificationManager = notificationManager
        )
    }

    fun injectProject(project: Project) {
        this.project = project
    }
}