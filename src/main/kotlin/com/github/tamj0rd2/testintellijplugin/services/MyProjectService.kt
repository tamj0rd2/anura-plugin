package com.github.tamj0rd2.testintellijplugin.services

import com.dmarcotte.handlebars.psi.HbPsiFile
import com.dmarcotte.handlebars.psi.HbSimpleMustache
import com.github.tamj0rd2.testintellijplugin.MyBundle
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.descendantsOfType
import org.jetbrains.kotlin.asJava.classes.KtLightClassBase
import org.jetbrains.kotlin.asJava.elements.KtLightField
import org.jetbrains.kotlin.asJava.elements.KtLightMethod
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

interface IMyProjectService {
    fun findReferenceInKotlin(ktModelName: String, hbsIdentifierParts: List<String>): Collection<KtDeclaration>
}

@Service(Service.Level.PROJECT)
class MyProjectService(private val project: Project) : IMyProjectService {
    private val psiShortNamesCache get() = PsiShortNamesCache.getInstance(project)

    init {
        thisLogger().info(MyBundle.message("projectService", project.name))
    }

    fun validateOneToOneMappingAgainstViewModel(hbsFile: HbPsiFile): MappingValidationResult {
        val fieldsRequiredByTemplate = hbsFile.findAllReferencedModelVariables()
        val viewModel = findCorrespondingKotlinModel(hbsFile.name) ?: error("model not found")

        return MappingValidationResult(
            fieldsInModel = viewModel.allFieldsAndProperties.mapNotNull { it.name }.toSet(),
            fieldsRequiredByTemplate = fieldsRequiredByTemplate
        )
    }

    override fun findReferenceInKotlin(ktModelName: String, hbsIdentifierParts: List<String>): Collection<KtDeclaration> {
        if (hbsIdentifierParts.isEmpty()) return emptyList()

        if (hbsIdentifierParts.size == 1) {
            val model = findCorrespondingKotlinModel(ktModelName) ?: return emptyList()
            return model.allFieldsAndProperties.filter { it.name == hbsIdentifierParts.first() }
        }

        val model = findCorrespondingKotlinModel(ktModelName) ?: return emptyList()
        val fieldInModel = model.allFieldsAndProperties.single { it.name == hbsIdentifierParts.first() }
        return findReferenceInKotlin(fieldInModel.nameOfReferencedType, hbsIdentifierParts.drop(1))
    }

    data class MappingValidationResult(
        val fieldsInModel: Set<String>,
        val fieldsRequiredByTemplate: Set<String>,
    ) {
        val fieldsMissingFromViewModel = fieldsRequiredByTemplate - fieldsInModel
    }

    private fun findCorrespondingKotlinModel(modelName: String): KtLightClassBase? {
        return psiShortNamesCache.getClassesByName(
            modelName,
            // NOTE: performance could be improved here by not using the scope of the entire project.
            GlobalSearchScope.projectScope(project)
        ).firstIsInstanceOrNull<KtLightClassBase>()
    }

    private companion object {
        fun HbPsiFile.findAllReferencedModelVariables(): Set<String> =
            PsiTreeUtil.collectElementsOfType(this, HbSimpleMustache::class.java).map { it.name }.toSet()

        val KtDeclaration.nameOfReferencedType: String
            get() = typeReference!!.descendantsOfType<KtNameReferenceExpression>().first().getReferencedName()

        val KtLightClassBase.allFieldsAndProperties: List<KtDeclaration>
            get() = allProperties + allFields.filterIsInstance<KtLightField>().mapNotNull { it.kotlinOrigin }

        val KtLightClassBase.allProperties: List<KtProperty>
            get() = allMethods
                .filterIsInstance<KtLightMethod>()
                .map { it.kotlinOrigin }
                .filterIsInstance<KtProperty>()

        val KtDeclaration.typeReference get() = when(this) {
            is KtParameter -> this.typeReference
            else -> error("unsupported type ${this::class.java}")
        }
    }
}
