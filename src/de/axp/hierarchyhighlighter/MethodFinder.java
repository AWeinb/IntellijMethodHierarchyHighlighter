package de.axp.hierarchyhighlighter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;

class MethodFinder {

	Set<PsiMethod> findMethodsCalledBy(PsiElement psiMethod) {
		List<PsiMethod> children = new ArrayList<>();
		MethodCollector.recurseAndCollect(psiMethod, e -> children.add(e.resolveMethod()));
		List<PsiMethod> childrenInClass = filterOutForeignMethods(children, psiMethod.getContainingFile());
		return new HashSet<>(childrenInClass);
	}

	private List<PsiMethod> filterOutForeignMethods(List<PsiMethod> psiMethods, PsiFile psiFile) {
		Stream<PsiMethod> psiMethodStream = psiMethods.stream()
				.filter(m -> Objects.equals(m.getContainingFile(), psiFile));
		return psiMethodStream.collect(Collectors.toList());
	}

	Set<PsiMethod> findMethodCallsOf(PsiMethod childMethod) {
		List<PsiMethod> parents = new ArrayList<>();
		MethodCollector.recurseAndCollect(Objects.requireNonNull(childMethod.getContainingClass()), callExpression -> {
			if (isCallOfMethod(callExpression, childMethod)) {
				findWhereCallHappened(callExpression).ifPresent(parents::add);
			}
		});
		return new HashSet<>(parents);
	}

	private boolean isCallOfMethod(PsiMethodCallExpression callExpression, PsiMethod childMethod) {
		return childMethod.equals(callExpression.resolveMethod());
	}

	private Optional<PsiMethod> findWhereCallHappened(PsiMethodCallExpression expression) {
		PsiElement caller = traversUpToMethodSignature(expression);
		if (caller != null) {
			return Optional.of((PsiMethod) caller);
		}
		return Optional.empty();
	}

	@Nullable
	private PsiElement traversUpToMethodSignature(PsiMethodCallExpression e) {
		PsiElement context = e.getContext();
		while (context != null && !(context instanceof PsiMethod)) {
			context = context.getContext();
		}
		return context;
	}

	private interface MethodCollector {

		static void recurseAndCollect(PsiElement psiElement, MethodCollector methodCollector) {
			JavaRecursiveElementVisitor recursiveElementVisitor = new JavaRecursiveElementVisitor() {

				@Override
				public void visitMethodCallExpression(PsiMethodCallExpression expression) {
					methodCollector.collect(expression);
					super.visitMethodCallExpression(expression);
				}
			};
			psiElement.accept(recursiveElementVisitor);
		}

		void collect(PsiMethodCallExpression expression);
	}
}
