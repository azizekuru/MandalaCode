// TokenType.java
package lexer;

public enum TokenType {

    // ─── KEYWORDS ───────────────────────────────────────────────
    BRUSH, // brush
    PATTERN, // pattern
    DRAW, // draw
    RADIAL_REPEAT, // radial_repeat
    LET, // let
    IF, // else
    ELSE, // else
    TRUE, // true
    FALSE, // false

    // ─── TYPES ──────────────────────────────────────────────────
    TYPE_ANGLE, // angle
    TYPE_RADIUS, // radius
    TYPE_COLOR, // color
    TYPE_INT, // int
    TYPE_BOOL, // bool
    TYPE_BRUSH, // brush — as a type annotation (same lexeme, context-resolved in parser)

    // ─── LITERALS ───────────────────────────────────────────────
    INT_LIT, // e.g. 42
    FLOAT_LIT, // e.g. 3.14
    COLOR_LIT, // e.g. #FF8800

    // ─── IDENTIFIERS ────────────────────────────────────────────
    IDENT, // e.g. myBrush, petalCount

    // ─── MATH OPERATORS ─────────────────────────────────────────
    PLUS, // +
    MINUS, // -
    STAR, // *
    SLASH, // /

    // ─── RELATIONAL OPERATORS ───────────────────────────────────
    EQ, // ==
    NEQ, // !=
    LT, //
    LTE, // <=
    GT, // >
    GTE, // >=

    // ─── LOGICAL OPERATORS ──────────────────────────────────────
    AND, // &&
    OR, // ||
    BANG, // !

    // ─── ASSIGNMENT ─────────────────────────────────────────────
    ASSIGN, // =

    // ─── SEPARATORS ─────────────────────────────────────────────
    LPAREN, // (
    RPAREN, // )
    LBRACE, // {
    RBRACE, // }
    COMMA, // ,
    COLON, // :
    SEMICOLON, // ;
    DOT, // .

    // ─── SPECIAL ────────────────────────────────────────────────
    EOF // end of source
}