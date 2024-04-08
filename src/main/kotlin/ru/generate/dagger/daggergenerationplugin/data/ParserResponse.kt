package ru.generate.dagger.daggergenerationplugin.data

sealed interface ParserResponse {

    data class ClassFound(val qualifiedClassName: String): ParserResponse

    sealed interface Error: ParserResponse {

        data class ClassNotFound(val name: String): Error
        data class ModuleNotFound(val name: String): Error
    }
}