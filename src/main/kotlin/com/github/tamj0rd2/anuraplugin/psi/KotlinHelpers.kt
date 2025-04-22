package com.github.tamj0rd2.anuraplugin.psi

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.idea.caches.KotlinShortNamesCache
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType

fun KotlinShortNamesCache.searchForViewModelByName(
    scope: GlobalSearchScope,
    modelName: String,
): PsiClass? {
    val possibleModelNames = buildSet {
        add(modelName)

        if (modelName.endsWith("View")) {
            add(modelName + "Model")
        }
    }

    return possibleModelNames
        .asSequence()
        .flatMap { getClassesByName(it, scope).toList() }
        .firstOrNull()
}

fun KotlinShortNamesCache.searchForMatchingKotlinDeclaration(
    scope: GlobalSearchScope,
    modelName: String,
    fieldName: String,
): KtElement? {
    val matchingModel = searchForViewModelByName(scope = scope, modelName = modelName) ?: return null

    val methodToSearchFor = fieldName.toGeneratedJavaName()

    val matchingMethod = matchingModel.allMethods
        .asIterable()
        .firstOrNull { it.name == methodToSearchFor }
        ?: return null

    return matchingMethod.navigationElement as? KtElement
}

private fun String.toGeneratedJavaName() =
    if (startsWith("is")) this
    else "get" + replaceFirstChar { it.uppercaseChar() }

fun KotlinShortNamesCache.searchForMatchingKotlinDeclaration(
    scope: GlobalSearchScope,
    fieldName: String,
    kotlinElement: PsiElement,
): KtElement? {
    val typeReference = when(kotlinElement) {
        is KtParameter -> kotlinElement.typeReference
        is KtProperty -> kotlinElement.typeReference
        else -> TODO("how do I deal with ${kotlinElement.elementType}?")
    }

    val ref = typeReference!!.findDescendantOfType<KtNameReferenceExpression>()!!

    val foundKtElement = searchForMatchingKotlinDeclaration(
        scope = scope,
        modelName = ref.getReferencedName(),
        fieldName = fieldName
    )
    return foundKtElement
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
