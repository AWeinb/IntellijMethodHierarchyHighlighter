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
		methodBackgroundPainter.getPaintJobs().clear();

		if (isValid(psiElement)) {
			PsiMethod psiMethod = (PsiMethod) psiElement;

			Set<PsiMethod> parentMethods = methodFinder.findAllParentMethodsOf(psiMethod);
			Set<PsiMethod> childMethods = methodFinder.findAllChildMethodsOf(psiMethod);

			methodFolder.foldMethods(editor);

			methodFolder.unfoldMethod(editor, psiMethod);
			parentMethods.forEach(m -> methodFolder.unfoldMethod(editor, m));
			childMethods.forEach(m -> methodFolder.unfoldMethod(editor, m));

			paintBackgroundOfImportantMethods(editor, psiMethod, parentMethods, childMethods);
		}
	}

	private boolean isValid(PsiElement psiElement) {
		return psiElement != null && psiElement instanceof PsiMethod;
	}

	private void paintBackgroundOfImportantMethods(Editor editor, PsiMethod psiMethod, Set<PsiMethod> parentMethods, Set<PsiMethod> childMethods) {
		methodBackgroundPainter.paintBackgroundOf(editor, PaintType.CURRENT_METHOD, Sets.newHashSet(psiMethod));
		methodBackgroundPainter.paintBackgroundOf(editor, PaintType.PARENT_METHOD, parentMethods);
		methodBackgroundPainter.paintBackgroundOf(editor, PaintType.CHILD_METHOD, childMethods);
	}
}
