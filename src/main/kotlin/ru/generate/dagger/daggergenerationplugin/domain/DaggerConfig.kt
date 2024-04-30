package ru.generate.dagger.daggergenerationplugin.domain

data class DaggerConfig(
    val appModule: String,
    val destinationPackage: String,
    val dependencies: List<Dependency>,
    val destinationModule: String,
    var changeGradle: Boolean,
)

data class Dependency(
    val classes: List<String>,
    val module: String
)