package de.axp.hierarchyhighlighter;

import java.awt.Color;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.JBColor;

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
			Set<PsiMethod> childMethods = methodFinder.findMethodsCalledBy(psiMethod);

			Optional<Set<PsiMethod>> childChildMethodsOpt = childMethods.stream().map(methodFinder::findMethodsCalledBy)
					.reduce((psiMethods, psiMethods2) -> {
						psiMethods.addAll(psiMethods2);
						return psiMethods;
					});
			Set<PsiMethod> childChildMethods = childChildMethodsOpt.orElse(Collections.emptySet());

			foldAll(editor, psiMethod);
			unfoldImportant(editor, psiMethod, childMethods, childChildMethods);
			paintBackgrounds(editor, psiMethod, childMethods, childChildMethods);
		}
	}

	private void doCleanUp(Editor editor) {
		methodBackgroundPainter.getPaintJobs().forEach(RangeMarker::dispose);
		methodBackgroundPainter.getPaintJobs().clear();
		unfoldMethodsInCurrentFile(editor);
	}

	private void unfoldMethodsInCurrentFile(Editor editor) {
		Project project = editor.getProject();
		if (project != null) {
			PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
			methodFolder.unfoldMethods(editor, methodFinder.findMethodsCalledBy(psiFile));
		}
	}

	private boolean isValid(PsiElement psiElement) {
		return psiElement instanceof PsiMethod;
	}

	private boolean isInSameFile(Editor editor, PsiElement psiElement) {
		Project project = editor.getProject();
		if (project != null) {
			PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
			return Objects.equals(psiFile, psiElement.getContainingFile());
		}
		return false;
	}

	private void foldAll(Editor editor, PsiElement psiElement) {
		methodFolder.foldMethods(editor, methodFinder.findMethodsCalledBy(psiElement.getContainingFile()));
	}

	private void unfoldImportant(Editor editor, PsiMethod psiMethod, Set<PsiMethod> childMethods,
			Set<PsiMethod> childChildMethods) {
		HashSet<PsiMethod> psiMethodsToUnfold = new HashSet<>();
		psiMethodsToUnfold.add(psiMethod);
		psiMethodsToUnfold.addAll(childMethods);
		psiMethodsToUnfold.addAll(childChildMethods);
		methodFolder.unfoldMethods(editor, psiMethodsToUnfold);
	}

	private void paintBackgrounds(Editor editor, PsiMethod psiMethod, Set<PsiMethod> childMethods,
			Set<PsiMethod> childChildMethods) {
		HashSet<PsiMethod> psiMethods = new HashSet<>();
		psiMethods.add(psiMethod);

		Color defaultBackground = editor.getColorsScheme().getDefaultBackground();
		int red = defaultBackground.getRed();
		int green = defaultBackground.getGreen();
		int blue = defaultBackground.getBlue();

		Color colorCurrentMethod = new JBColor(getColor(red - 5, green - 5, blue), getColor(red, green, blue + 10));
		Color colorChildMethods = new JBColor(getColor(red - 10, green - 10, blue), getColor(red, green, blue + 20));
		Color colorChildChildMethods = new JBColor(getColor(red - 30, green - 30, blue), getColor(red, green, blue + 30));

		methodBackgroundPainter.paintBackgroundOf(editor, psiMethods, colorCurrentMethod);
		methodBackgroundPainter.paintBackgroundOf(editor, childMethods, colorChildMethods);
		methodBackgroundPainter.paintBackgroundOf(editor, childChildMethods, colorChildChildMethods);
	}

	@NotNull
	private Color getColor(int red, int green, int blue) {
		return new Color(red % 256, green % 256, blue % 256);
	}
}
