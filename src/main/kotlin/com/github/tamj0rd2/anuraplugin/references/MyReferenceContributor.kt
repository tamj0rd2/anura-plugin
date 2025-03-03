package com.github.tamj0rd2.anuraplugin.references

import com.dmarcotte.handlebars.HbLanguage
import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbPsiElement
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar

class MyReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HbPsiElement::class.java)
                .withLanguage(HbLanguage.INSTANCE)
                .withElementType(HbTokenTypes.ID),
            HbsPsiReferenceProvider()
        )
    }
}

