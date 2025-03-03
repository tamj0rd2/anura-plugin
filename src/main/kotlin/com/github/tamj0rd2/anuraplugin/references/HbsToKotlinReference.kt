package com.github.tamj0rd2.anuraplugin.references

import com.dmarcotte.handlebars.psi.HbPsiElement
import com.github.tamj0rd2.anuraplugin.handlers.HbsUtils.isHbPsiIdElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.idea.base.util.module
import org.jetbrains.kotlin.idea.base.util.restrictToKotlinSources

abstract class HbsToKotlinReference(idElement: HbPsiElement) : PsiReferenceBase<HbPsiElement>(idElement, false) {

    init {
        require(idElement.isHbPsiIdElement()) { "Expected an ID element but got ${idElement.elementType}" }
    }

    companion object {
        val HbPsiElement.kotlinProductionCodeScope
            get() = module!!.moduleProductionSourceScope.restrictToKotlinSources()
    }
}