package com.github.tamj0rd2.anuraplugin.references

import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.junit.Test

class HbsToKotlinPsiReferenceProviderRegressionTest : LightPlatformCodeInsightFixture4TestCase() {
    @Test
    fun `going to declaration of variable that is a kotlin Boolean field`() {
        // properties that start with `is` don't get prefixed with `get` when creating the java method name.
        runGoToKotlinDeclarationTest(
            // language=Kt
            kotlinFileContent = "data class ViewModel(val isActive: Boolean)",
            handlebarsFileContent = "<h1>{{<caret>isActive}}, world</h1>",
            expectedReferences = listOf(
                ExpectedReference(
                    name = "isActive",
                    definedBy = "ViewModel"
                )
            )
        )
    }

    private data class ExpectedReference(
        val name: String,
        val definedBy: String,
    )

    private fun runGoToKotlinDeclarationTest(
        kotlinFileContent: String,
        handlebarsFileContent: String,
        expectedReferences: List<ExpectedReference>,
    ) = runGoToKotlinDeclarationTest(
        files = mapOf(
            "ViewModel.kt" to kotlinFileContent,
            "View.hbs" to handlebarsFileContent,
        ),
        expectedReferences = expectedReferences,
    )

    private fun runGoToKotlinDeclarationTest(
        files: Map<String, String>,
        expectedReferences: List<ExpectedReference>,
    ) {
        files.forEach { (fileName, content) -> myFixture.configureByText(fileName, content) }

        val targetElements =
            GotoDeclarationAction.findAllTargetElements(project, myFixture.editor, myFixture.caretOffset)
                .map {
                    ExpectedReference(
                        name = (it as KtNamedDeclaration).name ?: "undetermined",
                        definedBy = it.getParentOfType<KtClassOrObject>(true)?.name ?: "undetermined"
                    )
                }

        assertEquals(expectedReferences, targetElements)
    }
}