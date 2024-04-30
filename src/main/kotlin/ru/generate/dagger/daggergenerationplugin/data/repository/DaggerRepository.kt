package ru.generate.dagger.daggergenerationplugin.data.repository

import com.android.tools.idea.appinspection.inspectors.backgroundtask.view.capitalizedName
import ru.generate.dagger.daggergenerationplugin.data.FileGenerator
import ru.generate.dagger.daggergenerationplugin.data.ParserResponse
import ru.generate.dagger.daggergenerationplugin.data.ProjectParser
import ru.generate.dagger.daggergenerationplugin.data.builder.*
import ru.generate.dagger.daggergenerationplugin.util.NotificationManager

interface DaggerRepository {

    fun findClasses(classNames: List<String>, moduleName: String): List<String>

    fun generateFeatureClasses(moduleName: String, requiredClasses: List<String>)

    fun editAppComponent(moduleName: String, appModuleName: String)
}

class DaggerRepositoryImpl(
    private val projectParser: ProjectParser,
    private val fileGenerator: FileGenerator,
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

    override fun generateFeatureClasses(moduleName: String, requiredClasses: List<String>) {
        val className = moduleName.split(".").last().capitalizedName()
        val packageName =
            projectParser.getLastRootPackageWithMultipleSubpackages(moduleName) ?: throw NullPointerException()
        println("$moduleName ;    $packageName")
        val files = listOf(
            "${className}Component.kt" to buildComponentClass(packageName, className),
            "${className}ViewModel.kt" to buildViewModelClass(packageName, className),
            "${className}DepsProvider.kt" to buildDepsProviderClass(packageName, className),
            "${className}Dependencies.kt" to buildDependenciesClass(packageName, className, requiredClasses)
        )
        fileGenerator.generateClassesInModule(moduleName, files)
    }

    override fun editAppComponent(moduleName: String, appModuleName: String) {
        val depsInterface = moduleName.split(".").last().capitalizedName() + "Dependencies"
        val appModule = projectParser.findModule(appModuleName) ?: throw NullPointerException("app module not found")
        val appPackageName =
            projectParser.getLastRootPackageWithMultipleSubpackages(appModuleName) ?: throw NullPointerException()
        val featureModule = projectParser.findModule(moduleName) ?: throw NullPointerException("feature module not found")
        val depsFullName = (projectParser.getQualifiedClassName(depsInterface,featureModule) as ParserResponse.ClassFound).qualifiedClassName

        projectParser.findFile(
            module = appModule,
            fileName = "AppComponent"
        )?.let {
            fileGenerator.appendAppComponent(it, depsFullName)
        } ?: fileGenerator.generateClassesInModule(appModuleName, listOf("AppComponent.kt" to buildAppComponentClass(appPackageName,depsFullName)))
    }
}