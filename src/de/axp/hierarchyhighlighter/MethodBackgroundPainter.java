package de.axp.hierarchyhighlighter;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.JBColor;

import java.util.ArrayList;
import java.util.List;

class MethodBackgroundPainter {

	private static final HighlighterTargetArea TARGET_AREA = HighlighterTargetArea.LINES_IN_RANGE;
	private static final int COLOR_Z_INDEX = 32;

	private List<RangeHighlighter> paintJobs = new ArrayList<>();

	void paintBackgroundOf(Editor editor, PsiMethod psiMethod, PaintType paintType) {
		TextRange textRange = psiMethod.getTextRange();
		int start = textRange.getStartOffset();
		int end = textRange.getEndOffset();
		TextAttributes attributes = TextAttributes.fromFlyweight(paintType.getAttributesFlyweight());

		MarkupModel markupModel = editor.getMarkupModel();
		RangeHighlighter highlighter = markupModel.addRangeHighlighter(start, end, COLOR_Z_INDEX, attributes, TARGET_AREA);

		paintJobs.add(highlighter);
	}

	List<RangeHighlighter> getPaintJobs() {
		return paintJobs;
	}

	enum PaintType {
		PARENT_METHOD("#fff9ed"), CURRENT_METHOD("#fafff9"), CHILD_METHOD("#edfcff");

		private AttributesFlyweight attributesFlyweight;

		PaintType(String color) {
			this.attributesFlyweight = AttributesFlyweight.create(null, JBColor.decode(color), 1, null, null, null);
		}

		AttributesFlyweight getAttributesFlyweight() {
			return attributesFlyweight;
		}
	}
}
