package com.github.tamj0rd2.anuraplugin.references

import com.github.tamj0rd2.anuraplugin.handlers.HbsUtils.isHbPsiIdElement
import com.github.tamj0rd2.anuraplugin.services.HbsService
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.resolveFromRootOrRelative
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase.Immediate
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.util.descendantsOfType
import com.intellij.util.ProcessingContext
import org.jetbrains.kotlin.asJava.classes.KtLightClassBase
import org.jetbrains.kotlin.asJava.elements.KtLightField
import org.jetbrains.kotlin.asJava.elements.KtLightMethod
import org.jetbrains.kotlin.idea.base.psi.typeArguments
import org.jetbrains.kotlin.nj2k.types.typeFqName
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTypeProjection

class HbsToKotlinPsiReferenceProvider : PsiReferenceProvider() {
    override fun acceptsTarget(target: PsiElement): Boolean {
        return target.isHbPsiIdElement()
    }

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        if (!acceptsTarget(element)) return emptyArray()

        val hbsService = element.project.service<HbsService>()

        val declarations = recursivelyFindMatchingKotlinReferences(
            typesToSearchIn = setOf(hbsFileToKotlinModelName(hbsFile = element.containingFile.virtualFile)),
            hbsIdentifierParts = hbsService.getHbsIdentifierParts(element),
            scope = element.containingFile.toKotlinProductionScopeOrDefault()
        )

        return declarations.map { Immediate(element, it) }.toTypedArray()
    }

    private tailrec fun recursivelyFindMatchingKotlinReferences(
        typesToSearchIn: Set<String>,
        hbsIdentifierParts: List<String>,
        scope: GlobalSearchScope,
    ): Collection<KtDeclaration> {
        if (hbsIdentifierParts.isEmpty()) return emptyList()

        if (hbsIdentifierParts.first() == "this") {
            return recursivelyFindMatchingKotlinReferences(
                typesToSearchIn = typesToSearchIn,
                hbsIdentifierParts = hbsIdentifierParts.drop(1),
                scope = scope
            )
        }

        val psiShortNamesCache = PsiShortNamesCache.getInstance(scope.project)

        val models = typesToSearchIn
            .flatMap { psiShortNamesCache.findKotlinClassesByName(it, scope) }
            .distinctBy { it.node }

        val matchingFields = models
            .flatMap { it.allFieldsAndProperties }
            .filter { it.name == hbsIdentifierParts.first() }

        if (hbsIdentifierParts.size == 1) return matchingFields

        return recursivelyFindMatchingKotlinReferences(
            typesToSearchIn = matchingFields.map { it.referencedTypeName() }.toSet(),
            hbsIdentifierParts = hbsIdentifierParts.drop(1),
            scope = scope,
        )
    }

    private fun PsiShortNamesCache.findKotlinClassesByName(modelName: String, scope: GlobalSearchScope): List<KtLightClassBase> {
        return getClassesByName(modelName, scope)
            .filterIsInstance<KtLightClassBase>()
            .distinctBy { it.node }
    }

    private fun PsiFile.toKotlinProductionScopeOrDefault(): GlobalSearchScope {
        if (!virtualFile.path.contains("src/main/resources")) return GlobalSearchScope.projectScope(project)

        var folderToSearch = this.virtualFile
        while (!folderToSearch.path.endsWith("src/main")) {
            folderToSearch = folderToSearch.parent
        }
        folderToSearch = folderToSearch.resolveFromRootOrRelative("kotlin")!!

        return GlobalSearchScopesCore.directoryScope(project, folderToSearch, true)
    }

    private companion object {
        fun KtDeclaration.referencedTypeName(): String {
            if (this.isAKotlinList()) {
                return typeReference.typeArguments().single().referencedTyped.getReferencedName()
            }

            return referencedTyped.getReferencedName()
        }

        fun KtDeclaration.isAKotlinList(): Boolean {
            if (this !is KtParameter) return false

            @Suppress("DEPRECATION", "UnstableApiUsage")
            return typeFqName()?.asString() == "kotlin.collections.List"
        }

        val KtDeclaration.referencedTyped
            get() = typeReference!!.descendantsOfType<KtNameReferenceExpression>().first()

        val KtTypeProjection.referencedTyped
            get() = typeReference!!.descendantsOfType<KtNameReferenceExpression>().first()

        val KtLightClassBase.allFieldsAndProperties: List<KtDeclaration>
            get() = allProperties + allFields.filterIsInstance<KtLightField>().mapNotNull { it.kotlinOrigin }

        val KtLightClassBase.allProperties: List<KtProperty>
            get() = allMethods
                .filterIsInstance<KtLightMethod>()
                .map { it.kotlinOrigin }
                .filterIsInstance<KtProperty>()

        val KtDeclaration.typeReference
            get() = when (this) {
                is KtParameter -> this.typeReference
                is KtProperty -> this.typeReference
                else -> error("unsupported type ${this::class.java}")
            }

        fun hbsFileToKotlinModelName(hbsFile: VirtualFile): String {
            if (hbsFile.nameWithoutExtension.endsWith("View")) {
                return hbsFile.nameWithoutExtension + "Model"
            }

            return hbsFile.nameWithoutExtension
        }
    }
}

