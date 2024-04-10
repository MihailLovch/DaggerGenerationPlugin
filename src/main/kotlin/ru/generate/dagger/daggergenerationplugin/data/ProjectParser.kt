package ru.generate.dagger.daggergenerationplugin.data

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.psi.JavaPsiFacade
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
        val module = findModule(moduleName) ?: return listOf(ParserResponse.Error.ModuleNotFound(moduleName))

        return classNames.map {
            getQualifiedClassName(it, module)
        }
    }


    fun findModule(moduleName: String): Module? {
        return project.modules.find {
            it.name.endsWith(".${moduleName}.main")
        }
    }


    fun getLastRootPackageWithMultipleSubpackages(moduleName: String): String? {
        val psiFacade = JavaPsiFacade.getInstance(project)
        val moduleScope = GlobalSearchScope.moduleScope(findModule(moduleName)!!)

        var currentPackage = psiFacade.findPackage("")
        var subPackages = currentPackage?.getSubPackages(moduleScope)

        while (subPackages?.size!! == 1) {
            currentPackage = subPackages.first()
            subPackages = currentPackage?.getSubPackages(moduleScope)
        }

        return currentPackage?.qualifiedName
    }
}
