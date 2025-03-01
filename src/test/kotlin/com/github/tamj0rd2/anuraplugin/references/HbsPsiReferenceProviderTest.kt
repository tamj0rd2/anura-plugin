package com.github.tamj0rd2.anuraplugin.references

import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import junit.framework.TestCase

class HbsPsiReferenceProviderTest : LightPlatformCodeInsightTestCase() {
    fun `test going to alias within named each block`() {
        configureFromFileText(
            "View.hbs",
            """
            {{#each people as | person |}}
                {{<caret>person.name}]
            {{/each}}
            """.trimIndent(),
            true
        )

        val foundReference = GotoDeclarationAction.findTargetElement(project, editor, editor.caretModel.offset)
        TestCase.assertEquals("person", foundReference?.text)
    }

    fun `test going to alias within named with block`() {
        configureFromFileText(
            "View.hbs",
            """
            {{#with people as | person |}}
                {{<caret>person.name}]
            {{/each}}
            """.trimIndent(),
            true
        )

        val foundReference = GotoDeclarationAction.findTargetElement(project, editor, editor.caretModel.offset)
        TestCase.assertEquals("person", foundReference?.text)
    }
}
