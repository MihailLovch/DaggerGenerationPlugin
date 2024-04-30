package ru.generate.dagger.daggergenerationplugin.util

import com.intellij.openapi.ui.Messages

class NotificationManager {

    fun showErrorNotification(message: String) {
        Messages.showErrorDialog(message,"Error")
    }
}
