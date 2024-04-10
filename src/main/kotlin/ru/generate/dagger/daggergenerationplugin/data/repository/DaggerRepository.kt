package ru.generate.dagger.daggergenerationplugin.data.repository

import ru.generate.dagger.daggergenerationplugin.data.ParserResponse
import ru.generate.dagger.daggergenerationplugin.data.ProjectParser
import ru.generate.dagger.daggergenerationplugin.util.NotificationManager

interface DaggerRepository {

    fun findClasses(classNames: List<String>, moduleName: String): List<String>
}

class DaggerRepositoryImpl(
    private val projectParser: ProjectParser,
    private val notificationManager: NotificationManager
) : DaggerRepository {

    override fun findClasses(classNames: List<String>, moduleName: String): List<String> {
        val notFoundMessage = StringBuilder()
        return projectParser.getQualifiedClassNames(
            classNames = classNames,
            moduleName = moduleName
        ).mapNotNull { response ->
            when (response) {
                is ParserResponse.ClassFound -> response.qualifiedClassName
                is ParserResponse.Error.ClassNotFound -> {
                    notFoundMessage.append("Not found class: ${response.name} \n")
                    null
                }

                is ParserResponse.Error.ModuleNotFound -> {
                    notFoundMessage.append("Not found module: ${response.name} \n")
                    null
                }
            }
        }.also { if (notFoundMessage.isNotEmpty()) notificationManager.showErrorNotification(notFoundMessage.toString()) }
    }
}