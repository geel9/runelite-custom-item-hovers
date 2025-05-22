package com.geel.customitemhovers.expressions;

import com.geel.customitemhovers.expressions.models.CompiledExpression;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.TokenType;
import com.geel.customitemhovers.expressions.functions.Function;

import java.util.Stack;

/**
 * Evaluates compiled expressions, returning a single Token (if runtime evaluation) or multiple Tokens (if static evaluation).
 *
 * If a single Token with a constant value is returned, it is the output of the expression.
 *
 * Supports both runtime eval mode (must get an answer now or exception)
 * and static eval mode (evaluate as much as possible now, but leave any statically-unresolvable expressions or
 * functions alone). Static eval mode is useful for reducing expressions to their minimal form, improving runtime performance.
 */
class Evaluator {
    public static Token[] EvaluateExpression(CompiledExpression expression,
                                             ExecutionContext context,
                                             boolean staticEval) {
        Token[] rpn = expression.getTokens();
        Stack<Token> stack = new Stack<>();
        for (int i = 0; i < rpn.length; i++) {
            Token token = rpn[i];
            boolean isFirst = i == 0;
            boolean isLast = i == rpn.length - 1;

            // Handle values
            if (token.getType().isValue()) {
                if (isLast && !isFirst) {
                    throw new IllegalArgumentException("Dangling value");
                }

                stack.add(evaluateValue(token, context, staticEval));
                continue;
            }

            // Handle functions
            if (token.getType().isFunction()) {
                if (stack.isEmpty() && token.getNumFunctionArgs() != 0) {
                    throw new IllegalArgumentException("Function called with empty stack");
                }

                stack.add(evaluateFunction(stack, token, context, staticEval));
                continue;
            }

            // Handle operators
            if (token.getType().isOperator()) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Operator called with fewer than 2 items on the stack");
                }

                Token rightHand = stack.pop();
                Token leftHand = stack.pop();
                stack.add(evaluateOperator(stack, token, leftHand, rightHand, staticEval));
                continue;
            }
        }

        return stack.toArray(new Token[0]);
    }

    /**
     * Evaluates a Token to a constant numeric value.
     * <p>
     * If the Token is already a constant number, this just returns the Token.
     * <p>
     * Otherwise, the Token is a variable; evaluate the variable.
     */
    private static Token evaluateValue(
            Token value,
            ExecutionContext context,
            boolean staticEval
    ) {
        // If it's a constant type, just return that
        if (value.getType().isConstant()) {
            return value;
        }

        // Evaluate variable
        String variableVal = context.resolveVariable(value.getValue());
        if (variableVal == null) {
            // If we can't evaluate the variable, and we're in static eval mode, just return the variable token
            if (staticEval) {
                return value;
            }

            // Can't evaluate the variable and we're in runtime eval mode -- scream
            throw new IllegalArgumentException("Cannot resolve variable");
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

    /**
     * Evaluates a function call.
     *
     * If in static eval mode, any non-const arguments, or a call to a non-constexpr function, will result in
     * the function call being unchanged on the stack.
     *
     * If in runtime eval mode, any non-const arguments will result in an exception being thrown.
     */
    private static Token evaluateFunction(
            Stack<Token> stack,
            Token functionToken,
            ExecutionContext context,
            boolean staticEval) {
        String name = functionToken.getValue();
        int numArgs = functionToken.getNumFunctionArgs();

        // Ensure stack has the right amount of arguments
        if (stack.size() < numArgs) {
            throw new IllegalArgumentException("Function called with too few arguments on stack");
        }

        // Parse out the args.
        // They'll be on the stack in reverse order.
        Token[] args = new Token[numArgs];
        boolean allConst = true;
        int numArgsPopped = 0;
        for (int i = args.length - 1; i >= 0; i--) {
            args[i] = stack.pop();
            numArgsPopped++;

            // If any args are non-const, we should stop immediately -- we won't be able to evaluate the function.
            // (We're either in static eval mode, in which case, any non-const args are always a no go -- or we're in
            //  runtime eval mode, in which case, it's inherently erroneous that we end up with a function call
            //  without evaluated, constant arguments on the stack)
            if (!args[i].getType().isConstant()) {
                allConst = false;
                break;
            }
        }

        // If all the arguments are const, then try to find a function that fits
        if (allConst) {
            Function func = context.resolveFunction(name, args);
            if (func != null && (!staticEval || func.isConstExpr())) {
                // We found a function, and either we're not in static eval mode, or we are and it's a constexpr func
                return func.execute(args, context);
            }
        }

        // If nothing fits, and we're not in static eval mode, then we should just fail
        if (!staticEval) {
            throw new IllegalArgumentException("Invalid function call");
        }

        // We're in static eval mode and no function call fit (or it wasn't constexpr).
        // We need to put the stack back to where it was.
        //
        // Put all the args back on the stack
        for (int i = args.length - 1; i >= (args.length - numArgsPopped); i--) {
            stack.add(args[i]);
        }

        // Put the function back on the stack
        return functionToken;
    }

    /**
     * Evaluates an operator on the stack.
     *
     * If in static eval mode, operations involving anything non-const (unresolved variable, function call, etc),
     * or involving ternary, will result in the operation being put back on the stack, untouched.
     */
    private static Token evaluateOperator(
            Stack<Token> stack,
            Token operatorToken,
            Token leftValue,
            Token rightValue,
            boolean staticEvalMode
    ) {
        // In static eval mode, handle non-const operands or ternary operators specially
        boolean isTernary = operatorToken.getType().isTernaryOperator();
        if (staticEvalMode) {
            boolean isNonConst = (!leftValue.getType().isConstant() || !rightValue.getType().isConstant());

            if(isNonConst || isTernary) {
                // Put things back on the stack exactly as they were
                stack.add(leftValue);
                stack.add(rightValue);
                return operatorToken;
            }
        }

        // In runtime eval mode, handle ternary operators specially from the other operators. Fuck the other operators.
        if (operatorToken.getType() == TokenType.TERNARY_THEN) {
            return evaluateTernaryThen(operatorToken, leftValue, rightValue);
        } else if (operatorToken.getType() == TokenType.TERNARY_ELSE) {
            return evaluateTernaryElse(rightValue, leftValue);
        }

        // If either operator is a string, handle it specially
        if (leftValue.getType() == TokenType.STRING || rightValue.getType() == TokenType.STRING) {
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
                throw new IllegalStateException("Bitwise operations are not presently supported");
            default:
                throw new IllegalStateException("Unexpected value: " + operatorToken.getType());
        }

        return isDoubleOutput
                ? Token.fromDouble(result.doubleValue())
                : Token.fromInt(result.intValue());
    }

    /**
     * Evaluates an operation involving string operands
     */
    private static Token evaluateStringOperator(Token operatorToken, Token leftValue, Token rightValue) {
        // Operator must be valid for use on strings
        if (!operatorToken.getType().operatorSupportsStringArguments()) {
            throw new IllegalArgumentException("Operator " + operatorToken.getValue() + " does not support strings");
        }

        // At least one argument must be a string
        if (leftValue.getType() != TokenType.STRING && rightValue.getType() != TokenType.STRING) {
            throw new IllegalArgumentException("Both arguments must be string values");
        }

        switch (operatorToken.getType()) {
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
}
