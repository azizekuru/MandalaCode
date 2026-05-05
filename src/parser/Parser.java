// Parser.java
package parser;

import ast.*;
import lexer.Token;
import lexer.TokenType;

import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int pos; // index of the current token

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    // ════════════════════════════════════════════════════════════
    // PUBLIC ENTRY POINT
    // ════════════════════════════════════════════════════════════

    public ProgramNode parse() {
        // will be implemented when statement parsing is added
        throw new UnsupportedOperationException("Statement parsing not yet implemented.");
    }

    // ════════════════════════════════════════════════════════════
    // EXPRESSION PARSING
    // Precedence ladder (low → high):
    // parseExpr() → + -
    // parseTerm() → * /
    // parseUnary() → unary -
    // parseFactor() → literals, identifiers, grouped exprs
    // ════════════════════════════════════════════════════════════

    // <expr> ::= <term> { ( "+" | "-" ) <term> }
    protected Expression parseExpr() {
        Expression left = parseTerm();

        while (peek().isAny(TokenType.PLUS, TokenType.MINUS)) {
            Token opToken = advance();
            BinOpExpr.Op op = opToken.is(TokenType.PLUS)
                    ? BinOpExpr.Op.ADD
                    : BinOpExpr.Op.SUB;
            Expression right = parseTerm();
            left = new BinOpExpr(op, left, right, opToken.getLineNumber());
        }

        return left;
    }

    // <term> ::= <unary> { ( "*" | "/" ) <unary> }
    private Expression parseTerm() {
        Expression left = parseUnary();

        while (peek().isAny(TokenType.STAR, TokenType.SLASH)) {
            Token opToken = advance();
            BinOpExpr.Op op = opToken.is(TokenType.STAR)
                    ? BinOpExpr.Op.MUL
                    : BinOpExpr.Op.DIV;
            Expression right = parseUnary();
            left = new BinOpExpr(op, left, right, opToken.getLineNumber());
        }

        return left;
    }

    // <unary> ::= "-" <factor> | <factor>
    private Expression parseUnary() {
        if (peek().is(TokenType.MINUS)) {
            Token opToken = advance();
            Expression operand = parseFactor();
            return new UnaryExpr(UnaryExpr.Op.NEG, operand, opToken.getLineNumber());
        }
        if (peek().is(TokenType.BANG)) {
            Token opToken = advance();
            Expression operand = parseFactor();
            return new UnaryExpr(UnaryExpr.Op.NOT, operand, opToken.getLineNumber());
        }
        return parseFactor();
    }

    // <factor> ::= "(" <expr> ")"
    // | IDENT "." FIELD
    // | IDENT
    // | FLOAT_LIT ← checked before INT_LIT
    // | INT_LIT
    // | COLOR_LIT
    // | "true" | "false"
    private Expression parseFactor() {
        Token current = peek();

        // ── grouped expression ───────────────────────────────────
        if (current.is(TokenType.LPAREN)) {
            advance(); // consume '('
            Expression inner = parseExpr();
            consume(TokenType.RPAREN, "expected ')' after expression");
            return inner;
        }

        // ── float literal ────────────────────────────────────────
        if (current.is(TokenType.FLOAT_LIT)) {
            advance();
            return new FloatLitExpr(
                    Double.parseDouble(current.getValue()),
                    current.getLineNumber());
        }

        // ── integer literal ──────────────────────────────────────
        if (current.is(TokenType.INT_LIT)) {
            advance();
            return new IntLitExpr(
                    Integer.parseInt(current.getValue()),
                    current.getLineNumber());
        }

        // ── color literal ────────────────────────────────────────
        if (current.is(TokenType.COLOR_LIT)) {
            advance();
            return new ColorLitExpr(current.getValue(), current.getLineNumber());
        }

        // ── boolean literals ─────────────────────────────────────
        if (current.is(TokenType.TRUE)) {
            advance();
            // represent as IntLitExpr(1) internally, or add BoolLitExpr later
            return new IntLitExpr(1, current.getLineNumber());
        }
        if (current.is(TokenType.FALSE)) {
            advance();
            return new IntLitExpr(0, current.getLineNumber());
        }

        // ── identifier or field access ───────────────────────────
        if (current.is(TokenType.IDENT)) {
            Token identToken = advance();

            // look ahead for '.' → field access: IDENT.FIELD
            if (peek().is(TokenType.DOT)) {
                advance(); // consume '.'
                Token fieldToken = consume(TokenType.IDENT,
                        "expected field name after '.' on line "
                                + identToken.getLineNumber());
                return new FieldAccessExpr(
                        identToken.getValue(),
                        fieldToken.getValue(),
                        identToken.getLineNumber());
            }

            return new IdentExpr(identToken.getValue(), identToken.getLineNumber());
        }

        // ── nothing matched ──────────────────────────────────────
        throw parseError(current,
                "expected an expression but found '" + current.getValue() + "'");
    }

    // ════════════════════════════════════════════════════════════
    // BOOLEAN EXPRESSION PARSING
    // Handled separately from arithmetic expressions because
    // <bool_expr> appears only in <if_stmt> conditions, not inside
    // <expr>. This keeps the arithmetic precedence ladder clean.
    //
    // Precedence (low → high):
    // parseBoolExpr() → || (left-associative)
    // parseBoolAnd() → && (left-associative)
    // parseBoolComparison() → == != < <= > >= (non-associative)
    // ════════════════════════════════════════════════════════════

    // <bool_expr> ::= <bool_and> { "||" <bool_and> }
    protected BoolExpr parseBoolExpr() {
        Expression left = parseBoolAnd();
        int lineNum = peek().getLineNumber();

        while (peek().is(TokenType.OR)) {
            Token opToken = advance();
            Expression right = parseBoolAnd();
            left = new BoolExpr(BoolExpr.Op.OR, left, right, opToken.getLineNumber());
        }

        return guardBoolExpr(left, lineNum);
    }

    // <bool_and> ::= <bool_comparison> { "&&" <bool_comparison> }
    private Expression parseBoolAnd() {
        Expression left = parseBoolComparison();

        while (peek().is(TokenType.AND)) {
            Token opToken = advance();
            Expression right = parseBoolComparison();
            left = new BoolExpr(BoolExpr.Op.AND, left, right, opToken.getLineNumber());
        }

        return left;
    }

    // <bool_comparison> ::= <expr> <rel_op> <expr>
    // | "!" <bool_comparison>
    // | "true" | "false"
    private Expression parseBoolComparison() {

        // ── logical NOT ──────────────────────────────────────────
        if (peek().is(TokenType.BANG)) {
            Token bangToken = advance();
            Expression operand = parseBoolComparison();
            return new UnaryExpr(UnaryExpr.Op.NOT, operand, bangToken.getLineNumber());
        }

        // ── bare boolean literals ────────────────────────────────
        if (peek().is(TokenType.TRUE)) {
            Token t = advance();
            return new IntLitExpr(1, t.getLineNumber());
        }
        if (peek().is(TokenType.FALSE)) {
            Token t = advance();
            return new IntLitExpr(0, t.getLineNumber());
        }

        // ── relational: <expr> <rel_op> <expr> ──────────────────
        Expression left = parseExpr();

        if (peek().isAny(TokenType.EQ, TokenType.NEQ,
                TokenType.LT, TokenType.LTE,
                TokenType.GT, TokenType.GTE)) {
            Token opToken = advance();
            BoolExpr.Op op = relOp(opToken.getType());
            Expression right = parseExpr();
            return new BoolExpr(op, left, right, opToken.getLineNumber());
        }

        // ── no relational operator found ─────────────────────────
        throw parseError(peek(),
                "expected a relational operator (==, !=, <, <=, >, >=) "
                        + "but found '" + peek().getValue() + "'");
    }

    // ════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ════════════════════════════════════════════════════════════

    // Returns the current token without consuming it.
    protected Token peek() {
        return tokens.get(pos);
    }

    // Returns the current token and advances pos by one.
    protected Token advance() {
        Token t = tokens.get(pos);
        if (!t.is(TokenType.EOF))
            pos++;
        return t;
    }

    // Consumes the current token if it matches the expected type.
    // Returns the matched token.
    // Throws RuntimeException with line number if it does not match.
    protected Token consume(TokenType expected, String errorMessage) {
        if (peek().is(expected)) {
            return advance();
        }
        throw parseError(peek(), errorMessage);
    }

    // Returns true and advances if the current token matches.
    // Returns false and does nothing otherwise.
    protected boolean match(TokenType type) {
        if (peek().is(type)) {
            advance();
            return true;
        }
        return false;
    }

    // Returns true if the current token is EOF.
    protected boolean isAtEnd() {
        return peek().is(TokenType.EOF);
    }

    // ════════════════════════════════════════════════════════════
    // ERROR FACTORY
    // ════════════════════════════════════════════════════════════

    protected RuntimeException parseError(Token token, String message) {
        return new RuntimeException(
                "[Parser error] Line " + token.getLineNumber() + ": " + message);
    }

    // ════════════════════════════════════════════════════════════
    // PRIVATE UTILITIES
    // ════════════════════════════════════════════════════════════

    // Maps a relational TokenType to the corresponding BoolExpr.Op.
    private BoolExpr.Op relOp(TokenType type) {
        switch (type) {
            case EQ:
                return BoolExpr.Op.EQ;
            case NEQ:
                return BoolExpr.Op.NEQ;
            case LT:
                return BoolExpr.Op.LT;
            case LTE:
                return BoolExpr.Op.LTE;
            case GT:
                return BoolExpr.Op.GT;
            case GTE:
                return BoolExpr.Op.GTE;
            default:
                throw new IllegalStateException("relOp() called with non-relational type: " + type);
        }
    }

    // Ensures parseBoolExpr() always returns a BoolExpr, not a bare Expression.
    // This guards against a caller passing a lone arithmetic expression where
    // a boolean condition is required.
    private BoolExpr guardBoolExpr(Expression expr, int line) {
        if (expr instanceof BoolExpr)
            return (BoolExpr) expr;
        throw new RuntimeException(
                "[Parser error] Line " + line
                        + ": expected a boolean expression (comparison or logical operator)");
    }
}