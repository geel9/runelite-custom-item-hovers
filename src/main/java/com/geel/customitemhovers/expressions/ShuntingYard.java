package com.geel.customitemhovers.expressions;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Performs the Shunting Yard Algorithm to convert a postfix mathematical expression to Reverse Polish Notation
 *
 * This is here so that we can enable rich mathematical expressions, including variables and functions, in
 * item hover text.
 */
public class ShuntingYard {
    @Inject
    Tokenizer tokenizer;

    public Token[] EvaluateExpression(String expression) {
        Token[] tokenized = tokenizer.Tokenize(expression);
        if (tokenized.length == 0) {
            return tokenized;
        }

        ArrayList<Token> outputQueue = new ArrayList<>();
        Stack<Token> operatorStack = new Stack<>();

        for (Token token : tokenized) {
            // Handle numbers and variables
            if (token.getType().isValue()) {
                outputQueue.add(token);
                continue;
            }

            // Handle functions
            if (token.getType() == TokenType.FUNCTION) {
                operatorStack.add(token);
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
                    outputQueue.add(operatorStack.pop());
                }
            }

        }

        // Cleanup SY
        while (!operatorStack.isEmpty()) {
            Token popped = operatorStack.pop();

            if (popped.getType().isParenthesis()) {
                throw new IllegalArgumentException("Mismatched parentheses");
            }

            outputQueue.add(popped);
        }

        return outputQueue.toArray(new Token[0]);
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

        if (topPrecedence > newPrecedence) {
            return true;
        }

        if (topPrecedence == newPrecedence && !newToken.getType().isRightAssociative()) {
            return true;
        }

        // No shuntage
        return false;
    }
}


