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
		doCleanUp(editor);

		if (isValid(psiElement) && isInSameFile(editor, psiElement)) {
			PsiMethod psiMethod = (PsiMethod) psiElement;
			Set<PsiMethod> parentMethods = methodFinder.findMethodCallsOf(psiMethod);
			Set<PsiMethod> childMethods = methodFinder.findMethodsCalledBy(psiMethod);

			foldAll(editor, psiMethod);
			unfoldImportant(editor, psiMethod, parentMethods, childMethods);
			paintBackgrounds(editor, psiMethod, parentMethods, childMethods);
		}
	}

	private void doCleanUp(Editor editor) {
		methodBackgroundPainter.getPaintJobs().forEach(RangeMarker::dispose);
		methodBackgroundPainter.getPaintJobs().clear();
		unfoldMethodsInCurrentFile(editor);
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

	private void unfoldMethodsInCurrentFile(Editor editor) {
		Project project = editor.getProject();
		if (project != null) {
			PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
			methodFolder.unfoldMethods(editor, methodFinder.findMethodsCalledBy(psiFile));
		}
	}

	private void foldAll(Editor editor, PsiElement psiElement) {
		methodFolder.foldMethods(editor, methodFinder.findMethodsCalledBy(psiElement.getContainingFile()));
	}

	private void unfoldImportant(Editor editor, PsiMethod psiMethod, Set<PsiMethod> parentMethods, Set<PsiMethod> childMethods) {
		HashSet<PsiMethod> psiMethodsToUnfold = new HashSet<>();
		psiMethodsToUnfold.add(psiMethod);
		psiMethodsToUnfold.addAll(parentMethods);
		psiMethodsToUnfold.addAll(childMethods);
		methodFolder.unfoldMethods(editor, psiMethodsToUnfold);
	}

	private void paintBackgrounds(Editor editor, PsiMethod psiMethod, Set<PsiMethod> parentMethods, Set<PsiMethod> childMethods) {
		methodBackgroundPainter.paintBackgroundOf(editor, PaintType.CURRENT_METHOD, Sets.newHashSet(psiMethod));
		methodBackgroundPainter.paintBackgroundOf(editor, PaintType.PARENT_METHOD, parentMethods);
		methodBackgroundPainter.paintBackgroundOf(editor, PaintType.CHILD_METHOD, childMethods);
	}
}
