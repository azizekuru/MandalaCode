// Parser.java
package parser;

import ast.*;
import lexer.Token;
import lexer.TokenType;

import java.util.ArrayList;
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

    // ════════════════════════════════════════════════════════════
    // STATEMENT PARSING
    // Add these methods to Parser.java, inside the Parser class.
    // Replace the existing parse() stub with the one below.
    // ════════════════════════════════════════════════════════════

    // ── Entry point ──────────────────────────────────────────────

    public ProgramNode parse() {
        List<Statement> topLevel = new ArrayList<>();

        while (!isAtEnd()) {
            topLevel.add(parseTopLevel());
        }

        return new ProgramNode(topLevel);
    }

    // ── Top-level dispatcher ─────────────────────────────────────

    // A top-level item is either a brush declaration, a pattern
    // declaration, or a statement. All three are subtypes of
    // Statement in our AST, so the return type is Statement.
    private Statement parseTopLevel() {
        Token current = peek();

        if (current.is(TokenType.BRUSH))
            return parseBrushDecl();
        if (current.is(TokenType.PATTERN))
            return parsePatternDecl();
        return parseStatement();
    }

    // ── Statement dispatcher ─────────────────────────────────────

    // Dispatches to the correct parse method based on the current
    // token. Called both at the top level and inside blocks.
    private Statement parseStatement() {
        Token current = peek();

        if (current.is(TokenType.LET))
            return parseVarDecl();
        if (current.is(TokenType.IF))
            return parseIfStmt();
        if (current.is(TokenType.DRAW))
            return parseDrawStmt();
        if (current.is(TokenType.RADIAL_REPEAT))
            return parseRadialRepeat();

        // IDENT followed by '=' → assignment
        // IDENT followed by '(' → pattern call
        if (current.is(TokenType.IDENT)) {
            if (tokens.get(pos + 1).is(TokenType.ASSIGN))
                return parseAssignStmt();
            if (tokens.get(pos + 1).is(TokenType.LPAREN))
                return parsePatternCallStmt();
        }

        throw parseError(current,
                "unexpected token '" + current.getValue()
                        + "' — expected a statement");
    }

    // ── Block ─────────────────────────────────────────────────────

    // <block> ::= "{" { <stmt> } "}"
    private List<Statement> parseBlock() {
        consume(TokenType.LBRACE, "expected '{' to open block");
        List<Statement> stmts = new ArrayList<>();

        while (!isAtEnd() && !peek().is(TokenType.RBRACE)) {
            stmts.add(parseStatement());
        }

        consume(TokenType.RBRACE, "expected '}' to close block");
        return stmts;
    }

    // ════════════════════════════════════════════════════════════
    // DECLARATION STATEMENTS
    // ════════════════════════════════════════════════════════════

    // <brush_decl> ::= "brush" IDENT "{"
    // "radius" ":" <expr> ","
    // "angle" ":" <expr> ","
    // "color" ":" COLOR_LIT
    // "}"
    // parseBrushDecl() — corrected color field
    private BrushDeclStmt parseBrushDecl() {
        Token brushToken = consume(TokenType.BRUSH, "expected 'brush'");
        Token nameToken = consume(TokenType.IDENT, "expected brush name after 'brush'");

        consume(TokenType.LBRACE, "expected '{' after brush name '"
                + nameToken.getValue() + "'");

        consume(TokenType.TYPE_RADIUS, "expected 'radius' field in brush body");
        consume(TokenType.COLON, "expected ':' after 'radius'");
        Expression radius = parseExpr();
        consume(TokenType.COMMA, "expected ',' after radius value");

        consume(TokenType.TYPE_ANGLE, "expected 'angle' field in brush body");
        consume(TokenType.COLON, "expected ':' after 'angle'");
        Expression angle = parseExpr();
        consume(TokenType.COMMA, "expected ',' after angle value");

        consume(TokenType.TYPE_COLOR, "expected 'color' field in brush body");
        consume(TokenType.COLON, "expected ':' after 'color'");
        Expression color = parseExpr(); // ← fixed

        consume(TokenType.RBRACE, "expected '}' to close brush declaration '"
                + nameToken.getValue() + "'");

        return new BrushDeclStmt(
                nameToken.getValue(), radius, angle, color,
                brushToken.getLineNumber());
    }

    // <pattern_decl> ::= "pattern" IDENT "(" [ <param_list> ] ")" <block>
    private PatternDeclStmt parsePatternDecl() {
        Token patternToken = consume(TokenType.PATTERN, "expected 'pattern'");
        Token nameToken = consume(TokenType.IDENT, "expected pattern name after 'pattern'");

        consume(TokenType.LPAREN, "expected '(' after pattern name '"
                + nameToken.getValue() + "'");

        List<PatternParam> params = new ArrayList<>();
        if (!peek().is(TokenType.RPAREN)) {
            params = parseParamList();
        }

        consume(TokenType.RPAREN, "expected ')' after parameter list");

        List<Statement> body = parseBlock();

        return new PatternDeclStmt(
                nameToken.getValue(), params, body,
                patternToken.getLineNumber());
    }

    // <param_list> ::= <param> { "," <param> }
    // <param> ::= IDENT ":" <type>
    private List<PatternParam> parseParamList() {
        List<PatternParam> params = new ArrayList<>();
        params.add(parseParam());

        while (peek().is(TokenType.COMMA)) {
            advance(); // consume ','
            params.add(parseParam());
        }

        return params;
    }

    private PatternParam parseParam() {
        Token nameToken = consume(TokenType.IDENT, "expected parameter name");
        consume(TokenType.COLON, "expected ':' after parameter name '"
                + nameToken.getValue() + "'");
        String typeName = parseTypeName();
        return new PatternParam(nameToken.getValue(), typeName, nameToken.getLineNumber());
    }

    // ════════════════════════════════════════════════════════════
    // SIMPLE STATEMENTS
    // ════════════════════════════════════════════════════════════

    // <var_decl> ::= "let" IDENT ":" <type> "=" <expr> ";"
    private VarDeclStmt parseVarDecl() {
        Token letToken = consume(TokenType.LET, "expected 'let'");
        Token nameToken = consume(TokenType.IDENT, "expected variable name after 'let'");

        consume(TokenType.COLON, "expected ':' after variable name '"
                + nameToken.getValue() + "'");

        String typeName = parseTypeName();

        consume(TokenType.ASSIGN, "expected '=' after type in declaration of '"
                + nameToken.getValue() + "'");

        Expression initializer = parseExpr();

        consume(TokenType.SEMICOLON, "expected ';' after variable declaration");

        return new VarDeclStmt(
                nameToken.getValue(), typeName, initializer,
                letToken.getLineNumber());
    }

    // <assign_stmt> ::= IDENT "=" <expr> ";"
    private AssignStmt parseAssignStmt() {
        Token nameToken = consume(TokenType.IDENT, "expected variable name");
        consume(TokenType.ASSIGN, "expected '=' after '"
                + nameToken.getValue() + "'");
        Expression value = parseExpr();
        consume(TokenType.SEMICOLON, "expected ';' after assignment");

        return new AssignStmt(
                nameToken.getValue(), value,
                nameToken.getLineNumber());
    }

    // <draw_stmt> ::= "draw" IDENT ";"
    // | "draw" "brush" "{" ... "}" ";"
    // parseDrawStmt() — corrected inline brush color field
    private DrawStmt parseDrawStmt() {
        Token drawToken = consume(TokenType.DRAW, "expected 'draw'");

        if (peek().is(TokenType.BRUSH)) {
            advance();
            consume(TokenType.LBRACE, "expected '{' after 'draw brush'");

            consume(TokenType.TYPE_RADIUS, "expected 'radius' field in inline brush");
            consume(TokenType.COLON, "expected ':' after 'radius'");
            Expression radius = parseExpr();
            consume(TokenType.COMMA, "expected ',' after radius value");

            consume(TokenType.TYPE_ANGLE, "expected 'angle' field in inline brush");
            consume(TokenType.COLON, "expected ':' after 'angle'");
            Expression angle = parseExpr();
            consume(TokenType.COMMA, "expected ',' after angle value");

            consume(TokenType.TYPE_COLOR, "expected 'color' field in inline brush");
            consume(TokenType.COLON, "expected ':' after 'color'");
            Expression color = parseExpr(); // ← fixed

            consume(TokenType.RBRACE, "expected '}' to close inline brush");
            consume(TokenType.SEMICOLON, "expected ';' after draw statement");

            return new DrawStmt(radius, angle, color, drawToken.getLineNumber());
        }

        Token nameToken = consume(TokenType.IDENT,
                "expected a brush name or 'brush' keyword after 'draw'");
        consume(TokenType.SEMICOLON, "expected ';' after draw statement");

        return new DrawStmt(nameToken.getValue(), drawToken.getLineNumber());
    }

    // <radial_repeat_stmt> ::= "radial_repeat" "(" <expr> ")" <block>
    private RadialRepeatStmt parseRadialRepeat() {
        Token repeatToken = consume(TokenType.RADIAL_REPEAT, "expected 'radial_repeat'");

        consume(TokenType.LPAREN, "expected '(' after 'radial_repeat'");
        Expression count = parseExpr();
        consume(TokenType.RPAREN, "expected ')' after repeat count");

        List<Statement> body = parseBlock();

        return new RadialRepeatStmt(count, body, repeatToken.getLineNumber());
    }

    // <if_stmt> ::= "if" "(" <bool_expr> ")" <block> [ "else" <block> ]
    //
    // Dangling-else resolution: the [ "else" <block> ] is consumed
    // greedily here. When this method sees ELSE it always binds it to
    // the current (nearest) if — the eager-else convention.
    private IfStmt parseIfStmt() {
        Token ifToken = consume(TokenType.IF, "expected 'if'");

        consume(TokenType.LPAREN, "expected '(' after 'if'");
        BoolExpr condition = parseBoolExpr();
        consume(TokenType.RPAREN, "expected ')' after condition");

        List<Statement> thenBlock = parseBlock();

        List<Statement> elseBlock = null;
        if (peek().is(TokenType.ELSE)) {
            advance(); // consume 'else'
            elseBlock = parseBlock();
        }

        return new IfStmt(condition, thenBlock, elseBlock, ifToken.getLineNumber());
    }

    // <pattern_call_stmt> ::= IDENT "(" [ <arg_list> ] ")" ";"
    private PatternCallStmt parsePatternCallStmt() {
        Token nameToken = consume(TokenType.IDENT, "expected pattern name");
        consume(TokenType.LPAREN, "expected '(' after '"
                + nameToken.getValue() + "'");

        List<Expression> args = new ArrayList<>();
        if (!peek().is(TokenType.RPAREN)) {
            args = parseArgList();
        }

        consume(TokenType.RPAREN, "expected ')' after argument list");
        consume(TokenType.SEMICOLON, "expected ';' after pattern call");

        return new PatternCallStmt(
                nameToken.getValue(), args,
                nameToken.getLineNumber());
    }

    // <arg_list> ::= <expr> { "," <expr> }
    private List<Expression> parseArgList() {
        List<Expression> args = new ArrayList<>();
        args.add(parseExpr());

        while (peek().is(TokenType.COMMA)) {
            advance(); // consume ','
            args.add(parseExpr());
        }

        return args;
    }

    // ════════════════════════════════════════════════════════════
    // TYPE NAME HELPER
    // ════════════════════════════════════════════════════════════

    // Consumes one type keyword and returns its string name.
    // Called from parseVarDecl() and parseParam().
    private String parseTypeName() {
        Token t = peek();
        switch (t.getType()) {
            case TYPE_ANGLE:
                advance();
                return "angle";
            case TYPE_RADIUS:
                advance();
                return "radius";
            case TYPE_COLOR:
                advance();
                return "color";
            case TYPE_INT:
                advance();
                return "int";
            case TYPE_BOOL:
                advance();
                return "bool";
            case BRUSH:
                advance();
                return "brush";
            default:
                throw parseError(t,
                        "expected a type name (angle, radius, color, int, bool, brush) "
                                + "but found '" + t.getValue() + "'");
        }
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