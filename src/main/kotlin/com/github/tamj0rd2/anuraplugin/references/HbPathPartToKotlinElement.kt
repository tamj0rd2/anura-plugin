package com.github.tamj0rd2.anuraplugin.references

import com.dmarcotte.handlebars.psi.HbPath
import com.dmarcotte.handlebars.psi.HbPsiElement
import com.github.tamj0rd2.anuraplugin.psi.HbsPsiHelpers.identifierParts
import com.github.tamj0rd2.anuraplugin.psi.searchForMatchingKotlinDeclaration
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.caches.KotlinShortNamesCache

class HbPathPartToKotlinElement(element: HbPsiElement) : HbsToKotlinReference(element) {
    override fun resolve(): PsiElement? {
        val kotlinShortNamesCache = KotlinShortNamesCache(element.project)

        return kotlinShortNamesCache.searchForMatchingKotlinDeclaration(
            scope = element.kotlinProductionCodeScope,
            modelName = element.containingFile.virtualFile.nameWithoutExtension,
            hbsIdentifierParts = (element.context as HbPath).identifierParts(upToAndIncluding = element),
        )
    }
}