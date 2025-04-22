package com.github.tamj0rd2.anuraplugin.references

import com.dmarcotte.handlebars.psi.HbData
import com.dmarcotte.handlebars.psi.impl.HbPsiElementImpl
import com.github.tamj0rd2.anuraplugin.psi.HbsPsiHelpers.identifierParts
import com.github.tamj0rd2.anuraplugin.psi.searchForMatchingKotlinDeclaration
import com.github.tamj0rd2.anuraplugin.psi.searchForViewModelByName
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch
import org.jetbrains.kotlin.idea.caches.KotlinShortNamesCache
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.psiUtil.containingClass

class HbDataPartToKotlinElement(element: HbPsiElementImpl) : HbsToKotlinReference(element) {
    override fun resolve(): PsiElement? {
        val hbData = element.context as? HbData ?: return null
        val idParts = hbData.identifierParts(upToAndIncluding = element)
        if (idParts.first() != "root") TODO("HbData only supported for finding stuff from @root for now")

        val kotlinShortNamesCache = KotlinShortNamesCache(element.project)

        var rootModelName = element.containingFile.virtualFile.nameWithoutExtension

        while(!rootModelName.endsWith("ViewModel")) {
            val psiClass =  kotlinShortNamesCache.searchForViewModelByName(
                scope = element.kotlinProductionCodeScope,
                modelName = rootModelName,
            ) ?: break

            rootModelName = ReferencesSearch.search(psiClass)
                .asSequence()
                .mapNotNull { it.element }
                .filterIsInstance<KtElement>()
                .mapNotNull { it.containingClass()?.name }
                .firstOrNull() ?: break
        }

        return kotlinShortNamesCache.searchForMatchingKotlinDeclaration(
            scope = element.kotlinProductionCodeScope,
            modelName = rootModelName,
            hbsIdentifierParts = idParts.drop(1),
        )
    }
}
