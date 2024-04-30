package ru.generate.dagger.daggergenerationplugin.data

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPackage
import org.jetbrains.kotlin.psi.KtPsiFactory
import ru.generate.dagger.daggergenerationplugin.util.NotificationManager


class FileGenerator(
    private val project: Project,
    private val projectParser: ProjectParser,
    private val notificationManager: NotificationManager
) {
    private val psiFactory = KtPsiFactory(project)

    fun generateClassesInModule(moduleName: String, files: List<Pair<String, String>>) {
        val packageDir = findOrCreatePackageDirectory(project, moduleName)
            ?: run {
                println("Package directory not found for module: $moduleName")
                return
            }
        WriteCommandAction.runWriteCommandAction(project) {
            files.map { nameToCode ->
                psiFactory.createFile(nameToCode.second).apply { name = nameToCode.first }
            }.forEach { newFile ->
                try {
                    packageDir.add(newFile)
                } catch (ignored: Exception) {
                    notificationManager.showErrorNotification("${newFile.name} already exists")
                }
            }
        }
    }

    fun appendAppComponent(appComponent: PsiFile, depsQualifiedName: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            val content = StringBuilder(appComponent.text)
            val interfaceName = depsQualifiedName.split(".").last()
            if (!content.toString().contains(": $interfaceName") && !content.toString().contains(", $interfaceName ")) {
                val insertionPosition = findInsertionPosition(content.toString())
                content.insert(
                    insertionPosition.second + 1,
                    if (insertionPosition.first) " $interfaceName," else ": $interfaceName"
                )
            }

            if (!content.toString().contains("import $depsQualifiedName")) {
                content.insert(findImportPosition(content.toString()), "\nimport $depsQualifiedName")
            }
            if (appComponent.text != content.toString()) {
                val dir = appComponent.parent
                appComponent.delete()
                dir?.createFile("AppComponent.kt")?.apply {
                    virtualFile.setBinaryContent(content.toString().toByteArray())
                }
            }
        }
    }

    fun addGradleDependency(gradleFile: PsiFile, depsNames: List<String>) {
        WriteCommandAction.runWriteCommandAction(project) {
            val content = StringBuilder(gradleFile.text)
            depsNames.forEach { name ->
                val addDeps = "implementation(project(\"$name\"))"
                if (!content.toString().contains(addDeps)) {
                    content.insert(
                        content.toString().indexOf("dependencies {") + "dependencies {".length,
                        "\n    $addDeps"
                    )
                }
            }
            if (gradleFile.text != content.toString()) {
                val dir = gradleFile.parent
                gradleFile.delete()
                dir?.createFile("build.gradle.kts")?.apply {
                    virtualFile.setBinaryContent(content.toString().toByteArray())
                }
            }
        }
    }

    private fun findImportPosition(input: String, keyword: String = "import"): Int {
        val startIndex = input.indexOf(keyword) + keyword.length
        val trimmedString = input.substring(startIndex).trim()
        val words = trimmedString.split("\\s+".toRegex())

        return if (words.size > 1) {
            startIndex + words[0].length + 1
        } else {
            -1
        }
    }


    private fun findInsertionPosition(input: String): Pair<Boolean, Int> {
        input.forEachIndexed { index, char ->
            if (char == ':') {
                return true to index
            }
        }
        return false to (input.indexOf("AppComponent") + 11)
    }


    private fun findOrCreatePackageDirectory(project: Project, moduleName: String): PsiDirectory? {
        val psiManager = JavaPsiFacade.getInstance(project)
        val packageFqName = projectParser.getLastRootPackageWithMultipleSubpackages(moduleName) ?: return null
        val psiPackage: PsiPackage = psiManager.findPackage(packageFqName) ?: run {
            println("Package not found: $packageFqName")
            return null
        }
        val requiredPackage = psiPackage.directories.filterNot { dir ->
            dir.toString().contains("\\generated\\") || dir.toString().contains("\\test\\") || dir.toString()
                .contains("\\androidTest\\")
        }
        return requiredPackage.firstOrNull()
    }
}