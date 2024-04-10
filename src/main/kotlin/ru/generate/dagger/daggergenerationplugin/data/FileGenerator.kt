package ru.generate.dagger.daggergenerationplugin.data

import com.android.tools.idea.appinspection.inspectors.backgroundtask.view.capitalizedName
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiPackage
import org.jetbrains.kotlin.psi.KtPsiFactory
import ru.generate.dagger.daggergenerationplugin.data.builder.buildComponentClass
import ru.generate.dagger.daggergenerationplugin.data.builder.buildDependenciesClass
import ru.generate.dagger.daggergenerationplugin.data.builder.buildDepsProviderClass
import ru.generate.dagger.daggergenerationplugin.data.builder.buildViewModelClass

class FileGenerator(
    private val project: Project,
    private val projectParser: ProjectParser
) {

    fun generateClassInModule(moduleName: String, requiredClasses: List<String>) {
        // Find the package directory within the module
        val packageDir = findOrCreatePackageDirectory(project, moduleName)
            ?: run {
                println("Package directory not found for module: $moduleName")
                return
            }

        // Perform write action to modify PSI elements
        WriteCommandAction.runWriteCommandAction(project) {
            val psiFactory = KtPsiFactory(project)
            val className = moduleName.split(".").last().capitalizedName()
            val packageName = projectParser.getLastRootPackageWithMultipleSubpackages(moduleName) ?: throw NullPointerException()

            val componentKtFile = psiFactory.createFile(buildComponentClass(packageName,className)).apply {
                name = "${className}Component.kt"
            }
            val viewModelKtFile = psiFactory.createFile(buildViewModelClass(packageName,className)).apply {
                name = "${className}ViewModel.kt"
            }
            val depsProviderKtFile = psiFactory.createFile(buildDepsProviderClass(packageName,className)).apply {
                name = "${className}DepsProvider.kt"
            }
            val dependenciesKtFile = psiFactory.createFile(buildDependenciesClass(packageName,className,requiredClasses)).apply {
                name = "${className}Dependencies.kt"
            }

            packageDir.add(componentKtFile)
            packageDir.add(viewModelKtFile)
            packageDir.add(depsProviderKtFile)
            packageDir.add(dependenciesKtFile)

            println("Class generated successfully in module: $moduleName")
        }
    }

    private fun findOrCreatePackageDirectory(project: Project, moduleName: String): PsiDirectory? {
        val psiManager = JavaPsiFacade.getInstance(project)
        val packageFqName = projectParser.getLastRootPackageWithMultipleSubpackages(moduleName) ?: throw NullPointerException()
        val psiPackage: PsiPackage = psiManager.findPackage(packageFqName) ?: run {
            println("Package not found: $packageFqName")
            return null
        }
        val requiredPackage = psiPackage.directories.filterNot { dir ->
            dir.toString().contains("generated") || dir.toString().contains("test") || dir.toString().contains("androidTest")
        }
        return requiredPackage.firstOrNull()
    }
}