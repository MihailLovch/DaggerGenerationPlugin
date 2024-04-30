package ru.generate.dagger.daggergenerationplugin.data.repository

import com.android.tools.idea.appinspection.inspectors.backgroundtask.view.capitalizedName
import ru.generate.dagger.daggergenerationplugin.data.FileGenerator
import ru.generate.dagger.daggergenerationplugin.data.ParserResponse
import ru.generate.dagger.daggergenerationplugin.data.ProjectParser
import ru.generate.dagger.daggergenerationplugin.data.builder.*
import ru.generate.dagger.daggergenerationplugin.util.NotificationManager

interface DaggerRepository {

    fun findClasses(classNames: List<String>, moduleName: String): List<String>

    fun generateFeatureClasses(
        moduleName: String,
        requiredClasses: List<String>,
        foundModules: List<String>,
        editGradleFile: Boolean
    )

    fun editAppComponent(moduleName: String, appModuleName: String, editGradleFile: Boolean)
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
        }
            .also { if (notFoundMessage.isNotEmpty()) notificationManager.showErrorNotification(notFoundMessage.toString()) }
    }

    override fun generateFeatureClasses(
        moduleName: String,
        requiredClasses: List<String>,
        foundModules: List<String>,
        editGradleFile: Boolean
    ) {
        val className = moduleName.split(".").last().capitalizedName()
        val packageName =
            projectParser.getLastRootPackageWithMultipleSubpackages(moduleName) ?: throw RuntimeException()
        val files = listOf(
            "${className}Component.kt" to buildComponentClass(packageName, className),
            "${className}ViewModel.kt" to buildViewModelClass(packageName, className),
            "${className}DepsProvider.kt" to buildDepsProviderClass(packageName, className),
            "${className}Dependencies.kt" to buildDependenciesClass(packageName, className, requiredClasses)
        )
        fileGenerator.generateClassesInModule(moduleName, files)
        if (editGradleFile) {
            projectParser.findBuildGradleFileInModule(moduleName)?.let {
                fileGenerator.addGradleDependency(it, foundModules.map { module -> ":${module.replace('.', ':')}" })
            }
        }
    }

    override fun editAppComponent(
        moduleName: String,
        appModuleName: String,
        editGradleFile: Boolean
    ) {
        val depsInterface = moduleName.split(".").last().capitalizedName() + "Dependencies"
        val appModule =
            projectParser.findMainModule(appModuleName) ?: throw RuntimeException("app module not found")
        val appPackageName =
            projectParser.getLastRootPackageWithMultipleSubpackages(appModuleName) ?: throw RuntimeException()
        val featureModule =
            projectParser.findMainModule(moduleName) ?: throw RuntimeException("feature module not found")
        val depsFullName = (projectParser.getQualifiedClassName(
            depsInterface,
            featureModule
        ) as ParserResponse.ClassFound).qualifiedClassName

        projectParser.findFile(
            module = appModule,
            fileName = "AppComponent.kt"
        )?.let {
            fileGenerator.appendAppComponent(it, depsFullName)
        } ?: fileGenerator.generateClassesInModule(
            appModuleName,
            listOf("AppComponent.kt" to buildAppComponentClass(appPackageName, depsFullName))
        )

        if (editGradleFile){
            projectParser.findBuildGradleFileInModule(appModuleName)?.let {
                fileGenerator.addGradleDependency(it, listOf(":${moduleName.replace('.', ':')}"))
            }
        }
    }
}