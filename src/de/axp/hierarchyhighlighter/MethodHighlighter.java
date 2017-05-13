package de.axp.hierarchyhighlighter;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import de.axp.hierarchyhighlighter.MethodBackgroundPainter.PaintType;

import java.util.Set;

class MethodHighlighter {

	private final MethodFinder methodFinder;
	private final MethodFolder methodFolder;
	private final MethodBackgroundPainter methodBackgroundPainter;

	MethodHighlighter() {
		methodFinder = new MethodFinder();
		methodFolder = new MethodFolder();
		methodBackgroundPainter = new MethodBackgroundPainter();
	}

	void highlight(Editor editor, PsiElement psiElement) {
		methodBackgroundPainter.getPaintJobs().forEach(RangeMarker::dispose);

		if (isValid(psiElement)) {
			PsiMethod psiMethod = (PsiMethod) psiElement;

			Set<PsiMethod> parentMethods = methodFinder.findAllParentMethodsOf(psiMethod);
			Set<PsiMethod> childMethods = methodFinder.findAllChildMethodsOf(psiMethod);

			methodFolder.foldMethods(editor);

			methodFolder.unfoldMethod(editor, psiMethod);
			parentMethods.forEach(m -> methodFolder.unfoldMethod(editor, m));
			childMethods.forEach(m -> methodFolder.unfoldMethod(editor, m));

			methodBackgroundPainter.paintBackgroundOf(editor, psiMethod, PaintType.CURRENT_METHOD);
			parentMethods.forEach(m -> methodBackgroundPainter.paintBackgroundOf(editor, m, PaintType.PARENT_METHOD));
			childMethods.forEach(m -> methodBackgroundPainter.paintBackgroundOf(editor, m, PaintType.CHILD_METHOD));
		}
	}

	private boolean isValid(PsiElement psiElement) {
		return psiElement != null && psiElement instanceof PsiMethod;
	}
}
