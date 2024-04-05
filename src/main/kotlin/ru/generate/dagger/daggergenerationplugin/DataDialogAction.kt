package ru.generate.dagger.daggergenerationplugin

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import java.awt.Dimension
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JTextField
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

class DataDialogAction: AnAction() {
    override fun actionPerformed(p0: AnActionEvent) {
        DataDialogWrapper().show()
    }

    private class DataDialogWrapper : DialogWrapper(true) {

        private var textArea = JBTextArea()
        private var gradleCheckBox = JBCheckBox()
        private val exampleJson = "{\"app-module\":\"app\",\"module\":\"feature-auth\",\"default-package\":\"ru.kpfu.itis\",\"dependencies\":[{\"module\":\"common\",\"classes\":[\"NetworkProvide\",\"RoomProvider\"]}]}"

        init {
            init()
            title = "Enter Json"
            setOKButtonText("Generate")
        }

        override fun createCenterPanel(): JComponent {
            return panel {
                row {
                    textArea().apply {
                        text(JsonFormatter.formatJson(exampleJson))
                        isResizable = true
                        textArea = this.component
                    }
                }
                row {
                    checkBox("Edit gradle files").apply {
                        gradleCheckBox = this.component
                    }
                    button("Format Json") {
                        try {
                            textArea.text = JsonFormatter.formatJson(textArea.text)
                        } catch (e: JsonSyntaxException) {
                            Messages.showMessageDialog(
                                    null,
                                    "Wrong Json format",
                                    "Something Went Wrong :(",
                                    Messages.getInformationIcon()
                            )
                        }
                    }
                }
            }
        }

        override fun doOKAction() {
            super.doOKAction()
            val inputText = textArea.text
            Messages.showMessageDialog(
                    null,
                    "Entered Text: $inputText",
                    "Information",
                    Messages.getInformationIcon()
            )
        }
    }
}