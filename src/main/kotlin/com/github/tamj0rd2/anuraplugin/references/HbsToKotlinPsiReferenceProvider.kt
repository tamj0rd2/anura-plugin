package com.github.tamj0rd2.anuraplugin.references

import com.github.tamj0rd2.anuraplugin.handlers.HbsUtils.isHbPsiIdElement
import com.github.tamj0rd2.anuraplugin.services.HbsService
import com.github.tamj0rd2.anuraplugin.services.MyProjectService
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase.Immediate
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext

class HbsToKotlinPsiReferenceProvider : PsiReferenceProvider() {
    override fun acceptsTarget(target: PsiElement): Boolean {
        return target.isHbPsiIdElement()
    }

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        if (!acceptsTarget(element)) return emptyArray()

        val kotlinService = element.project.service<MyProjectService>()
        val hbsService = element.project.service<HbsService>()

        val declarations = kotlinService.findKotlinReferences(
            hbsFile = element.containingFile.virtualFile,
            hbsIdentifierParts = hbsService.getHbsIdentifierParts(element)
        )

        return declarations.map { Immediate(element, it) }.toTypedArray()
    }
}

