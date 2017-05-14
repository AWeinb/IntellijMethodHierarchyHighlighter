package de.axp.hierarchyhighlighter;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class MethodBackgroundPainter {

	private static final HighlighterTargetArea TARGET_AREA = HighlighterTargetArea.LINES_IN_RANGE;
	private static final int COLOR_Z_INDEX = 32;

	private List<RangeHighlighter> paintJobs = new ArrayList<>();

	void paintBackgroundOf(Editor editor, PaintType paintType, Set<PsiMethod> psiMethods) {
		TextAttributes attributes = TextAttributes.fromFlyweight(paintType.getAttributesFlyweight());

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

	enum PaintType {
		PARENT_METHOD("#fff9ed"), CURRENT_METHOD("#e8fff6"), CHILD_METHOD("#dbf2ff");

		private AttributesFlyweight attributesFlyweight;

		PaintType(String color) {
			this.attributesFlyweight = AttributesFlyweight.create(null, JBColor.decode(color), 1, null, null, null);
		}

		AttributesFlyweight getAttributesFlyweight() {
			return attributesFlyweight;
		}
	}
}
