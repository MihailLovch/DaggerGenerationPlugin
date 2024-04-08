package ru.generate.dagger.daggergenerationplugin.ui

import com.google.gson.JsonSyntaxException
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import ru.generate.dagger.daggergenerationplugin.servicelocator.Locator
import ru.generate.dagger.daggergenerationplugin.util.JsonUtil
import javax.swing.JComponent

class DataDialogAction : AnAction() {

    private val generateDaggerComponentsUseCase by lazy {Locator.generateDaggerComponentsUseCase}

    override fun actionPerformed(event: AnActionEvent) {
        DataDialogWrapper { json ->
            Locator.injectProject(event.project ?: throw NullPointerException())
            generateDaggerComponentsUseCase(
                daggerConfig = JsonUtil.toDaggerConfig(json)
            )
        }.show()
    }
}


private class DataDialogWrapper(
    private val onGenerateClicked: (String) -> Unit
) : DialogWrapper(true) {

    private var textArea = JBTextArea()
    private var gradleCheckBox = JBCheckBox()
    private val exampleJson =
        "{\"appModule\":\"app\",\"module\":\"feature-auth\",\"defaultPackage\":\"ru.kpfu.itis\",\"dependencies\":[{\"module\":\"common\",\"classes\":[\"NetworkProvide\",\"RoomProvider\"]}]}"

    init {
        init()
        title = "Enter Json"
        setOKButtonText("Generate")
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row {
                textArea().apply {
                    text(JsonUtil.formatJson(exampleJson))
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
                        textArea.text = JsonUtil.formatJson(textArea.text)
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
        onGenerateClicked(textArea.text)
    }
}