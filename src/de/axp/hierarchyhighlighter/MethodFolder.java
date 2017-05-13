package de.axp.hierarchyhighlighter;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiMethod;

import java.util.Optional;

class MethodFolder {

	void foldMethods(Editor editor) {
		editor.getFoldingModel().runBatchFoldingOperation(() -> {
			for (FoldRegion foldRegion : editor.getFoldingModel().getAllFoldRegions()) {
				foldRegion.setExpanded(false);
			}
		});
	}

	void unfoldMethod(Editor editor, PsiMethod psiMethod) {
		editor.getFoldingModel().runBatchFoldingOperation(() -> {
			Optional<FoldRegion> foldRegionOptional = getFoldRegionOfMethod(editor, psiMethod);
			foldRegionOptional.ifPresent(f -> f.setExpanded(true));
		});
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
