package de.axp.hierarchyhighlighter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.AttributesFlyweight;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiMethod;

class MethodBackgroundPainter {

	private static final HighlighterTargetArea TARGET_AREA = HighlighterTargetArea.LINES_IN_RANGE;
	private static final int COLOR_Z_INDEX = 32;

	private List<RangeHighlighter> paintJobs = new ArrayList<>();

	void paintBackgroundOf(Editor editor, Set<PsiMethod> psiMethods, Color color) {
		AttributesFlyweight attributesFlyweight = AttributesFlyweight.create(null, color, 1, null, null, null);
		TextAttributes attributes = TextAttributes.fromFlyweight(attributesFlyweight);

		for (PsiMethod psiMethod : psiMethods) {
			RangeHighlighter highlighter = applyHighlight(editor, attributes, psiMethod);
			paintJobs.add(highlighter);
		}
	}

	@NotNull
	private RangeHighlighter applyHighlight(Editor editor, TextAttributes attributes, PsiMethod psiMethod) {
		TextRange textRange = psiMethod.getTextRange();
		int start = textRange.getStartOffset();
		int end = textRange.getEndOffset();
		MarkupModel markupModel = editor.getMarkupModel();
		return markupModel.addRangeHighlighter(start, end, COLOR_Z_INDEX, attributes, TARGET_AREA);
	}

	List<RangeHighlighter> getPaintJobs() {
		return paintJobs;
	}
}
