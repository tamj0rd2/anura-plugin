package com.github.tamj0rd2.anuraplugin.references

import com.dmarcotte.handlebars.psi.HbCloseBlockMustache
import com.dmarcotte.handlebars.psi.HbData
import com.dmarcotte.handlebars.psi.HbOpenBlockMustache
import com.dmarcotte.handlebars.psi.HbPartial
import com.dmarcotte.handlebars.psi.HbPsiElement
import com.github.tamj0rd2.anuraplugin.handlers.HbsUtils.isHbPsiIdElement
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.parentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

class HbsUnresolvedReferenceInspection : LocalInspectionTool() {
    override fun isEnabledByDefault() = true

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (shouldSkipInspection(element)) return

                element.references
                    .filter { it.resolve() == null }
                    .forEach { unresolvableReference ->
                        holder.registerProblem(unresolvableReference, ProblemHighlightType.ERROR)
                    }
            }
        }
    }

    private fun shouldSkipInspection(element: PsiElement): Boolean {
        if (!element.isHbPsiIdElement()) return true
        if (element.isAPartialOrPartialContext()) return true
        if (element.isANonRootDataPath()) return true
        return false
    }

    private fun HbPsiElement.isAPartialOrPartialContext() =
        parents.any {
            it is HbPartial
                    || it is HbOpenBlockMustache
                    || it is HbCloseBlockMustache
        }

    private fun HbPsiElement.isANonRootDataPath(): Boolean {
        val hbData = parentOfType<HbData>(true) ?: return false
        return !hbData.text.startsWith("@root")
    }
}
