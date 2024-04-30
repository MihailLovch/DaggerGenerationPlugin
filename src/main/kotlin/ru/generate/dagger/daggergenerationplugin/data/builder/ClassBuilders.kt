package ru.generate.dagger.daggergenerationplugin.data.builder

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

fun buildDependenciesClass(packageName: String, featureName: String, requiredClasses: List<String>): String = buildString {
    append("package $packageName\n\n")
    for (dependency in requiredClasses) {
        append("import $dependency\n")
    }
    append("\n")
    append("interface ${featureName}Dependencies {\n\n")
    for (dependency in requiredClasses.map { it.split(".").last() }) {
        append("   fun provide${dependency}(): $dependency\n\n")
    }
    append("}")
}

fun buildAppComponentClass(packageName: String, depsQualifiedName: String) = """
    package $packageName

    import dagger.Component
    import $depsQualifiedName

    @Component
    interface AppComponent : ${depsQualifiedName.split(".").last()} 
""".trimIndent()