package com.jetbrains.python.inspection;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.python.inspections.PyInspection;
import com.jetbrains.python.inspections.PyInspectionVisitor;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PyConstantExpression extends PyInspection {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly,
                                          @NotNull LocalInspectionToolSession session) {
        return new Visitor(holder, session);
    }

    private static class Visitor extends PyInspectionVisitor {

        private Visitor(@Nullable ProblemsHolder holder, @NotNull LocalInspectionToolSession session) {
            super(holder, session);
        }

        @Override
        public void visitPyIfStatement(PyIfStatement node) {
            super.visitPyIfStatement(node);
            processIfPart(node.getIfPart());
            for (PyIfPart part : node.getElifParts()) {
                processIfPart(part);
            }
        }

        private void processIfPart(@NotNull PyIfPart pyIfPart) {
            final PyExpression condition = pyIfPart.getCondition();
            if (condition instanceof PyBoolLiteralExpression) {
                registerProblem(condition, "The condition is always " + ((PyBoolLiteralExpression) condition).getValue());
            }
            if (condition instanceof PyBinaryExpression) {
                int firstInt = Integer.parseInt(condition.getFirstChild().getText());
                int secondInt = Integer.parseInt(condition.getLastChild().getText());
                String operator = ((PyBinaryExpression) condition).getPsiOperator().getText();
                if (operator == "==") {
                    registerProblem(condition, "The condition is always " + (firstInt == secondInt));
                }
                if (operator == "!=") {
                    registerProblem(condition, "The condition is always " + (firstInt != secondInt));
                }
                if (operator == "<") {
                    registerProblem(condition, "The condition is always " + (firstInt < secondInt));
                }
                if (operator == ">") {
                    registerProblem(condition, "The condition is always " + (firstInt > secondInt));
                }
            }
        }
    }
}
