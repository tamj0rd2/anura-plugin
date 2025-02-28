package com.github.tamj0rd2.anuraplugin.references

import com.dmarcotte.handlebars.psi.HbPsiElement
import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulator

class HbsElementManipulator : ElementManipulator<HbPsiElement> {
    override fun handleContentChange(element: HbPsiElement, range: TextRange, newContent: String?): HbPsiElement? {
        TODO("Not yet implemented")
    }

    override fun handleContentChange(element: HbPsiElement, newContent: String?): HbPsiElement? {
        TODO("Not yet implemented")
    }

    override fun getRangeInElement(element: HbPsiElement): TextRange {
        return TextRange(0, element.textLength)
    }
}