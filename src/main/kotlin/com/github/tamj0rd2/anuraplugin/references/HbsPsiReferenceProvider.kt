package com.github.tamj0rd2.anuraplugin.references

import com.dmarcotte.handlebars.psi.HbBlockWrapper
import com.dmarcotte.handlebars.psi.HbOpenBlockMustache
import com.dmarcotte.handlebars.psi.HbPath
import com.dmarcotte.handlebars.psi.impl.HbPsiElementImpl
import com.github.tamj0rd2.anuraplugin.handlers.HbsUtils.isHbPsiIdElement
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.ResolveState
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext


class HbsPsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        if (!element.isHbPsiIdElement()) return emptyArray()
        val resolutionContext = getResolutionContext(element)

        if (element.context is HbPath && resolutionContext.get(isInScopeOfWithBlockBody) == true) {
            return arrayOf(HbPathPartInsideWithBlockBodyToKotlinElement(element))
        }

        if (element.context is HbPath && resolutionContext.get(isInScopeOfEachBlockBody) == true) {
            return arrayOf(HbPathPartInsideEachBlockBodyToKotlinElement(element))
        }

        if (element.context is HbPath) {
            return arrayOf(HbPathPartToKotlinElement(element))
        }

        thisLogger().error("don't know how to resolve this one")
        return emptyArray()
    }

    private fun getResolutionContext(element: HbPsiElementImpl): ResolveState {
        var resolveState = ResolveState.initial()
        PsiTreeUtil.treeWalkUp(element, element.containingFile) { scope, prevScope ->
            when (scope) {
                is HbBlockWrapper -> {
                    if (scope.name == "with" && prevScope !is HbOpenBlockMustache) {
                        resolveState = resolveState.put(isInScopeOfWithBlockBody, true)
                    }

                    if (scope.name == "each" && prevScope !is HbOpenBlockMustache) {
                        resolveState = resolveState.put(isInScopeOfEachBlockBody, true)
                    }

                    true
                }

                else -> true
            }
        }
        return resolveState
    }

    private companion object {
        val isInScopeOfWithBlockBody = Key<Boolean>("is in scope of with block body")
        val isInScopeOfEachBlockBody = Key<Boolean>("is in scope of each block body")
    }
}
