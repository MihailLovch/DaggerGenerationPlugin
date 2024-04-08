package ru.generate.dagger.daggergenerationplugin.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

class NotificationManager(
    private val project: Project,
) {

    fun showErrorNotification(message: String) {
        Messages.showErrorDialog(message,"Error")
    }
}
