package com.github.tamj0rd2.anuraplugin.references

import com.dmarcotte.handlebars.psi.HbCloseBlockMustache
import com.dmarcotte.handlebars.psi.HbOpenBlockMustache
import com.dmarcotte.handlebars.psi.HbPartial
import com.dmarcotte.handlebars.psi.HbPsiElement
import com.github.tamj0rd2.anuraplugin.handlers.HbsUtils.isHbPsiIdElement
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty

internal class HbsVariableAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (!element.isHbPsiIdElement()) return
        if (element.isAPartialOrWithinAPartial()) return

        element.references
            .mapNotNull { it.resolve() }
            .filterIsInstance<KtElement>()
            .ifNotEmpty { return }

        holder.newSilentAnnotation(HighlightSeverity.ERROR)
            .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            .create()
    }

    private fun HbPsiElement.isAPartialOrWithinAPartial() =
        parents.any {
            it is HbPartial
                    || it is HbOpenBlockMustache
                    || it is HbCloseBlockMustache
        }
}
