package ru.generate.dagger.daggergenerationplugin.util

import com.google.gson.GsonBuilder
import ru.generate.dagger.daggergenerationplugin.domain.DaggerConfig

object JsonUtil {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun formatJson(rawJson: String): String {
        return gson.toJson(gson.fromJson(rawJson, Any::class.java))
    }

    fun toDaggerConfig(daggerConfig: String): DaggerConfig {
        return gson.fromJson(daggerConfig, DaggerConfig::class.java)
    }
}