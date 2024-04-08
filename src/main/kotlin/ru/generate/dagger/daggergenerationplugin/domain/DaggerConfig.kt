package ru.generate.dagger.daggergenerationplugin.domain

data class DaggerConfig(
    val appModule: String,
    val defaultPackage: String,
    val dependencies: List<Dependency>,
    val module: String
)

data class Dependency(
    val classes: List<String>,
    val module: String
)