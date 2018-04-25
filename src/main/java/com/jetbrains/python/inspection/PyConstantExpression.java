package com.jetbrains.python.inspection;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
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
            } else {
                int answer = type_definition(condition);
                if (answer == 0) {
                    registerProblem(condition, "The condition is always false");
                } else {
                    registerProblem(condition, "The condition is always true ");
                }
            }
        }

        private int type_definition(PsiElement el) {
            int answer;
            if (el instanceof PyBinaryExpression) {
                answer = descent(el);
            } else if (el instanceof PyParenthesizedExpression) {
                answer = type_definition(el.getFirstChild().getNextSibling());
            } else if (el instanceof PyPrefixExpression) {
                answer = type_definition(el.getFirstChild().getNextSibling());
                if (answer == 1) {
                    answer = 0;
                } else {
                    answer = 1;
                }
            } else {
                answer = Integer.parseInt(el.getText());
            }
            return answer;
        }

        private int descent(PsiElement el) {
            int leftChild, rightChild;
            String operator;
            leftChild = type_definition(el.getFirstChild());
            rightChild = type_definition(el.getLastChild());
            operator = ((PyBinaryExpression) el).getPsiOperator().getText();
            if (operator.equals("==")) {
                if (leftChild == rightChild) {
                    return 1;
                } else {
                    return 0;
                }
            }
            if (operator.equals("!=")) {
                if (leftChild != rightChild) {
                    return 1;
                } else {
                    return 0;
                }
            }
            if (operator.equals("<")) {
                if (leftChild < rightChild) {
                    return 1;
                } else {
                    return 0;
                }
            }
            if (operator.equals(">")) {
                if (leftChild > rightChild) {
                    return 1;
                } else {
                    return 0;
                }
            }
            if (operator.equals("+")) {
                return leftChild + rightChild;
            }
            if (operator.equals("-")) {
                return leftChild - rightChild;
            }
            if (operator.equals("*")) {
                return leftChild * rightChild;
            }
            if (operator.equals("/")) {
                return leftChild / rightChild;
            }
            if (operator.equals("and")) {
                if (leftChild == 1 && rightChild == 1) {
                    return 1;
                } else {
                    return 0;
                }
            }
            if (operator.equals("or")) {
                if (leftChild == 1 || rightChild == 1) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return 0;
        }
    }
}
