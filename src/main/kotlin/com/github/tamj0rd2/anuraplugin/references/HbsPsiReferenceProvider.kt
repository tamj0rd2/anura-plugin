package com.github.tamj0rd2.anuraplugin.references

import com.dmarcotte.handlebars.psi.HbBlockWrapper
import com.dmarcotte.handlebars.psi.HbOpenBlockMustache
import com.dmarcotte.handlebars.psi.HbPsiElement
import com.github.tamj0rd2.anuraplugin.handlers.HbsUtils.isHbPsiIdElement
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase.Immediate
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext

class HbsPsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        if (!element.isHbPsiIdElement()) return emptyArray()
        val blockWrapper = element.parentOfType<HbBlockWrapper>() ?: return emptyArray()
        val openBlock = blockWrapper.childrenOfType<HbOpenBlockMustache>().singleOrNull() ?: return emptyArray()
        val idElement = openBlock.children.filterIsInstance<HbPsiElement>().singleOrNull { it.isHbPsiIdElement() }

        if (idElement == null) {
            thisLogger().error("handle case where idElement is null")
            return emptyArray()
        }

        if (idElement == element) return emptyArray()

        if (idElement.textMatches(element)) {
            return arrayOf(Immediate<HbPsiElement>(element, false, idElement))
        }

        return emptyArray()
    }
}
