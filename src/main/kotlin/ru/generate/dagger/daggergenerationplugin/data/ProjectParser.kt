package ru.generate.dagger.daggergenerationplugin.data

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache

class ProjectParser(
    private val project: Project
) {

    private val psiShortNames = PsiShortNamesCache.getInstance(project)

    fun getQualifiedClassName(className: String, module: Module): ParserResponse {
        val classes = psiShortNames.getClassesByName(className, GlobalSearchScope.moduleScope(module))
        return if (classes.isEmpty()) {
            ParserResponse.Error.ClassNotFound(className)
        } else {
            ParserResponse.ClassFound(
                classes.first().qualifiedName ?: throw NullPointerException("Qualified class name $className is null")
            )
        }
    }

    fun getQualifiedClassNames(classNames: List<String>, moduleName: String): List<ParserResponse> {
        val module = findMainModule(moduleName) ?: return listOf(ParserResponse.Error.ModuleNotFound(moduleName))
        return classNames.map {
            getQualifiedClassName(it, module)
        }
    }


    fun findMainModule(moduleName: String): Module? {
        return project.modules.find {
            it.name.endsWith(".${moduleName}.main")
        }
    }

    fun findFile(module: Module, fileName: String): PsiFile? {
        val moduleScope = GlobalSearchScope.moduleScope(module)
        return FilenameIndex.getFilesByName(project,fileName, moduleScope).firstOrNull()
    }

    fun findBuildGradleFileInModule(moduleName: String): PsiFile? {
        val module = project.modules.first { it.name.endsWith(".$moduleName") }
        val psiManager = PsiManager.getInstance(project)

        for (contentRoot in ModuleRootManager.getInstance(module).contentRoots) {
            val moduleFiles = contentRoot.children ?: continue
            for (file in moduleFiles) {
                if (file.name == "build.gradle.kts") {
                    val psiFile = psiManager.findFile(file)
                    if (psiFile != null) {
                        return psiFile
                    }
                }
            }
        }
        return null
    }

    fun getLastRootPackageWithMultipleSubpackages(moduleName: String): String? {
        val psiFacade = JavaPsiFacade.getInstance(project)
        val moduleScope = GlobalSearchScope.moduleScope(findMainModule(moduleName) ?: return null)

        var currentPackage = psiFacade.findPackage("")
        var subPackages = currentPackage?.getSubPackages(moduleScope)

        do {
            currentPackage = subPackages?.first()
            subPackages = currentPackage?.getSubPackages(moduleScope)
        }while (subPackages?.size!! == 1)

        return currentPackage?.qualifiedName
    }
}
