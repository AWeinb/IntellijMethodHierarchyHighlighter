package de.axp.hierarchyhighlighter;

import com.google.common.collect.Sets;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import de.axp.hierarchyhighlighter.MethodBackgroundPainter.PaintType;

import java.util.HashSet;
import java.util.Objects;
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

		if (isValid(psiElement) && isInSameFile(editor, psiElement)) {
			PsiMethod psiMethod = (PsiMethod) psiElement;
			Set<PsiMethod> parentMethods = methodFinder.findMethodCallsOf(psiMethod);
			Set<PsiMethod> childMethods = methodFinder.findAllChildMethodsOf(psiMethod);

			foldMethodsInFile(editor, psiMethod);
			unfoldImportantMethods(editor, psiMethod, parentMethods, childMethods);
			paintBackgroundOfImportantMethods(editor, psiMethod, parentMethods, childMethods);
		}
	}

	private boolean isValid(PsiElement psiElement) {
		return psiElement != null && psiElement instanceof PsiMethod;
	}

	private boolean isInSameFile(Editor editor, PsiElement psiElement) {
		Project project = editor.getProject();
		if (project != null) {
			PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
			return Objects.equals(psiFile, psiElement.getContainingFile());
		}
		return false;
	}

	private void foldMethodsInFile(Editor editor, PsiMethod psiMethod) {
		methodFolder.foldMethods(editor, methodFinder.findAllChildMethodsOf(psiMethod.getContainingClass()));
	}

	private void unfoldImportantMethods(Editor editor, PsiMethod psiMethod, Set<PsiMethod> parentMethods, Set<PsiMethod> childMethods) {
		HashSet<PsiMethod> psiMethodsToUnfold = new HashSet<>();
		psiMethodsToUnfold.add(psiMethod);
		psiMethodsToUnfold.addAll(parentMethods);
		psiMethodsToUnfold.addAll(childMethods);
		methodFolder.unfoldMethod(editor, psiMethodsToUnfold);
	}

	private void paintBackgroundOfImportantMethods(Editor editor, PsiMethod psiMethod, Set<PsiMethod> parentMethods, Set<PsiMethod> childMethods) {
		methodBackgroundPainter.paintBackgroundOf(editor, PaintType.CURRENT_METHOD, Sets.newHashSet(psiMethod));
		methodBackgroundPainter.paintBackgroundOf(editor, PaintType.PARENT_METHOD, parentMethods);
		methodBackgroundPainter.paintBackgroundOf(editor, PaintType.CHILD_METHOD, childMethods);
	}
}
