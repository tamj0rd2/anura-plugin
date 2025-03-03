package com.github.tamj0rd2.anuraplugin.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.elementType
import fleet.util.singleOrNullOrThrow
import org.jetbrains.kotlin.idea.caches.KotlinShortNamesCache
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType

fun KotlinShortNamesCache.searchForMatchingKotlinDeclaration(
    scope: GlobalSearchScope,
    modelName: String,
    fieldName: String,
): KtElement? {
    val possibleModelNames = buildSet {
        add(modelName)

        if (modelName.endsWith("View")) {
            add(modelName + "Model")
        }
    }

    val matchingModel = possibleModelNames
        .flatMap { getClassesByName(it, scope).toList() }
        .singleOrNullOrThrow()
        ?: return null

    val methodToSearchFor = "get" + fieldName.replaceFirstChar { it.uppercaseChar() }

    val matchingMethod = matchingModel.allMethods
        .asIterable()
        .singleOrNullOrThrow { it.name == methodToSearchFor }
        ?: return null

    return matchingMethod.navigationElement as? KtElement
}

fun KotlinShortNamesCache.searchForMatchingKotlinDeclaration(
    scope: GlobalSearchScope,
    fieldName: String,
    kotlinElement: PsiElement,
): KtElement? {
    when (kotlinElement) {
        is KtParameter -> {
            val ref = kotlinElement.typeReference!!.findDescendantOfType<KtNameReferenceExpression>()!!

            val foundKtElement = searchForMatchingKotlinDeclaration(
                scope = scope,
                modelName = ref.getReferencedName(),
                fieldName = fieldName
            )
            return foundKtElement
        }

        else -> {
            TODO("how do I deal with ${kotlinElement.elementType}?")
        }
    }
}

fun KotlinShortNamesCache.searchForMatchingKotlinDeclaration(
    scope: GlobalSearchScope,
    modelName: String,
    hbsIdentifierParts: List<String>,
): KtElement? {
    var ktElement = searchForMatchingKotlinDeclaration(
        scope = scope,
        modelName = modelName,
        fieldName = hbsIdentifierParts[0],
    ) ?: return null

    for (idPart in hbsIdentifierParts.drop(1)) {
        ktElement = searchForMatchingKotlinDeclaration(
            scope = scope,
            fieldName = idPart,
            kotlinElement = ktElement,
        ) ?: return null
    }

    return ktElement
}
