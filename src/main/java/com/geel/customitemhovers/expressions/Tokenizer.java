package com.geel.customitemhovers.expressions;

import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.TokenType;
import com.geel.customitemhovers.expressions.models.TokenizedExpression;
import lombok.Getter;

import java.util.ArrayList;

/**
 * Tokenizes a mathematical expression (including variables and functions).
 * <p>
 * Not for external use; used by `ExpressionParser` as first step in parsing of an expression
 */
public class Tokenizer {
    // The text we are evaluating to find an expression in
    private String haystack;

    // The actual expression we've parsed out
    private StringBuilder parsedExpressionBuilder = new StringBuilder();
    private TokenizedExpression tokenizedExpression = null;

    // The tokens we've read so far.
    // Empty if tokenization has not yet begun, or has already finished.
    private final ArrayList<Token> tokens = new ArrayList<>();

    // Where we started reading the string
    private int startPos = 0;

    // How far into the current string we've read
    @Getter
    private int numRead = 0;

    // The current parentheses depth
    private int parenDepth = 0;

    // The current ternary depth
    private int ternaryDepth = 0;

    // The current sub-expression depth
    private int subExpressionDepth = 0;

    public Tokenizer(String haystack, int position) {
        this.haystack = haystack;
        this.startPos = position;
    }

    // Sub-expression constructor
    private Tokenizer(Tokenizer other) {
        this.haystack = other.haystack;
        this.startPos = other.startPos + other.numRead;
        this.subExpressionDepth = other.subExpressionDepth + 1;
    }

    /**
     * Tokenizes the expression and returns the result.
     * <p>
     * If the expression has already been tokenized, just returns the result.
     */
    public TokenizedExpression tokenize() {
        // Do actual tokenization if we haven't yet
        if (numRead == 0) {
            boolean isLegacyExpression = false;
            boolean isFirst = subExpressionDepth == 0;
            Token t;
            while ((t = readToken()) != null) {
                TokenType type = t.getType();
                // First token must be expression start, and expression start can only appear as the first token
                if (isFirst && !type.isExpressionBegin()) {
                    throw new IllegalArgumentException("Expected EXPRESSION_START");
                } else if (!isFirst && type.isExpressionBegin()) {
                    throw new IllegalArgumentException("Unexpected EXPRESSION_START");
                }

                // Detect if it's a legacy expression
                if (type.isLegacyExpressionBeginOrEnd()) {
                    isLegacyExpression = true;
                }

                // If we reach an expression end, we're done
                if (type.isExpressionEnd()) {
                    // End of top-level expression?
                    parsedExpressionBuilder.setLength(parsedExpressionBuilder.length() - t.getValue().length());
                    break;
                }

                // Don't add ${ and } to the actual tokenized output / expression
                if (!type.isExpressionBeginOrEnd()) {
                    tokens.add(t);
                } else {
                    // Strip out opening/ending tags from output
                    parsedExpressionBuilder.setLength(parsedExpressionBuilder.length() - t.getValue().length());
                }

                isFirst = false;
            }

            if (!isValidFinalToken()) {
                throw new IllegalArgumentException("Invalid final token in expression");
            }

            // Copy expression into parsedExpression, and clear out haystack and the builder
            tokenizedExpression = new TokenizedExpression(tokens.toArray(new Token[0]), parsedExpressionBuilder.toString(), isLegacyExpression);
            parsedExpressionBuilder.setLength(0);
            parsedExpressionBuilder = null;
            haystack = null;
        }

        // Result is null after we've read some -- expression must be invalid
        if (tokenizedExpression == null) {
            throw new IllegalArgumentException("Invalid expression");
        }

        return tokenizedExpression;
    }

    /**
     * Reads a single Token from the input string.
     * <p>
     * Returns null if the end of the string has been reached.
     */
    private Token readToken() {
        boolean isFirstToken = numRead == 0;

        if ((startPos + numRead) >= haystack.length()) {
            return null;
        }

        // Find the first next character that isn't whitespace
        while (isWhitespaceChar(peekChar()))
            readChar();

        char c = peekChar();

        // Handle expression begin/end. We do this specially here because, although we classify them as
        // "operators" (for ease of parsing), the rules are different for them.
        if (isFirstToken && isExpressionBeginFirstChar(c)) {
            return readOperator();
        } else if (!isFirstToken && isExpressionEndFirstChar(c)) {
            return readOperator();
        }

        // Handle *sub-expression* begin.
        if(c == '{') {
            assertCanReadIdentifier();
            return readSubExpression();
        }

        // Handle numbers
        if (isNumber(c)) {
            assertCanReadIdentifier();
            return readNumber();
        }

        // Handle strings
        if (c == '\'') {
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

    private Token readSubExpression() {
        readChar();

        // Create a new sub-Tokenizer
        Tokenizer tokenizer = new Tokenizer(this);

        // Read the sub-expression
        TokenizedExpression subExpression = tokenizer.tokenize();
        Token subExpressionToken = new Token(TokenType.SUB_EXPRESSION, subExpression.getExpressionText());
        subExpressionToken.setTokenizedSubExpression(subExpression);

        numRead += tokenizer.getNumRead();

        return subExpressionToken;
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

            possibleOperators = newPossibleOperators;
            buff.append(readChar());

            // If only one matches, and it has the same length as the current buffer, use it
            if (possibleOperators.size() == 1) {
                TokenType type = possibleOperators.get(0);
                if (type.getConstantValue().length() == buff.length()) {
                    break;
                }
            }
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
        if ((startPos + numRead) >= haystack.length()) return '\0';
        char ret = haystack.charAt((startPos + numRead++));
        parsedExpressionBuilder.append(ret);
        return ret;
    }

    /**
     * Reads a single character without advancing the stream
     */
    private char peekChar() {
        if ((startPos + numRead) >= haystack.length()) return '\0';
        return haystack.charAt(startPos + numRead);
    }

    /**
     * Gets the type of the last token we parsed
     */
    private TokenType lastTokenType() {
        return tokens.size() == 0 ? null : tokens.get(tokens.size() - 1).getType();
    }

    /**
     * Gets the type of the token BEFORE the last token we parsed
     */
    private TokenType secondToLastTokenType() {
        return tokens.size() <= 1 ? null : tokens.get(tokens.size() - 2).getType();
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
        if (!(token != null && (token.isValue() || token == TokenType.SUB_EXPRESSION || token == TokenType.PAREN_RIGHT))) {
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

        TokenType lastToken = lastTokenType();
        TokenType secondToLastToken = secondToLastTokenType();

        if (lastToken == null) {
            throw new IllegalArgumentException("Unexpected ) at start of expression");
        }

        // Right parentheses is always allowed to follow another right parentheses or a value
        if (lastToken == TokenType.PAREN_RIGHT || lastToken.isValue() || lastToken == TokenType.SUB_EXPRESSION) {
            return;
        }

        // HACK: If the last token isn't a right paren or a value, we *may* still be allowed to parse a right paren
        // *IF* the last token was a LEFT paren and the token before that is a function identifier.
        // This is a special case for function calls with no arguments.
        if (lastToken == TokenType.PAREN_LEFT && secondToLastToken == TokenType.FUNCTION) {
            return;
        }

        throw new IllegalArgumentException("Unexpected ) after " + lastToken.name());
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

    private static boolean isExpressionBeginFirstChar(char c) {
        return c == '$' || c == '<';
    }

    private static boolean isExpressionEndFirstChar(char c) {
        return c == '}' || c == '%';
    }

    final static char[] allowedOperators = "${}+-*/%><=!&|^?:,".toCharArray();

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
