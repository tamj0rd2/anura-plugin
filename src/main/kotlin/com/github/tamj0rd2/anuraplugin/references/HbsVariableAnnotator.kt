package com.github.tamj0rd2.anuraplugin.references

import com.dmarcotte.handlebars.psi.HbCloseBlockMustache
import com.dmarcotte.handlebars.psi.HbData
import com.dmarcotte.handlebars.psi.HbOpenBlockMustache
import com.dmarcotte.handlebars.psi.HbPartial
import com.dmarcotte.handlebars.psi.HbPsiElement
import com.github.tamj0rd2.anuraplugin.handlers.HbsUtils.isHbPsiIdElement
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty

internal class HbsVariableAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (shouldSkipAnnotating(element)) return

        element.references
            .mapNotNull { it.resolve() }
            .filterIsInstance<KtElement>()
            .ifNotEmpty { return }

        holder.newSilentAnnotation(HighlightSeverity.ERROR)
            .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            .create()
    }

    private fun shouldSkipAnnotating(element: PsiElement): Boolean {
        if (!element.isHbPsiIdElement()) return true
        if (element.isAPartialOrPartialContext()) return true
        if (element.reliesOnBuiltIn()) return true
        return false
    }

    private fun HbPsiElement.isAPartialOrPartialContext() =
        parents.any {
            it is HbPartial
                    || it is HbOpenBlockMustache
                    || it is HbCloseBlockMustache
        }

    private fun HbPsiElement.reliesOnBuiltIn() = parentOfType<HbData>(true) != null
}
