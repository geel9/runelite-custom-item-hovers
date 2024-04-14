package com.geel.customitemhovers.expressions;

import lombok.Getter;

import java.util.ArrayList;

/**
 * Tokenizes a mathematical expression (including variables and functions).
 * <p>
 * Not for external use; used by `ExpressionParser` as first step in parsing of an expression
 */
public class Tokenizer {
    @Getter
    private final String expression;
    @Getter
    private final ArrayList<Token> tokens = new ArrayList<>();

    // How far into the current string we've read
    private int pos = 0;

    // The current parentheses depth
    private int parenDepth = 0;

    // The current ternary depth
    private int ternaryDepth = 0;

    public Tokenizer(String expression) {
        this.expression = expression;
    }

    /**
     * Tokenizes the expression and returns the result.
     * <p>
     * If the expression has already been tokenized, just returns the result.
     */
    public Token[] tokenize() {
        Token t;
        while ((t = readToken()) != null) {
            tokens.add(t);
        }

        if (!isValidFinalToken()) {
            throw new IllegalArgumentException("Invalid final token in expression");
        }

        return tokens.toArray(new Token[0]);
    }

    /**
     * Reads a single Token from the input string.
     * <p>
     * Returns null if the end of the string has been reached.
     */
    private Token readToken() {
        if (pos >= expression.length()) {
            return null;
        }

        // Find the first next character that isn't whitespace
        while (isWhitespaceChar(peekChar()))
            readChar();

        char c = peekChar();

        // Handle numbers
        if (isNumber(c)) {
            assertCanReadIdentifier();
            return readNumber();
        }

        // Handle strings
        if(c == '\'') {
            assertCanReadIdentifier();
            return readString();
        }

        // Handle identifiers
        if (isNameChar(c)) {
            assertCanReadIdentifier();
            return readIdentifier();
        }

        // Handle parentheses
        if (c == '(') {
            assertCanReadParenStart();
            parenDepth++;
            return readOperator();
        } else if (c == ')') {
            assertCanReadParenEnd();
            parenDepth--;
            return readOperator();
        }

        // Handle ternary
        if (c == '?') {
            assertCanReadTernaryThen();
            ternaryDepth++;
            return readOperator();
        } else if (c == ':') {
            assertCanReadTernaryElse();
            ternaryDepth--;
            return readOperator();
        }

        // Handle all operators
        if (isOperatorChar(c)) {
            assertCanReadOperator();
            return readOperator();
        }

        return null;
    }

    /**
     * Reads a number Token from the string
     */
    private Token readNumber() {
        StringBuilder builder = new StringBuilder();
        boolean hasHadPeriod = false;

        do {
            char c = readChar();
            builder.append(c);

            if (c == '.') {
                if (hasHadPeriod || builder.length() == 1)
                    throw new IllegalArgumentException("Invalid period placement");

                hasHadPeriod = true;
            }
        } while (isNumericChar(peekChar()));

        if (hasHadPeriod) {
            return Token.fromDouble(Double.parseDouble(builder.toString()));
        } else {
            return Token.fromInt(Integer.parseInt(builder.toString()));
        }
    }

    /**
     * Reads a string constant Token from the string
     */
    private Token readString() {
        readChar(); // Discard opening '
        StringBuilder builder = new StringBuilder();
        boolean isEscaping = false;

        char c;
        while ((c = readChar()) != '\0') {
            // If an escape is afoot, just append whatever character is after.
            // There are no invalid escapes :)
            if (isEscaping) {
                builder.append(c);
                isEscaping = false;
                continue;
            }

            // Detect an escapee
            if (c == '\\') {
                isEscaping = true;
                continue;
            }

            // Handle closing string
            if (c == '\'') {
                return new Token(TokenType.STRING, builder.toString());
            }

            // If it's not an escape or a ', I think... any character is valid?
            builder.append(c);
        }

        throw new IllegalArgumentException("End of string reached without string termination");
    }

    /**
     * Reads an identifier Token (variable name, function name) from the string
     */
    private Token readIdentifier() {
        StringBuilder builder = new StringBuilder();

        do {
            char c = readChar();
            builder.append(c);

            if (builder.length() == 1 && isNumber(c)) {
                throw new IllegalArgumentException("First character of an identifier cannot be a number");
            }
        } while (isNameChar(peekChar()));

        // Won't catch a space between identifier and paren. Don't really care atm.
        if (peekChar() == '(') {
            return new Token(TokenType.FUNCTION, builder.toString());
        }

        return new Token(TokenType.VARIABLE, builder.toString());
    }

    /**
     * Reads an operator Token from the string
     */
    private Token readOperator() {
        StringBuilder buff = new StringBuilder();
        buff.append(readChar());

        ArrayList<TokenType> possibleOperators = computeOperatorsStartingWith(buff.toString());
        int offset = 0;
        while (true) {
            char c = peekChar();
            ArrayList<TokenType> newPossibleOperators = constrainOperators(c, ++offset, possibleOperators);

            // If nothing matches the next character, then we need to just use whatever matches `buff`
            if (newPossibleOperators.size() == 0) {
                TokenType type = possibleOperators.stream().filter(p -> p.getConstantValue().length() == buff.length()).findFirst().get();
                return new Token(type, buff.toString());
            }

            // If only one matches, and it has the same length as the current buffer, use it
            if (newPossibleOperators.size() == 1) {
                TokenType type = possibleOperators.get(possibleOperators.size() - 1);
                if (type.getConstantValue().length() == buff.length()) {
                    break;
                }
            }

            // If multiple things still match, or only one matches but it's not fully read yet, continue
            possibleOperators = newPossibleOperators;
            buff.append(readChar());
        }

        return new Token(possibleOperators.get(0), buff.toString());
    }

    /**
     * Returns a list of the operators which start with the given string
     */
    private static ArrayList<TokenType> computeOperatorsStartingWith(String prefix) {
        ArrayList<TokenType> ret = new ArrayList<>(2);

        for (TokenType tokenType : TokenType.GetOperatorTokenTypes()) {
            if (tokenType.getConstantValue().startsWith(prefix)) {
                ret.add(tokenType);
            }
        }

        return ret;
    }

    /**
     * Returns a list of the operators in `existing` which continue to match with the addition of `newChar`
     */
    private static ArrayList<TokenType> constrainOperators(char newChar, int offset, ArrayList<TokenType> existing) {
        ArrayList<TokenType> ret = new ArrayList<>(2);

        for (TokenType tokenType : existing) {
            String value = tokenType.getConstantValue();

            // If it's too short, don't consider it
            if (offset >= value.length()) {
                continue;
            }

            if (value.charAt(offset) != newChar) {
                continue;
            }

            ret.add(tokenType);
        }

        return ret;
    }

    /**
     * Reads a single character and advances the stream
     */
    private char readChar() {
        if (pos >= expression.length()) return '\0';
        return expression.charAt(pos++);
    }

    /**
     * Reads a single character without advancing the stream
     */
    private char peekChar() {
        if (pos >= expression.length()) return '\0';
        return expression.charAt(pos);
    }

    /**
     * Gets the type of the last token we parsed
     */
    private TokenType lastTokenType() {
        return tokens.size() == 0 ? null : tokens.get(tokens.size() - 1).getType();
    }

    /**
     * Determines if an Identifier can start given the last Token
     */
    private void assertCanReadIdentifier() {
        TokenType token = lastTokenType();
        if (!(token == null || token.isOperator() || token == TokenType.PAREN_LEFT)) {
            throw new IllegalArgumentException("Unexpected identifier");
        }
    }

    /**
     * Determines if an Operator can start given the last Token
     */
    private void assertCanReadOperator() {
        TokenType token = lastTokenType();
        if (!(token != null && (token.isValue() || token == TokenType.PAREN_RIGHT))) {
            throw new IllegalArgumentException("Unexpected operator");
        }
    }

    private void assertCanReadTernaryThen() {
        assertCanReadOperator();
    }

    private void assertCanReadTernaryElse() {
        assertCanReadOperator();

        if (ternaryDepth <= 0) {
            throw new IllegalArgumentException("Mismatched ternary : operator");
        }
    }

    /**
     * Determines if a ) can occur given the last Token
     */
    private void assertCanReadParenEnd() {
        if (parenDepth <= 0) {
            throw new IllegalArgumentException("Mismatched ending parenthesis");
        }
        TokenType token = lastTokenType();
        if (!(token != null
                && (token == TokenType.PAREN_RIGHT || token.isValue()))) {
            throw new IllegalArgumentException("Unexpected )");
        }
    }

    /**
     * Determines if a ( can occur given the last Token
     */
    private void assertCanReadParenStart() {
        TokenType token = lastTokenType();
        if (!(token == null
                || token == TokenType.PAREN_LEFT
                || token == TokenType.FUNCTION
                || token.isOperator())) {
            throw new IllegalArgumentException("Unexpected (");
        }
    }

    /**
     * Determines if a Token is a valid ending token
     */
    private boolean isValidFinalToken() {
        TokenType token = lastTokenType();
        return token != null && token.isValue() || token == TokenType.PAREN_RIGHT;
    }

    final static char[] allowedOperators = "+-*/%><=!&|?:".toCharArray();

    /**
     * Determines if c is a valid operator character
     */
    private static boolean isOperatorChar(char c) {

        for (char t : allowedOperators) {
            if (t == c) return true;
        }

        return false;
    }

    /**
     * Determines if a character is in the character set allowed for identifiers (function and variable names)
     */
    private static boolean isNameChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    /**
     * Determines if a character is in the character set allowed for numbers (0-9 and a period)
     */
    private static boolean isNumericChar(char c) {
        return isNumber(c) || c == '.';
    }

    /**
     * Determines if a character is in the character set allowed for numbers (0-9)
     */
    private static boolean isNumber(char c) {
        return (c >= '0' && c <= '9');
    }

    /**
     * Determines if a character is in the character set allowed for whitespace
     */
    private static boolean isWhitespaceChar(char c) {
        return c == ' ' || c == '\t';
    }
}
