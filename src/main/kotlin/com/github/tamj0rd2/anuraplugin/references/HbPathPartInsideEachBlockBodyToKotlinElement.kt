package com.github.tamj0rd2.anuraplugin.references

import com.dmarcotte.handlebars.psi.HbBlockWrapper
import com.dmarcotte.handlebars.psi.HbPath
import com.dmarcotte.handlebars.psi.HbPsiElement
import com.github.tamj0rd2.anuraplugin.psi.HbsPsiHelpers.eachBlockAlias
import com.github.tamj0rd2.anuraplugin.psi.HbsPsiHelpers.findContextProvidedByEach
import com.github.tamj0rd2.anuraplugin.psi.HbsPsiHelpers.identifierParts
import com.github.tamj0rd2.anuraplugin.psi.searchForMatchingKotlinDeclaration
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import org.jetbrains.kotlin.idea.caches.KotlinShortNamesCache
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType

class HbPathPartInsideEachBlockBodyToKotlinElement(element: HbPsiElement) : HbsToKotlinReference(element) {
    override fun resolve(): PsiElement? {
        val kotlinShortNamesCache = KotlinShortNamesCache(element.project)

        // TODO: having 3 methods for the same thing is confusing. Refactor searchForMatchingKotlinDeclaration.
        val ktElementForEachBlockTarget = kotlinShortNamesCache.searchForMatchingKotlinDeclaration(
            scope = element.kotlinProductionCodeScope,
            modelName = element.containingFile.virtualFile.nameWithoutExtension,
            hbsIdentifierParts = element.findContextProvidedByEach().identifierParts(),
        )

        val eachBlockAlias = element.parentOfType<HbBlockWrapper>()?.eachBlockAlias()?.text ?: "this"

        return kotlinShortNamesCache.searchForMatchingKotlinDeclaration(
            scope = element.kotlinProductionCodeScope,
            modelName = ktElementForEachBlockTarget?.findCollectionTypeArgumentName()!!,
            hbsIdentifierParts = (element.context as HbPath)
                .identifierParts(upToAndIncluding = element)
                .excludeEachBlockAliasIfFirst(eachBlockAlias),
        )
    }

    // TODO: improve this. There has to be a way better than this hacky mess!
    private fun KtElement.findCollectionTypeArgumentName(): String? {
        if (this !is KtParameter) return null
        val userType = typeReference?.typeElement as? KtUserType ?: return null
        if (userType.referencedName !in setOf("List", "Collection", "Set")) return null

        return userType
            .typeArguments
            .single()
            .typeReference
            ?.findDescendantOfType<KtNameReferenceExpression>()
            ?.getReferencedName()
    }

    private fun List<String>.excludeEachBlockAliasIfFirst(withBlockAliasName: String?): List<String> {
        if (withBlockAliasName == null) return this
        return if (first() == withBlockAliasName) drop(1) else this
    }
}