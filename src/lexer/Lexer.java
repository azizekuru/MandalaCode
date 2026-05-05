// Lexer.java
package lexer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final String source;
    private int pos; // current character index
    private int line; // current line number (1-based)

    // ── Keyword map ─────────────────────────────────────────────
    private static final java.util.Map<String, TokenType> KEYWORDS = new java.util.HashMap<>();

    static {
        KEYWORDS.put("brush", TokenType.BRUSH);
        KEYWORDS.put("pattern", TokenType.PATTERN);
        KEYWORDS.put("draw", TokenType.DRAW);
        KEYWORDS.put("radial_repeat", TokenType.RADIAL_REPEAT);
        KEYWORDS.put("let", TokenType.LET);
        KEYWORDS.put("if", TokenType.IF);
        KEYWORDS.put("else", TokenType.ELSE);
        KEYWORDS.put("true", TokenType.TRUE);
        KEYWORDS.put("false", TokenType.FALSE);
        KEYWORDS.put("angle", TokenType.TYPE_ANGLE);
        KEYWORDS.put("radius", TokenType.TYPE_RADIUS);
        KEYWORDS.put("color", TokenType.TYPE_COLOR);
        KEYWORDS.put("int", TokenType.TYPE_INT);
        KEYWORDS.put("bool", TokenType.TYPE_BOOL);
    }

    public Lexer(String source) {
        this.source = source;
        this.pos = 0;
        this.line = 1;
    }

    // ── Public entry point ───────────────────────────────────────

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (!isAtEnd()) {
            skipWhitespaceAndComments();
            if (isAtEnd())
                break;
            tokens.add(scanToken());
        }

        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    // ── Core scanner ─────────────────────────────────────────────

    private Token scanToken() {
        char c = advance();

        switch (c) {

            // ── Single-character separators ──────────────────────
            case '(':
                return token(TokenType.LPAREN, "(");
            case ')':
                return token(TokenType.RPAREN, ")");
            case '{':
                return token(TokenType.LBRACE, "{");
            case '}':
                return token(TokenType.RBRACE, "}");
            case ',':
                return token(TokenType.COMMA, ",");
            case ':':
                return token(TokenType.COLON, ":");
            case ';':
                return token(TokenType.SEMICOLON, ";");
            case '.':
                return token(TokenType.DOT, ".");

            // ── Math operators ───────────────────────────────────
            case '+':
                return token(TokenType.PLUS, "+");
            case '-':
                return token(TokenType.MINUS, "-");
            case '*':
                return token(TokenType.STAR, "*");
            case '/':
                return token(TokenType.SLASH, "/");

            // ── One-or-two-character operators (maximal munch) ───
            case '=':
                return token(match('=') ? TokenType.EQ : TokenType.ASSIGN,
                        match('=') ? "==" : "="); // note: match already advanced
            case '!':
                return token(match('=') ? TokenType.NEQ : TokenType.BANG,
                        peekPrev2());
            case '<':
                return token(match('=') ? TokenType.LTE : TokenType.LT,
                        peekPrev2());
            case '>':
                return token(match('=') ? TokenType.GTE : TokenType.GT,
                        peekPrev2());
            case '&':
                if (match('&'))
                    return token(TokenType.AND, "&&");
                throw lexError("unexpected character '&' — did you mean '&&'?");
            case '|':
                if (match('|'))
                    return token(TokenType.OR, "||");
                throw lexError("unexpected character '|' — did you mean '||'?");

            // ── Color literal ────────────────────────────────────
            case '#':
                return scanColorLit();

            default:
                if (isDigit(c))
                    return scanNumber(c);
                if (isAlpha(c))
                    return scanIdentOrKeyword(c);
                throw lexError("unrecognized character '" + c + "'");
        }
    }

    // ── Literal scanners ─────────────────────────────────────────

    private Token scanNumber(char first) {
        StringBuilder sb = new StringBuilder();
        sb.append(first);

        while (!isAtEnd() && isDigit(peek())) {
            sb.append(advance());
        }

        // check for decimal point followed by at least one digit
        if (!isAtEnd() && peek() == '.' && isDigit(peekNext())) {
            sb.append(advance()); // consume '.'
            while (!isAtEnd() && isDigit(peek())) {
                sb.append(advance());
            }
            return token(TokenType.FLOAT_LIT, sb.toString());
        }

        return token(TokenType.INT_LIT, sb.toString());
    }

    private Token scanColorLit() {
        // '#' already consumed — now expect exactly 6 hex digits
        StringBuilder sb = new StringBuilder("#");
        for (int i = 0; i < 6; i++) {
            if (isAtEnd() || !isHexDigit(peek())) {
                throw lexError(
                        "invalid color literal '" + sb + "': expected exactly "
                                + "6 hexadecimal digits after '#' (e.g. #FF8800)");
            }
            sb.append(advance());
        }

        // guard: a 7th hex digit would make it malformed (#FF88001)
        if (!isAtEnd() && isHexDigit(peek())) {
            sb.append(advance()); // consume the offending extra char for context
            throw lexError(
                    "invalid color literal '" + sb + "': too many hex digits after '#'");
        }

        return token(TokenType.COLOR_LIT, sb.toString());
    }

    private Token scanIdentOrKeyword(char first) {
        StringBuilder sb = new StringBuilder();
        sb.append(first);

        // radial_repeat contains '_' — isAlphaNumeric covers it
        while (!isAtEnd() && isAlphaNumeric(peek())) {
            sb.append(advance());
        }

        String text = sb.toString();
        TokenType type = KEYWORDS.getOrDefault(text, TokenType.IDENT);
        return token(type, text);
    }

    // ── Whitespace & comment skipping ────────────────────────────

    private void skipWhitespaceAndComments() {
        while (!isAtEnd()) {
            char c = peek();
            switch (c) {
                case ' ':
                case '\r':
                case '\t':
                    advance();
                    break;
                case '\n':
                    line++;
                    advance();
                    break;
                case '/':
                    // single-line comment: // …
                    if (peekNext() == '/') {
                        while (!isAtEnd() && peek() != '\n')
                            advance();
                    } else {
                        return; // it's a SLASH token, not a comment
                    }
                    break;
                default:
                    return;
            }
        }
    }

    // ── Error factory ────────────────────────────────────────────

    private RuntimeException lexError(String message) {
        return new RuntimeException(
                "[Lexer error] Line " + line + ": " + message);
    }

    // ── Navigation helpers ───────────────────────────────────────

    private char advance() {
        return source.charAt(pos++);
    }

    // consume the next character only if it matches expected
    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(pos) != expected)
            return false;
        pos++;
        return true;
    }

    private char peek() {
        return source.charAt(pos);
    }

    private char peekNext() {
        if (pos + 1 >= source.length())
            return '\0';
        return source.charAt(pos + 1);
    }

    // returns the one-or-two character lexeme that was just committed
    // used to build the value string after a match() call
    private String peekPrev2() {
        // pos was already advanced past the second char if match() succeeded
        // walk back up to 2 positions to reconstruct the lexeme
        int start = pos - 2;
        if (start < 0)
            start = 0;
        return source.substring(start, pos);
    }

    private boolean isAtEnd() {
        return pos >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isHexDigit(char c) {
        return isDigit(c)
                || (c >= 'a' && c <= 'f')
                || (c >= 'A' && c <= 'F');
    }

    private boolean isAlpha(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    // ── Token factory ────────────────────────────────────────────

    private Token token(TokenType type, String value) {
        return new Token(type, value, line);
    }
}