package com.geel.customitemhovers.expressions;

import com.geel.customitemhovers.ItemHoverContext;
import java.util.Stack;

/**
 * Expression evaluator.
 * <p>
 * Responsible for taking the output of the Shunting Yard Algorithm (implemented by `ShuntingYard`), and evaluating
 * it to produce a final numerical result.
 */
public class ExpressionEvaluator {
    /**
     * Evaluates a Reverse Polish Notation queue (output from `ShuntingYard`), returning the result
     */
    public static Number Evaluate(Expression expression, ItemHoverContext context) {
        Token[] rpn = expression.getTokens();
        Stack<Token> stack = new Stack<>();
        for (int i = 0; i < rpn.length; i++) {
            boolean isFirst = i == 0;
            boolean isLast = i == rpn.length - 1;
            Token token = rpn[i];

            // Handle values
            if (token.getType().isValue()) {
                if (isLast && !isFirst) {
                    throw new IllegalArgumentException("Dangling value");
                }
                stack.add(evaluateValue(token, context));
            }

            // Handle functions
            if (token.getType().isFunction()) {
                if (stack.isEmpty()) {
                    throw new IllegalArgumentException("Function called with empty stack");
                }

                Token popped = stack.pop();
                stack.add(evaluateFunction(token, popped, context));
            }

            // Handle operators
            if (token.getType().isOperator()) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Operator called with fewer than 2 items in the stack");
                }

                Token rightHand = stack.pop();
                Token leftHand = stack.pop();
                stack.add(evaluateOperator(token, leftHand, rightHand));
            }
        }

        // Stack should have one entry: the result
        if (stack.size() != 1) {
            throw new IllegalArgumentException("Stack should only have a single entry at the end");
        }

        // Single entry should be a constant integer value
        Token result = stack.pop();
        if (!result.getType().isConstant()) {
            throw new IllegalArgumentException("Invalid token type after RPN evaluation");
        }

        // Return result
        return result.getConstantValue();
    }

    private static Token evaluateFunction(Token functionToken, Token argToken, ItemHoverContext context) {
        Number argValue = argToken.getConstantValue();
        switch (functionToken.getValue()) {
            case "floor":
                return new Token(TokenType.INTEGER, String.valueOf((int) Math.floor(argValue.doubleValue())));
            case "ceil":
                return new Token(TokenType.INTEGER, String.valueOf((int) Math.ceil(argValue.doubleValue())));

            // These three functions are here for backwards-compatibility with the old, garbage, function system
            case "qtymult":
                return new Token(TokenType.INTEGER, String.valueOf(context.getItem().getQuantity() * argValue.intValue()));
            case "qtydivide":
                return new Token(TokenType.INTEGER, String.valueOf(context.getItem().getQuantity() / argValue.intValue()));
            case "qtydivideceil":
                return new Token(TokenType.INTEGER, String.valueOf(Math.ceil((double) context.getItem().getQuantity() / (double) argValue.intValue())));
            default:
                return argToken;
        }
    }

    private static Token evaluateOperator(Token operatorToken, Token leftValue, Token rightValue) {
        Number leftVal, rightVal, result;

        // TODO: Figure out a better way to handle this. Fundamentally, we are trying to figure out:
        // 1. How do we deserialize the input?
        // 2. How do we serialize the output?
        boolean isDoubleInput = isDoubleOperation(operatorToken, leftValue, rightValue);
        boolean isDoubleOutput = isDoubleInput && !operatorToken.getType().isBooleanOperator();

        // I hate this
        if (isDoubleInput) {
            leftVal = Double.parseDouble(leftValue.getValue());
            rightVal = Double.parseDouble(rightValue.getValue());
        } else {
            leftVal = Integer.parseInt(leftValue.getValue());
            rightVal = Integer.parseInt(rightValue.getValue());
        }

        // TODO: Surely this can be done more elegantly
        switch (operatorToken.getType()) {
            case OP_TIMES:
                result = isDoubleInput
                        ? (leftVal.doubleValue() * rightVal.doubleValue())
                        : (leftVal.intValue() * rightVal.intValue());
                break;
            case OP_DIVIDE:
                // Avoid a divide-by-zero
                if (Math.abs(rightVal.doubleValue()) < 0.0000001) {
                    result = 0;
                } else {
                    result = leftVal.doubleValue() / rightVal.doubleValue();
                }
                break;
            case OP_MODULO:
                // Avoid a divide-by-zero
                if (rightVal.intValue() == 0) {
                    result = 0;
                } else {
                    result = leftVal.intValue() % rightVal.intValue();
                }
                break;
            case OP_PLUS:
                result = isDoubleInput
                        ? (leftVal.doubleValue() + rightVal.doubleValue())
                        : (leftVal.intValue() + rightVal.intValue());
                break;
            case OP_MINUS:
                result = isDoubleInput
                        ? (leftVal.doubleValue() - rightVal.doubleValue())
                        : (leftVal.intValue() - rightVal.intValue());
                break;
            case CMP_LT:
                result = leftVal.doubleValue() < rightVal.doubleValue() ? 1 : 0; // EPSILON
                break;
            case CMP_LTE:
                result = (leftVal.doubleValue() - rightVal.doubleValue() < 0.000001) ? 1 : 0; // EPSILON
                break;
            case CMP_GT:
                result = leftVal.doubleValue() > rightVal.doubleValue() ? 1 : 0; // EPSILON
                break;
            case CMP_GTE:
                result = (leftVal.doubleValue() - rightVal.doubleValue() > -0.000001) ? 1 : 0; // EPSILON
                break;
            case CMP_EQUALS:
                result = Math.abs(leftVal.doubleValue() - rightVal.doubleValue()) < 0.000001 ? 1 : 0; // EPSILON
                break;
            case CMP_NEQUALS:
                result = Math.abs(leftVal.doubleValue() - rightVal.doubleValue()) > 0.000001 ? 1 : 0; // EPSILON
                break;
            case LOG_AND:
                result = (leftVal.intValue() != 0 && rightVal.intValue() != 0) ? 1 : 0;
                break;
            case LOG_OR:
                result = (leftVal.intValue() != 0 || rightVal.intValue() != 0) ? 1 : 0;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + operatorToken.getType());
        }

        return isDoubleOutput
                ? Token.fromDouble(result.doubleValue())
                : Token.fromInt(result.intValue());
    }

    /**
     * Determines if an operation requires the use of decimal numbers.
     * <p>
     * This is true if either operand is a decimal itself, or if the operation is a division.
     */
    private static boolean isDoubleOperation(Token operatorToken, Token leftValue, Token rightValue) {
        // If a double is involved on either side, we get a double.
        if (leftValue.getType() == TokenType.DOUBLE || rightValue.getType() == TokenType.DOUBLE) {
            return true;
        }

        // Otherwise, division always results in doubles
        return operatorToken.getType() == TokenType.OP_DIVIDE;
    }

    /**
     * Evaluates a Token to a constant numeric value.
     * <p>
     * If the Token is already a constant number, this just returns the Token.
     * <p>
     * Otherwise, the Token is a variable; the variable is evaluated and, if it's a number, its value is returned.
     */
    private static Token evaluateValue(Token value, ItemHoverContext context) {
        if (value.getType().isConstant()) {
            return value;
        }

        // Evaluate variable
        String variableVal = context.resolveVariable(value.getValue());
        if (variableVal == null) {
            throw new IllegalArgumentException("Unknown variable " + value.getValue());
        }

        // Determine if variable is integer or double
        // TODO: Holy _wow_ this is fucking stupid
        int intVal = 0;
        double doubleVal = 0;
        boolean isInt = false;
        boolean isDouble = false;

        try {
            intVal = Integer.parseInt(variableVal);
            isInt = true;
        } catch (NumberFormatException e) {
        }

        try {
            doubleVal = Double.parseDouble(variableVal);
            isDouble = true;
        } catch (NumberFormatException e) {
        }

        if (isInt) {
            return new Token(TokenType.INTEGER, String.valueOf(intVal));
        } else if (isDouble) {
            return new Token(TokenType.DOUBLE, String.valueOf(doubleVal));
        } else {
            throw new IllegalArgumentException("Neither int nor double");
        }
    }
}
