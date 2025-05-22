package com.geel.customitemhovers.expressions;
import com.geel.customitemhovers.expressions.models.*;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Performs the Shunting Yard Algorithm to convert an infix mathematical expression to Reverse Polish Notation (postfix).
 *
 * This is here so that we can enable rich mathematical expressions, including variables and functions, in
 * item hover text.
 */
public class Compiler {
    /**
     * Parses an infix notation (eg "(a + b) - sqrt(12)") expression, returning an Expression object.
     */
    public static CompiledExpression Compile(TokenizedExpression expression) {
        Token[] tokenized = expression.getTokens();
        if (tokenized.length == 0) {
            return new CompiledExpression(expression);
        }

        ArrayList<Token> outputQueue = new ArrayList<>();
        Stack<Token> operatorStack = new Stack<>();

        // Stack for tracking how many arguments are passed to each function as we parse it
        Stack<Integer> functionArgs = new Stack<>();

        for (int i = 0; i < tokenized.length; i++) {
            Token token = tokenized[i];

            // Handle sub-expressions (ez)
            if(token.getType() == TokenType.LAMBDA) {
                token.setCompiledLambda(Compile(token.getTokenizedLambda()));
                token.setTokenizedLambda(null);
                outputQueue.add(token);
                continue;
            }

            // Handle numbers and variables
            if (token.getType().isValue()) {
                outputQueue.add(token);
                continue;
            }

            // Handle accessors
            if (token.getType() == TokenType.NAMESPACE_ACCESSOR) {
                outputQueue.add(token);
                continue;
            }

            // Handle function calls
            if (token.getType() == TokenType.FUNCTION) {
                operatorStack.add(token);

                int numArgsToInitiateWith = 1;

                // HACK: look ahead two tokens, which we know will exist because the parser ensures it.
                //       If it's a right paren, we know it's a function with no arguments,
                //       so we can just add it to the output queue and move on.
                //       We do this because we track the number of function arguments by incrementing whenever we encounter
                //       an argument separator (comma), so to handle single-argument (no-comma) scenarios we start with 1,
                //       but this breaks in a no-argument scenario.
                if(i + 2 < tokenized.length
                        && tokenized[i + 2].getType() == TokenType.PAREN_RIGHT) {
                    numArgsToInitiateWith = 0;
                }

                functionArgs.add(numArgsToInitiateWith);
                continue;
            }

            // Handle comma
            if(token.getType() == TokenType.COMMA) {
                // While the top of the stack isn't a left paren, pop from operator stack to output queue
                while (true) {
                    // If the stack is empty before we hit a left paren, there's a mismatch
                    if (operatorStack.isEmpty()) {
                        throw new IllegalArgumentException("Operator stack empty");
                    }

                    // If we find a left paren, exit the loop. Leave the left paren on the stack.
                    if (operatorStack.peek().getType() == TokenType.PAREN_LEFT) {
                        break;
                    }

                    // If not a left paren, just move to the output queue
                    outputQueue.add(operatorStack.pop());
                }

                // Increment top of functionArgs stack
                functionArgs.push(functionArgs.pop() + 1);
                continue;
            }

            // Handle operators
            if (token.getType().isOperator()) {
                // While there exists a token at the top of the stack that should be moved to the output queue
                // before this operator is added to the stack, do so
                while (ShuntOperator(token, operatorStack)) {
                    outputQueue.add(operatorStack.pop());
                }

                operatorStack.add(token);
                continue;
            }

            // Handle left paren
            if (token.getType() == TokenType.PAREN_LEFT) {
                operatorStack.add(token);
            }

            // Handle right paren
            if (token.getType() == TokenType.PAREN_RIGHT) {
                // While the top of the stack isn't a left paren, pop from operator stack to output queue
                while (true) {
                    // If the stack is empty before we hit a left paren, there's a mismatch
                    if (operatorStack.isEmpty()) {
                        throw new IllegalArgumentException("Operator stack empty");
                    }

                    Token popped = operatorStack.pop();
                    if (popped.getType() == TokenType.PAREN_LEFT) {
                        // If we hit a left paren, drop it from the stack and don't move it to the output queue
                        break;
                    }

                    // If not a left paren, just move to the output queue
                    outputQueue.add(popped);
                }

                // If the top of the operator stack is a function, then the function should also be moved now
                if (!operatorStack.isEmpty() && operatorStack.peek().getType() == TokenType.FUNCTION) {
                    if(functionArgs.size() == 0) {
                        throw new IllegalArgumentException("Function argument stack corrupted");
                    }

                    Token funcArg = operatorStack.pop();
                    funcArg.setNumFunctionArgs(functionArgs.pop());
                    outputQueue.add(funcArg);
                }
            }
        }

        // Cleanup Stack
        while (!operatorStack.isEmpty()) {
            Token popped = operatorStack.pop();

            if (popped.getType().isParenthesis()) {
                throw new IllegalArgumentException("Mismatched parentheses");
            }

            outputQueue.add(popped);
        }

        CompiledExpression compiled = new CompiledExpression(outputQueue.toArray(new Token[0]), expression.getExpressionText(), expression.isLegacy());
        return compiled;
    }

    /**
     * Called when processing an operator token.
     *
     * Determines if the top of the stack should be popped to the output queue before the new operator
     * is added to the stack.
     */
    private static boolean ShuntOperator(Token newToken, Stack<Token> stack) {
        // No top of stack? Nothing to shunt
        // (I don't know if "shunt" is the right word to actually use, but I like it)
        if (stack.isEmpty()) {
            return false;
        }

        Token topOfStack = stack.peek();

        // If it's a left paren, don't shunt
        if (topOfStack.getType() == TokenType.PAREN_LEFT) {
            return false;
        }

        // If the top has higher precedence than the new operator,
        // OR the same precedence but the new operator is left-associative,
        // it should be shunted.
        int topPrecedence = topOfStack.getType().getPrecedence();
        int newPrecedence = newToken.getType().getPrecedence();

        if (topPrecedence < newPrecedence) {
            return true;
        }

        if (topPrecedence == newPrecedence && !newToken.getType().isRightAssociative()) {
            return true;
        }

        // No shuntage
        return false;
    }
}


