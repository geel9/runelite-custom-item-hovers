package com.geel.customitemhovers.expressions;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Tokenizes a mathematical expression (including variables and functions)
 */
public class Tokenizer {
    public Token[] Tokenize(String input) {
        ArrayList<Token> output = new ArrayList<>();
        StringBuilder tempBuff = new StringBuilder();
        boolean isReadingIdentifier = false;
        int parenDepth = 0;

        Consumer<TokenType> addToken = (TokenType t) -> {
            Token token = new Token(t, tempBuff.toString());
            output.add(token);
            tempBuff.delete(0, tempBuff.length());
        };

        for (int i = 0; i < input.length(); i++) {
            char curChar = input.charAt(i);

            TokenType curType = GetTokenType(curChar);
            if (curType == null) {
                throw new IllegalArgumentException("Invalid expression");
            }

            if (isReadingIdentifier) {
                if (!curType.isIdentifier()) {
                    // Not an identifier character -- identifier has ended. Process character normally.
                    addToken.accept(DiscriminateIdentifier(tempBuff, curChar));
                    isReadingIdentifier = false;
                }
            }

            // If whitespace, then just ignore the character.
            if (curType == TokenType.WHITESPACE) {
                continue;
            }

            Token lastToken = output.size() == 0 ? null : output.get(output.size() - 1);
            tempBuff.append(curChar);

            // If this is an identifier char, start readin' it
            if (curType.isIdentifier()) {
                if(!IsValidBeforeIdentifier(lastToken)) {
                    throw new IllegalArgumentException("Unexpected identifier start");
                }
                isReadingIdentifier = true;
                continue;
            }

            // Handle parentheses
            if (curType == TokenType.PAREN_LEFT) {
                if (!IsValidBeforeParenStart(lastToken)) {
                    throw new IllegalArgumentException("Invalid opening parenthesis");
                }

                parenDepth++;
            } else if (curType == TokenType.PAREN_RIGHT) {
                if (parenDepth == 0) {
                    throw new IllegalArgumentException("Ending parenthesis without an opening one");
                }

                if (!IsValidBeforeParenEnd(lastToken)) {
                    throw new IllegalArgumentException("Invalid ending parenthesis");
                }

                parenDepth--;
            } else if (curType.isOperator()) {
                if (!IsValidBeforeOperator(lastToken)) {
                    throw new IllegalArgumentException("Invalid operator placement");
                }
            }

            addToken.accept(curType);
        }

        // Handle reading an identifier at the end
        if (isReadingIdentifier) {
            isReadingIdentifier = false;
            addToken.accept(DiscriminateIdentifier(tempBuff, '\0'));
        }

        // Handle dangling parenthesis
        if(parenDepth != 0) {
            throw new IllegalArgumentException("Unmatched opening parenthesis");
        }

        // Ensure we ended on a good note
        if(output.size() > 0 && !IsValidFinalToken(output.get(output.size() - 1))) {
            throw new IllegalArgumentException("Invalid ending token");
        }

        return output.toArray(new Token[0]);
    }

    /**
     * Determines if a value that was read is an integer, double, variable, or function.
     */
    private static TokenType DiscriminateIdentifier(StringBuilder identifier, char newChar) throws IllegalArgumentException {
        char firstChar = identifier.charAt(0);
        boolean identifierIsNumeric = IsNumericChar(firstChar);

        // An identifier is numeric if it's an unending stream of numeric characters
        //      (but neither starts nor ends with a period)
        if (identifierIsNumeric) {
            if (firstChar == '.') {
                throw new IllegalArgumentException("Invalid identifier " + identifier.toString());
            }

            boolean containsPeriod = false;
            for (int i = 1; i < identifier.length(); i++) {
                boolean isEnd = i == identifier.length() - 1;
                char curChar = identifier.charAt(i);

                if (!IsNumericChar(curChar)) {
                    throw new IllegalArgumentException("Invalid identifier " + identifier.toString());
                }

                if (curChar != '.') {
                    continue;
                }

                if (containsPeriod) {
                    throw new IllegalArgumentException("Invalid identifier " + identifier.toString());
                }

                containsPeriod = true;

                if (isEnd) {
                    throw new IllegalArgumentException("Invalid identifier " + identifier.toString());
                }
            }

            return containsPeriod ? TokenType.DOUBLE : TokenType.INTEGER;
        }

        // If the next character after the identifier is a parenthesis, it's a function.
        // Otherwise, a variable
        // TODO: This won't catch instances where someone puts whitespace between an identifier and left paren
        return newChar == '(' ? TokenType.FUNCTION : TokenType.VARIABLE;
    }

    /**
     * Determines if an Identifier can start given the last Token
     */
    private static boolean IsValidBeforeIdentifier(Token token) {
        return token == null
                || token.getType().isOperator()
                || token.getType() == TokenType.PAREN_LEFT;
    }

    /**
     * Determines if an Operator can start given the last Token
     */
    private static boolean IsValidBeforeOperator(Token token) {
        return token != null &&
                (token.getType().isValue()
                        || token.getType() == TokenType.PAREN_RIGHT);
    }

    /**
     * Determines if a ) can occur given the last Token
     */
    private static boolean IsValidBeforeParenEnd(Token token) {
        return token != null
                && (token.getType() == TokenType.PAREN_RIGHT
                || token.getType().isValue());
    }

    /**
     * Determines if a ( can occur given the last Token
     */
    private static boolean IsValidBeforeParenStart(Token token) {
        return token == null
                || token.getType() == TokenType.PAREN_LEFT
                || token.getType() == TokenType.FUNCTION
                || token.getType().isOperator();
    }

    /**
     * Determines if a Token is a valid ending token
     */
    private static boolean IsValidFinalToken(Token token) {
        return token.getType().isValue() || token.getType() == TokenType.PAREN_RIGHT;
    }

    /**
     * Returns the Type of token corresponding to c, for most cases.
     *
     * Will return INTEGER for all numeric types and VARIABLE for identifiers (which may be functions).
     * Further investigation of the entire token value is necessary to discern the type of number or identifier.
     */
    private static TokenType GetTokenType(char c) {
        switch (c) {
            case '(':
                return TokenType.PAREN_LEFT;
            case ')':
                return TokenType.PAREN_RIGHT;
            case '+':
                return TokenType.OP_PLUS;
            case '-':
                return TokenType.OP_MINUS;
            case '*':
                return TokenType.OP_TIMES;
            case '/':
                return TokenType.OP_DIVIDE;
            case ' ':
            case '\t':
                return TokenType.WHITESPACE;
        }

        // Could be a DOUBLE, we can't know for sure (we check later)
        if (IsNumericChar(c)) {
            return TokenType.INTEGER;
        }

        // Could be a FUNCTION
        if (IsNameChar(c)) {
            return TokenType.VARIABLE;
        }

        return null;
    }

    /**
     * Determines if a character is in the character set allowed for identifiers (function and variable names)
     */
    private static boolean IsNameChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    /**
     * Determines if a character is in the character set allowed for numbers (0-9 and a period)
     */
    private static boolean IsNumericChar(char c) {
        return (c >= '0' && c <= '9') || c == '.';
    }
}
