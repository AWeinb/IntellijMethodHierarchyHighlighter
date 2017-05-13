package de.axp.hierarchyhighlighter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;

public class MethodHighlightingAction extends AnAction {

	private MethodHighlighter methodHighlighter = new MethodHighlighter();

	@Override
	public void actionPerformed(AnActionEvent e) {
		Editor editor = e.getData(CommonDataKeys.EDITOR);
		PsiElement psiElementUnderCursor = e.getData(CommonDataKeys.PSI_ELEMENT);

		methodHighlighter.highlight(editor, psiElementUnderCursor);
	}
}
