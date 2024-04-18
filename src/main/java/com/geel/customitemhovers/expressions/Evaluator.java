package com.geel.customitemhovers.expressions;

import com.geel.customitemhovers.ItemHoverContext;
import com.geel.customitemhovers.expressions.models.BaseExpression;
import com.geel.customitemhovers.expressions.models.CompiledExpression;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.TokenType;
import com.geel.customitemhovers.functions.Function;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Stack;

/**
 * Expression evaluator.
 * <p>
 * Responsible for taking the output of the Shunting Yard Algorithm (implemented by `ExpressionCompiler`), and evaluating
 * it to produce a final numerical result.
 */
public class Evaluator {
    /**
     * Evaluates a Reverse Polish Notation queue (output from `ExpressionCompiler`), returning the result
     */
    public static Token Evaluate(CompiledExpression expression, ItemHoverContext context) {
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

                stack.add(evaluateFunction(token, stack, context));
            }

            // Handle operators
            if (token.getType().isOperator()) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Operator called with fewer than 2 items on the stack");
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

        // Single entry should be a constant integer, double, or string value
        Token result = stack.pop();
        if (!result.getType().isConstant()) {
            throw new IllegalArgumentException("Invalid token type after RPN evaluation");
        }

        // Return result
        return result;
    }

    private static Token evaluateFunction(Token functionToken, Stack<Token> stack, ItemHoverContext context) {
        String name = functionToken.getValue();
        int numArgs = functionToken.getNumFunctionArgs();

        // Ensure stack has the right amount of arguments
        if(stack.size() < numArgs) {
            throw new IllegalArgumentException("Function called with too few arguments on stack");
        }

        // Parse out the args.
        // They'll be on the stack in reverse order.
        Token[] args = new Token[numArgs];
        for(int i = args.length - 1; i >= 0; i--) {
            args[i] = stack.pop();
        }

        // Find a function which fits
        Function func = context.getFunctionProvider().findFunction(name, args);
        if(func == null) {
            return Token.fromString("[INVALID FUNCTION CALL]");
        }

        return func.execute(args, context);
    }

    /**
     * Evaluates a ternary ? operator.
     *
     * The expression `condition ? ifTrue : ifFalse` will result in a token stack of `condition ifTrue ? ifFalse :`.
     *
     * The ? operator receives `condition` and `ifTrue` as its arguments.
     *
     * If `condition` is truthy, then `ifTrue` is the result of this operator.
     * Otherwise, the TERNARY_WAS_FALSE token (?!) is the result of the operator.
     *
     * After evaluating the above ternary, the resulting stack will look like:
     * [if `condition` is true] `ifTrue ifFalse :`
     * [if `condition` is false] `?! ifFalse :`
     *
     * This sets up the `:` operator to do its job
     */
    private static Token evaluateTernaryThen(Token operator, Token condition, Token ifTrue) {
        // Ensure that the number is false in integer or double representation
        // (0.1 is not false)
        if(Math.abs(condition.getNumericValue().doubleValue()) > 0.0000001) {
            return ifTrue;
        }

        return new Token(TokenType.TERNARY_WAS_FALSE, "?!");
    }

    /**
     * Evaluates a ternary : operator.
     *
     * After evaluating the ? operator paired with this :, the resulting stack will look like:
     * [if `condition` is true] `ifTrue ifFalse :`
     * [if `condition` is false] `?! ifFalse :`
     *
     * So, to evaluate `:`, we just see if the lefthand argument is a `?!`.
     * If it is, then we return `ifFalse`.
     * Otherwise, we return the lefthand argument.
     */
    private static Token evaluateTernaryElse(Token ifFalse, Token discriminator) {
        if(discriminator.getType() == TokenType.TERNARY_WAS_FALSE) {
            return ifFalse;
        }

        return discriminator;
    }

    private static Token evaluateOperator(Token operatorToken, Token leftValue, Token rightValue) {
        // Handle ternary operators specially from the other operators. Fuck the other operators.
        if(operatorToken.getType() == TokenType.TERNARY_THEN) {
            return evaluateTernaryThen(operatorToken, leftValue, rightValue);
        } else if(operatorToken.getType() == TokenType.TERNARY_ELSE) {
            return evaluateTernaryElse(rightValue, leftValue);
        }

        // If either operator is a string, handle it specially
        if(leftValue.getType() == TokenType.STRING || rightValue.getType() == TokenType.STRING) {
            return evaluateStringOperator(operatorToken, leftValue, rightValue);
        }

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
            case BIT_AND:
            case BIT_OR:
            case BIT_XOR:
            case BITSHIFT_LEFT:
            case BITSHIFT_RIGHT:
                throw new NotImplementedException();
            default:
                throw new IllegalStateException("Unexpected value: " + operatorToken.getType());
        }

        return isDoubleOutput
                ? Token.fromDouble(result.doubleValue())
                : Token.fromInt(result.intValue());
    }

    private static Token evaluateStringOperator(Token operatorToken, Token leftValue, Token rightValue) {
        // Operator must be valid for use on strings
        if(!operatorToken.getType().operatorSupportsStringArguments()) {
            throw new IllegalArgumentException("Operator " + operatorToken.getValue() + " does not support strings");
        }

        // Both arguments must be strings
        if(leftValue.getType() != rightValue.getType() || leftValue.getType() != TokenType.STRING) {
            throw new IllegalArgumentException("Both arguments must be string values");
        }

        switch(operatorToken.getType()) {
            case CMP_EQUALS:
                return Token.fromInt(leftValue.getValue().equals(rightValue.getValue()) ? 1 : 0);
            case CMP_NEQUALS:
                return Token.fromInt(leftValue.getValue().equals(rightValue.getValue()) ? 0 : 1);
            case OP_PLUS:
                return Token.fromString(leftValue.getValue() + rightValue.getValue());
            default:
                throw new IllegalArgumentException("Unknown string operator " + operatorToken.getValue());
        }
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
            return Token.fromInt(intVal);
        } else if (isDouble) {
            return Token.fromDouble(doubleVal);
        } else {
            return Token.fromString(variableVal);
        }
    }
}
