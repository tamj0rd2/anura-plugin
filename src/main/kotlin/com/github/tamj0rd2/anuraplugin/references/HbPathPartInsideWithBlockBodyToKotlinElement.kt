package com.github.tamj0rd2.anuraplugin.references

import com.dmarcotte.handlebars.psi.HbBlockWrapper
import com.dmarcotte.handlebars.psi.HbPath
import com.dmarcotte.handlebars.psi.HbPsiElement
import com.github.tamj0rd2.anuraplugin.psi.HbsPsiHelpers.findContextProvidedByWith
import com.github.tamj0rd2.anuraplugin.psi.HbsPsiHelpers.identifierParts
import com.github.tamj0rd2.anuraplugin.psi.HbsPsiHelpers.withBlockAlias
import com.github.tamj0rd2.anuraplugin.psi.searchForMatchingKotlinDeclaration
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import org.jetbrains.kotlin.idea.caches.KotlinShortNamesCache

class HbPathPartInsideWithBlockBodyToKotlinElement(element: HbPsiElement) : HbsToKotlinReference(element) {
    override fun resolve(): PsiElement? {
        val kotlinShortNamesCache = KotlinShortNamesCache(element.project)

        val withBlockAlias = element.parentOfType<HbBlockWrapper>()?.withBlockAlias()

        val ktElementForImplicitThis = kotlinShortNamesCache.searchForMatchingKotlinDeclaration(
            scope = element.kotlinProductionCodeScope,
            modelName = element.containingFile.virtualFile.nameWithoutExtension,
            hbsIdentifierParts = element.findContextProvidedByWith().identifierParts(),
        )

        return (element.context as HbPath)
            .identifierParts(upToAndIncluding = element)
            .excludeWithBlockAliasIfFirst(withBlockAlias?.text ?: "this")
            .fold(ktElementForImplicitThis) { ktElement, idPart ->
                if (ktElement == null) return@fold null

                val nextElement = kotlinShortNamesCache.searchForMatchingKotlinDeclaration(
                    scope = element.kotlinProductionCodeScope,
                    fieldName = idPart,
                    kotlinElement = ktElement,
                )

                nextElement
            }
    }


    private fun List<String>.excludeWithBlockAliasIfFirst(withBlockAliasName: String?): List<String> {
        if (withBlockAliasName == null) return this
        return if (first() == withBlockAliasName) drop(1) else this
    }
}