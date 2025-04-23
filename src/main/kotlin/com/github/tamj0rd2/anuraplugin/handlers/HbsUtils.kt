package com.github.tamj0rd2.anuraplugin.handlers

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbPsiElement
import com.dmarcotte.handlebars.psi.impl.HbPsiElementImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object HbsUtils {
    fun PsiElement.isHbsIdElement(): Boolean {
        return this.elementType == HbTokenTypes.ID
    }

    @OptIn(ExperimentalContracts::class)
    fun PsiElement.isHbPsiIdElement(): Boolean {
        contract { returns(true) implies(this@isHbPsiIdElement is HbPsiElementImpl) }
        return this.elementType == HbTokenTypes.ID && this is HbPsiElement
    }
}