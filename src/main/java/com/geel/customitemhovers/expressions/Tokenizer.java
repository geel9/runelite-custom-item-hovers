package com.geel.customitemhovers.expressions;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Tokenizes a mathematical expression (including variables and functions)
 */
public class Tokenizer {
    public Token[] Tokenize(String input) {
        ArrayList<Token> output = new ArrayList<>();
        StringBuilder tempBuff = new StringBuilder();
        boolean isReadingIdentifier = false;

        ArrayList<TokenType> possibleOperators = new ArrayList<>();
        boolean isReadingOperator = false;

        int parenDepth = 0;

        Consumer<TokenType> addToken = (TokenType t) -> {
            output.add(new Token(t, tempBuff.toString()));
            tempBuff.delete(0, tempBuff.length());
        };

        for (int i = 0; i < input.length(); i++) {
            char curChar = input.charAt(i);

            TokenType curType = GetTokenType(curChar);
            if (curType == null) {
                throw new IllegalArgumentException("Invalid expression");
            }

            // If we're currently reading an operator, then we should evaluate this new character
            // to see if it belongs to the current operator or is the start of a new token
            if (isReadingOperator) {
                String curOperatorText = tempBuff.toString();
                String withNewChar = curOperatorText + curChar; // Eep

                // Determine which operators start with the new total string
                ArrayList<TokenType> newPossibleOperators = ComputeOperatorsStartingWith(withNewChar);

                // New string does not complete an operator.
                // Take old string, find its equivalent operator, and push it.
                // Then treat new character as start of new token.
                if (newPossibleOperators.size() == 0) {
                    Optional<TokenType> match = possibleOperators.stream().filter(t -> t.getConstantValue().equals(curOperatorText)).findFirst();
                    if (match.isEmpty()) {
                        throw new IllegalArgumentException("Invalid operator " + curOperatorText);
                    }

                    addToken.accept(match.get());
                    isReadingOperator = false;
                    possibleOperators.clear();
                }
                else if (newPossibleOperators.size() == 1 && newPossibleOperators.get(0).getConstantValue().equals(withNewChar)) {
                    // Only one possible operator left, and it matches the new char.
                    // Consume the operator and move on to the next char
                    tempBuff.append(curChar);
                    addToken.accept(newPossibleOperators.get(0));

                    isReadingOperator = false;
                    possibleOperators.clear();
                    continue;
                } else {
                    // Either multiple possible operators left, OR one operator that's longer than the current buffer.
                    // Just keep reading.
                    tempBuff.append(curChar);
                    continue;
                }
            }

            // If we're currently reading an identifier, and the current token type is NOT an identifier,
            // then we're done reading the identifier
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
                if (!IsValidBeforeIdentifier(lastToken)) {
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

                possibleOperators = ComputeOperatorsStartingWith(String.valueOf(curChar));

                if (possibleOperators.size() == 0) {
                    // No operators start with this character? Impossible
                    throw new IllegalArgumentException("Somehow, no possible operators? Don't think this code will ever be hit actually");
                } else if (possibleOperators.size() == 1 && possibleOperators.get(0).getConstantValue().length() == 1) {
                    // Only one operator starts with this character, and it's a single-character operator.
                    // Just consume it immediately.
                    // We'll fall through to `addToken.accept(curType)` below.
                    // Set curType to what the operator actually is (though they should match).
                    curType = possibleOperators.get(0);
                } else {
                    // Either multiple operators start with this character, OR only one does, but it's a multi-char
                    // operator, so we need to finish reading it.
                    isReadingOperator = true;
                    continue;
                }
            }

            addToken.accept(curType);
        }

        // Handle reading an identifier at the end
        if (isReadingIdentifier) {
            addToken.accept(DiscriminateIdentifier(tempBuff, '\0'));
        }

        // Handle reading an operator at the end
        // We can just fail -- operators shouldn't be ending an expression
        if (isReadingOperator) {
            throw new IllegalArgumentException("Expression ended with an operator");
        }

        // Handle dangling parenthesis
        if (parenDepth != 0) {
            throw new IllegalArgumentException("Unmatched opening parenthesis");
        }

        // Ensure we ended on a good note
        if (output.size() > 0 && !IsValidFinalToken(output.get(output.size() - 1))) {
            throw new IllegalArgumentException("Invalid ending token");
        }

        return output.toArray(new Token[0]);
    }

    private static ArrayList<TokenType> ComputeOperatorsStartingWith(String prefix) {
        ArrayList<TokenType> ret = new ArrayList<>(2);

        for (TokenType tokenType : TokenType.GetOperatorTokenTypes()) {
            if (tokenType.getConstantValue().startsWith(prefix)) {
                ret.add(tokenType);
            }
        }

        return ret;
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
     * <p>
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
            case '%':
                return TokenType.OP_MODULO;
            case '<':
                return TokenType.CMP_LT;
            case '>':
                return TokenType.CMP_GT;
            case '=':
                return TokenType.CMP_EQUALS;
            case '!':
                return TokenType.CMP_NEQUALS;
            case '&':
                return TokenType.LOG_AND;
            case '|':
                return TokenType.LOG_OR;
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

    private static boolean IsOperatorChar(char c) {
        return c == '>' || c == '<' || c == '=' || c == '+' || c == '-' || c == '|' || c == '&';
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
