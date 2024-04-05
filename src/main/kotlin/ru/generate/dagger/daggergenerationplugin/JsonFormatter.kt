package ru.generate.dagger.daggergenerationplugin

import com.google.gson.GsonBuilder

object JsonFormatter {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun formatJson(rawJson: String): String {
        return gson.toJson(gson.fromJson(rawJson, Any::class.java))
    }
}