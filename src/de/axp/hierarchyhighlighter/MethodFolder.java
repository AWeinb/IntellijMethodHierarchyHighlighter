package de.axp.hierarchyhighlighter;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiMethod;

import java.util.Optional;
import java.util.Set;

class MethodFolder {

	void foldMethods(Editor editor, Set<PsiMethod> psiMethods) {
		editor.getFoldingModel().runBatchFoldingOperation(() -> setMethodsExpanded(editor, psiMethods, false));
	}

	void unfoldMethod(Editor editor, Set<PsiMethod> psiMethods) {
		editor.getFoldingModel().runBatchFoldingOperation(() -> setMethodsExpanded(editor, psiMethods, true));
	}

	private void setMethodsExpanded(Editor editor, Set<PsiMethod> psiMethods, boolean expanded) {
		for (PsiMethod psiMethod : psiMethods) {
			Optional<FoldRegion> foldRegionOptional = getFoldRegionOfMethod(editor, psiMethod);
			foldRegionOptional.ifPresent(f -> f.setExpanded(expanded));
		}
	}

	private Optional<FoldRegion> getFoldRegionOfMethod(Editor editor, PsiMethod psiMethod) {
		TextRange textRange = psiMethod.getTextRange();
		for (FoldRegion foldRegion : editor.getFoldingModel().getAllFoldRegions()) {
			if (textRange.getEndOffset() == foldRegion.getEndOffset()) {
				return Optional.of(foldRegion);
			}
		}
		return Optional.empty();
	}
}
