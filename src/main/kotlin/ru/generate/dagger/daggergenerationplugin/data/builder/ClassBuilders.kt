package ru.generate.dagger.daggergenerationplugin.data.builder

import androidx.compose.ui.text.toLowerCase

fun buildComponentClass(packageName: String, featureName: String) = """
        package $packageName
        
        import dagger.Component
        
        @Component(
            dependencies = [${featureName}Dependencies::class]
        )
        interface ${featureName}Component {

            @Component.Builder
            interface Builder {

                fun deps(deps: ${featureName}Dependencies): Builder

                fun build(): ${featureName}Component
            }
        }
        """.trimIndent()

fun buildViewModelClass(packageName: String, featureName: String) = """
    package $packageName
    
    import androidx.lifecycle.ViewModel

    internal class ${featureName}ComponentViewModel : ViewModel() {

        val ${featureName.lowercase()}Component = Dagger${featureName}Component.builder()
            .deps(${featureName}DepsProvider.deps)
            .build()
    }
""".trimIndent()

fun buildDepsProviderClass(packageName: String, featureName: String) = """
    package $packageName
    
    import kotlin.properties.Delegates.notNull

    interface ${featureName}DepsProvider {

        var deps: ${featureName}Dependencies

        companion object : ${featureName}DepsProvider by ${featureName}DepsStore
    }

    object ${featureName}DepsStore : ${featureName}DepsProvider {
        override var deps: ${featureName}Dependencies by notNull()
    }
""".trimIndent()

fun buildDependenciesClass(packageName: String, featureName: String, requiredClasses: List<String>): String {

    val classContent = StringBuilder()

    classContent.append("package $packageName\n\n")
    for (dependency in requiredClasses) {
        classContent.append("import $dependency\n")
    }
    classContent.append("\n")
    classContent.append("interface ${featureName}Dependencies {\n")
    for (dependency in requiredClasses.map { it.split(".").last() }) {
        classContent.append("   fun provide${dependency}(): $dependency\n\n")
    }
    classContent.append("}\n")

    return classContent.toString()
}