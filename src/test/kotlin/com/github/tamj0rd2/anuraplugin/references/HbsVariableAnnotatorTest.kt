package com.github.tamj0rd2.anuraplugin.references

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase

class HbsVariableAnnotatorTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "path/to/your/testdata" // Set the path to your test data files
    }

    fun `test variables that don't have corresponding kotlin fields are highlighted`() {
        setupFiles(
            files = mapOf(
                "ViewModel.kt" to
                    // language=Kt
                    """
                    |data class ViewModel(val greeting: String, val name: String)
                        """.trimMargin(),
                "View.hbs" to
                    // language=Handlebars
                    "<h1>{{greeting}}, {{incorrectName}}</h1>",
            ),
        )

        val highlightedTexts = myFixture.doHighlighting().map { it.text }
        TestCase.assertEquals(listOf("incorrectName"), highlightedTexts)
    }

    fun `test that partial names and their context are not annotated`() {
        setupFiles(
            files = mapOf(
                "View.hbs" to
                    // language=Handlebars
                    """
                    |{{>src/path/to/APartial}}
                    |{{>src/path/to/SomePartialWithContext someContext someTemplateVariable=123}}
                    |{{#>src/path/to/PartialWithBlock}}
                    |    {{someBlockContent}}
                    |{{/src/path/to/PartialWithBlock}}
                    """.trimMargin()
            ),
        )

        val highlightedTexts = myFixture.doHighlighting().map { it.text }
        TestCase.assertEquals(listOf("someBlockContent"), highlightedTexts)
    }

    fun `test that built in references are not highlighted`() {
        setupFiles(
            files = mapOf(
                "View.hbs" to
                    // language=Handlebars
                    """
                    |{{@root.person}}
                    |{{#if @root.person.name}}
                    |    {{@root.person.emailAddress}}
                    |    {{nonRootVariable}}
                    |    {{index}}
                    |    {{@index}}
                    |{{/if}}
                    """.trimMargin()
            ),
        )

        val highlightedTexts = myFixture.doHighlighting().map { it.text }
        TestCase.assertEquals(listOf("nonRootVariable", "index"), highlightedTexts)
    }

    private fun setupFiles(files: Map<String, String>) {
        files.forEach { (fileName, content) -> myFixture.configureByText(fileName, content) }
        myFixture.enableInspections(HbsUnresolvedReferenceInspection::class.java)
    }
}