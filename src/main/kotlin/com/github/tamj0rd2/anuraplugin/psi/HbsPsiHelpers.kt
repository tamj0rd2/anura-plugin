package com.github.tamj0rd2.anuraplugin.psi

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbBlockWrapper
import com.dmarcotte.handlebars.psi.HbData
import com.dmarcotte.handlebars.psi.HbOpenBlockMustache
import com.dmarcotte.handlebars.psi.HbParam
import com.dmarcotte.handlebars.psi.HbPath
import com.dmarcotte.handlebars.psi.HbPsiElement
import com.github.tamj0rd2.anuraplugin.handlers.HbsUtils.isHbPsiIdElement
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.util.takeWhileInclusive

object HbsPsiHelpers {

    private fun HbPsiElement.isAWithBlockAlias(): Boolean {
        val openBlock = parentOfType<HbOpenBlockMustache>(false) ?: return false
        if (openBlock.name != "with") return false
        if (prevSibling?.elementType != HbTokenTypes.OPEN_BLOCK_PARAMS) return false
        if (nextSibling?.elementType != HbTokenTypes.CLOSE_BLOCK_PARAMS) return false
        return true
    }

    fun HbBlockWrapper.withBlockAlias(): HbPsiElement? {
        if (name != "with") return null
        return getChildOfType<HbOpenBlockMustache>()
            ?.children
            ?.filterIsInstance<HbPsiElement>()
            ?.singleOrNull { it.isAWithBlockAlias() }
    }

    private fun HbPsiElement.isAnEachBlockAlias(): Boolean {
        val openBlock = parentOfType<HbOpenBlockMustache>(false) ?: return false
        if (openBlock.name != "each") return false
        if (prevSibling?.elementType != HbTokenTypes.OPEN_BLOCK_PARAMS) return false
        if (nextSibling?.elementType != HbTokenTypes.CLOSE_BLOCK_PARAMS) return false
        return true
    }

    fun HbBlockWrapper.eachBlockAlias(): HbPsiElement? {
        if (name != "each") return null
        return getChildOfType<HbOpenBlockMustache>()
            ?.children
            ?.filterIsInstance<HbPsiElement>()
            ?.singleOrNull { it.isAnEachBlockAlias() }
    }

    fun HbPsiElement.findContextProvidedByWith() =
        parentOfType<HbBlockWrapper>()
            ?.takeIf { it.name == "with" }
            ?.getChildOfType<HbOpenBlockMustache>()
            ?.getChildOfType<HbParam>()
            ?.findDescendantOfType<HbPath>()
            ?: error("could not find source of implicit this.")

    fun HbPsiElement.findContextProvidedByEach() =
        parentOfType<HbBlockWrapper>()
            ?.takeIf { it.name == "each" }
            ?.getChildOfType<HbOpenBlockMustache>()
            ?.getChildOfType<HbParam>()
            ?.findDescendantOfType<HbPath>()
            ?: error("could not find source of implicit this.")


    fun HbPath.identifierParts(upToAndIncluding: HbPsiElement? = null) =
        childIdElements()
            .toList()
            .takeWhileInclusive { upToAndIncluding == null || !it.isEquivalentTo(upToAndIncluding) }
            .map { it.text }

    private fun HbPath.childIdElements() =
        childrenOfType<HbPsiElement>()
            .asSequence()
            .filter { it.isHbPsiIdElement() }


    fun HbData.identifierParts(upToAndIncluding: HbPsiElement? = null) =
        childrenOfType<HbPsiElement>()
            .asSequence()
            .filter { it.isHbPsiIdElement() }
            .toList()
            .takeWhileInclusive { upToAndIncluding == null || !it.isEquivalentTo(upToAndIncluding) }
            .map { it.text }
}