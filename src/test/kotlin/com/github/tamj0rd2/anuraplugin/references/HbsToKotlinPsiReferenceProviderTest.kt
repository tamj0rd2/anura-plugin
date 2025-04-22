package com.github.tamj0rd2.anuraplugin.references

import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.junit.Test

class HbsToKotlinPsiReferenceProviderTest : LightPlatformCodeInsightFixture4TestCase() {
    @Test
    fun `going to declaration of variable that is a kotlin field`() {
        runGoToKotlinDeclarationTest(
            // language=Kt
            kotlinFileContent = "data class ViewModel(val greeting: String?)",
            handlebarsFileContent = "<h1>{{<caret>greeting}}, world</h1>",
            expectedReferences = listOf(
                ExpectedReference(
                    name = "greeting",
                    definedBy = "ViewModel"
                )
            )
        )
    }

    @Test
    fun `going to declaration of variable that is a kotlin property`() {
        runGoToKotlinDeclarationTest(
            // language=Kt
            kotlinFileContent = """data class ViewModel(val name: String) { val greeting = "Hello" }""",
            handlebarsFileContent = "<h1>{{<caret>greeting}}, {{name}}</h1>",
            expectedReferences = listOf(
                ExpectedReference(
                    name = "greeting",
                    definedBy = "ViewModel"
                )
            )
        )
    }

    @Test
    fun `going to declaration of variable that is a kotlin computed property`() {
        runGoToKotlinDeclarationTest(
            // language=Kt
            kotlinFileContent = """data class ViewModel(val name: String) { val greeting get() = "Hello" }""",
            handlebarsFileContent = "<h1>{{<caret>greeting}}, {{name}}</h1>",
            expectedReferences = listOf(
                ExpectedReference(
                    name = "greeting",
                    definedBy = "ViewModel"
                )
            )
        )
    }

    @Test
    fun `going to declaration of variable that includes nesting`() {
        runGoToKotlinDeclarationTest(
            files = mapOf(
                "ViewModel.kt" to
                        // language=Kt
                        """
                        |package models
                        |data class ViewModel(val person: Person)
                        """.trimMargin(),
                "Person.kt" to
                        // language=Kt
                        """
                        |package models
                        |data class Person(val name: String)
                        """.trimMargin(),
                "View.hbs" to
                        "<h1>{{<caret>person.name}}, world</h1>",
            ),
            expectedReferences = listOf(
                ExpectedReference(
                    name = "person",
                    definedBy = "ViewModel"
                )
            )
        )
    }

    @Test
    fun `going to declaration of variable that includes nesting - nested`() {
        runGoToKotlinDeclarationTest(
            files = mapOf(
                "ViewModel.kt" to
                        // language=Kt
                        """
                        |package models
                        |data class ViewModel(val person: Person)
                        """.trimMargin(),
                "Person.kt" to
                        // language=Kt
                        """
                        |package models
                        |data class Person(val name: String)
                        """.trimMargin(),
                "View.hbs" to
                        "<h1>{{person.<caret>name}}, world</h1>",
            ),
            expectedReferences = listOf(
                ExpectedReference(
                    name = "name",
                    definedBy = "Person"
                )
            )
        )
    }

    @Test
    fun `going to declaration of variable used in if block`() {
        runGoToKotlinDeclarationTest(
            // language=Kt
            kotlinFileContent = "data class ViewModel(val greeting: String?)",
            handlebarsFileContent = "{{#if <caret>greeting}}<h1>Hello world</h1>{{/if}}",
            expectedReferences = listOf(
                ExpectedReference(
                    name = "greeting",
                    definedBy = "ViewModel"
                )
            )
        )
    }

    @Test
    fun `going to declaration of variable used in if block - with more than 1 id part`() {
        runGoToKotlinDeclarationTest(
            // language=Kt
            kotlinFileContent =
                """
                data class Person(val name: String?)
                data class ViewModel(val person: Person)
                """.trimIndent(),
            handlebarsFileContent = "{{#if <caret>person.name}}<h1>Hello world</h1>{{/if}}",
            expectedReferences = listOf(
                ExpectedReference(
                    name = "person",
                    definedBy = "ViewModel"
                )
            )
        )
    }

    @Test
    fun `going to declaration of variable used in if block - when targeting the 2nd id part`() {
        runGoToKotlinDeclarationTest(
            // language=Kt
            kotlinFileContent =
                """
                data class Person(val name: String?)
                data class ViewModel(val person: Person)
                """.trimIndent(),
            handlebarsFileContent = "{{#if person.<caret>name}}<h1>Hello world</h1>{{/if}}",
            expectedReferences = listOf(
                ExpectedReference(
                    name = "name",
                    definedBy = "Person"
                )
            )
        )
    }

    @Test
    fun `going to declaration of param of with block`() {
        runGoToKotlinDeclarationTest(
            kotlinFileContent =
                // language=Kt
                """
                |data class ViewModel(val person: Person)
                """.trimMargin(),
            handlebarsFileContent =
                """
                |{{#with <caret>person}}{{/with}}
                """.trimMargin(),
            expectedReferences = listOf(
                ExpectedReference(
                    name = "person",
                    definedBy = "ViewModel"
                )
            )
        )
    }

    @Test
    fun `going to declaration of param of with block - with more than 1 id part`() {
        runGoToKotlinDeclarationTest(
            kotlinFileContent =
                // language=Kt
                """
                |data class Person(val name: String)
                |data class ViewModel(val person: Person)
                """.trimMargin(),
            handlebarsFileContent =
                """
                |{{#with <caret>person.name}}{{/with}}
                """.trimMargin(),
            expectedReferences = listOf(
                ExpectedReference(
                    name = "person",
                    definedBy = "ViewModel"
                )
            )
        )
    }

    @Test
    fun `going to declaration of param of with block - when targeting the 2nd id part`() {
        runGoToKotlinDeclarationTest(
            kotlinFileContent =
                // language=Kt
                """
                |data class Person(val name: String)
                |data class ViewModel(val person: Person)
                """.trimMargin(),
            handlebarsFileContent =
                """
                |{{#with person.<caret>name}}{{/with}}
                """.trimMargin(),
            expectedReferences = listOf(
                ExpectedReference(
                    name = "name",
                    definedBy = "Person"
                )
            )
        )
    }

    @Test
    fun `going to declaration of variable used in with block`() {
        runGoToKotlinDeclarationTest(
            kotlinFileContent =
                // language=Kt
                """
                |data class Person(val age: String)
                |data class ViewModel(val person: Person)
                """.trimMargin(),
            handlebarsFileContent =
                """
                |{{#with person}}<h1>{{<caret>age}}</h1>{{/with}}
                """.trimMargin(),
            expectedReferences = listOf(
                ExpectedReference(
                    name = "age",
                    definedBy = "Person"
                )
            )
        )
    }

    @Test
    fun `going to declaration of variable used in with block - using explicit this`() {
        runGoToKotlinDeclarationTest(
            kotlinFileContent =
                // language=Kt
                """
                |data class Person(val age: String)
                |data class ViewModel(val person: Person)
                """.trimMargin(),
            handlebarsFileContent =
                """
                |{{#with person}}<h1>{{this.<caret>age}}</h1>{{/with}}
                """.trimMargin(),
            expectedReferences = listOf(
                ExpectedReference(
                    name = "age",
                    definedBy = "Person"
                )
            )
        )
    }

    @Test
    fun `going to declaration of variable used in with block - 1st nested id part`() {
        runGoToKotlinDeclarationTest(
            kotlinFileContent =
                // language=Kt
                """
                |data class Address(val postCode: String)
                |data class Person(val address: String)
                |data class ViewModel(val person: Person)
                """.trimMargin(),
            handlebarsFileContent =
                """
                |{{#with person}}<h1>{{<caret>address.postCode}}</h1>{{/with}}
                """.trimMargin(),
            expectedReferences = listOf(
                ExpectedReference(
                    name = "address",
                    definedBy = "Person"
                )
            )
        )
    }

    @Test
    fun `going to declaration of variable used in with block - 2nd nested id part`() {
        runGoToKotlinDeclarationTest(
            kotlinFileContent =
                // language=Kt
                """
                |data class Address(val postcode: String)
                |data class Person(val address: Address)
                |data class ViewModel(val person: Person)
                """.trimMargin(),
            handlebarsFileContent =
                """
                |{{#with person}}<h1>{{address.<caret>postcode}}</h1>{{/with}}
                """.trimMargin(),
            expectedReferences = listOf(
                ExpectedReference(
                    name = "postcode",
                    definedBy = "Address"
                )
            )
        )
    }

    @Test
    fun `going to declaration of with block alias within a mustache`() {
        runGoToKotlinDeclarationTest(
            kotlinFileContent =
                // language=Kt
                """
                |data class Person(val age: String)
                |data class ViewModel(val person: Person)
                """.trimMargin(),
            handlebarsFileContent =
                """
                |{{#with person as |p|}}
                |{{<caret>p}}
                |{{/with}}
                """.trimMargin(),
            expectedReferences = listOf(
                ExpectedReference(
                    name = "person",
                    definedBy = "ViewModel"
                )
            )
        )
    }

    @Test
    fun `going to declaration of variable used in named with block`() {
        runGoToKotlinDeclarationTest(
            kotlinFileContent =
                // language=Kt
                """
                |data class Person(val age: String)
                |data class ViewModel(val person: Person)
                """.trimMargin(),
            handlebarsFileContent =
                """
                |{{#with person as |p|}}<h1>{{p.<caret>age}}</h1>{{/with}}
                """.trimMargin(),
            expectedReferences = listOf(
                ExpectedReference(
                    name = "age",
                    definedBy = "Person"
                )
            )
        )
    }

    @Test
    fun `going to declaration of variable used in each block`() {
        runGoToKotlinDeclarationTest(
            kotlinFileContent =
                // language=Kt
                """
                |data class Person(val name: String, val age: String)
                |data class ViewModel(val people: List<Person>)
                """.trimMargin(),
            handlebarsFileContent =
                """
                |{{#each people}}
                |{{this.<caret>age}
                |{{/each}}
                """.trimMargin(),
            expectedReferences = listOf(
                ExpectedReference(
                    name = "age",
                    definedBy = "Person"
                )
            )
        )
    }

    @Test
    fun `going to declaration of variable used in each block - without using this as reference`() {
        runGoToKotlinDeclarationTest(
            kotlinFileContent =
                // language=Kt
                """
                |data class Person(val name: String, val age: String)
                |data class ViewModel(val people: List<Person>)
                """.trimMargin(),
            handlebarsFileContent =
                """
                |{{#each people}}<h1>{{name}} is {{<caret>age}}</h1>{{/each}}
                """.trimMargin(),
            expectedReferences = listOf(
                ExpectedReference(
                    name = "age",
                    definedBy = "Person"
                )
            )
        )
    }

    @Test
    fun `going to declaration of variable used in each block with nesting`() {
        runGoToKotlinDeclarationTest(
            kotlinFileContent =
                // language=Kt
                """
                |data class Person(val name: String, val age: String)
                |data class Nested(val people: List<Person>)
                |data class ViewModel(val nested: Nested)
                """.trimMargin(),
            handlebarsFileContent =
                """
                |{{#each nested.people}}<h1>{{name}} is {{<caret>age}}</h1>{{/each}}
                """.trimMargin(),
            expectedReferences = listOf(
                ExpectedReference(
                    name = "age",
                    definedBy = "Person"
                )
            )
        )
    }

    @Test
    fun `going to declaration of variable used in named each block`() {
        runGoToKotlinDeclarationTest(
            kotlinFileContent =
                // language=Kt
                """
                |data class Person(val name: String, val age: String)
                |data class ViewModel(val people: List<Person>)
                """.trimMargin(),
            handlebarsFileContent =
                """
                |{{#each people as |person|}}<h1>{{person.<caret>age}}</h1>{{/each}}
                """.trimMargin(),
            expectedReferences = listOf(
                ExpectedReference(
                    name = "age",
                    definedBy = "Person"
                )
            )
        )
    }

    @Test
    fun `going to declaration of variable used in each block nested within an if block`() {
        runGoToKotlinDeclarationTest(
            kotlinFileContent =
                // language=Kt
                """
                |data class Person(val age: String)
                |data class ViewModel(val people: List<Person>)
                """.trimMargin(),
            handlebarsFileContent =
                """
                |{{#if people}}
                |    {{#each people}}
                |        <h1>{{this.<caret>age}}</h1>
                |    {{/each}}
                |{{/if}}
                """.trimMargin(),
            expectedReferences = listOf(
                ExpectedReference(
                    name = "age",
                    definedBy = "Person"
                )
            )
        )
    }

    @Test
    fun `can go to a definition in the viewmodel, using the root prefix`() {
        runGoToKotlinDeclarationTest(
            // language=Kt
            kotlinFileContent = "data class ViewModel(val greeting: String?)",
            handlebarsFileContent = "<h1>{{@root.<caret>greeting}}, world</h1>",
            expectedReferences = listOf(
                ExpectedReference(
                    name = "greeting",
                    definedBy = "ViewModel"
                )
            )
        )
    }

    @Test
    fun `from within a partial - can go to a definition within the root view model`() {
        runGoToKotlinDeclarationTest(
            files = mapOf(
                "View.kt" to
                        // language=Kt
                        """
                        |data class Person(val name: String)
                        |data class ViewModel(val somethingAtRoot: Int, val person: Person)
                        """.trimMargin(),
                "Person.hbs" to
                        "<h1>{{@root.<caret>somethingAtRoot}}, world</h1>",
            ),
            expectedReferences = listOf(
                ExpectedReference(
                    name = "somethingAtRoot",
                    definedBy = "ViewModel"
                )
            )
        )
    }

    @Test
    fun `from within a deep partial - can go to a definition within the root view model`() {
        runGoToKotlinDeclarationTest(
            files = mapOf(
                "View.kt" to
                        // language=Kt
                        """
                        |data class Deep(val name: String)
                        |data class Person(val deep: Deep)
                        |data class ViewModel(val somethingAtRoot: Int, val person: Person)
                        """.trimMargin(),
                "Person.hbs" to
                        "<h1>{{@root.<caret>somethingAtRoot}}, world</h1>",
            ),
            expectedReferences = listOf(
                ExpectedReference(
                    name = "somethingAtRoot",
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