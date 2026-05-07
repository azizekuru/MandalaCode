# MandalaCode — Interpreter

A domain-specific language for drawing ASCII mandalas using polar coordinates.
Course project for CSE 341, Gebze Technical University, Spring 2026.

---

## Project Structure
```text
MandalaCode/
├── src/
│   ├── Main.java
│   ├── ast/
│   │   ├── ASTNode.java
│   │   ├── Expression.java
│   │   ├── Statement.java
│   │   ├── PatternParam.java
│   │   ├── ProgramNode.java
│   │   ├── BinOpExpr.java
│   │   ├── BoolExpr.java
│   │   ├── ColorLitExpr.java
│   │   ├── FieldAccessExpr.java
│   │   ├── FloatLitExpr.java
│   │   ├── IdentExpr.java
│   │   ├── IntLitExpr.java
│   │   ├── UnaryExpr.java
│   │   ├── AssignStmt.java
│   │   ├── BrushDeclStmt.java
│   │   ├── DrawStmt.java
│   │   ├── IfStmt.java
│   │   ├── PatternCallStmt.java
│   │   ├── PatternDeclStmt.java
│   │   ├── RadialRepeatStmt.java
│   │   └── VarDeclStmt.java
│   ├── lexer/
│   │   ├── Lexer.java
│   │   ├── Token.java
│   │   └── TokenType.java
│   └── parser/
│       └── Parser.java
└── examples/
    ├── mandala1.mnd
    ├── mandala2.mnd
    ├── mandala3.mnd
    ├── bad1.mnd
    ├── bad2.mnd
    ├── bad3.mnd
    ├── bad4.mnd
    └── bad5.mnd
---

## Build Instructions

No build tool is required. Compile all source files from the `src/` directory:

```bash
cd src
javac -d ../out $(find . -name "*.java")
```

This compiles every `.java` file and places the `.class` files into `out/`.

---

## Run Instructions

From the project root, after compiling:

```bash
java -cp out Main <source.mnd> [flags]
```

### Flags

| Flag | Effect |
|---|---|
| `--dump-ast` | Print the full AST after parsing |
| `--print-tokens` | Print the token stream after lexing |
| `--help` | Show usage information |

---

## Example Commands

```bash
# Parse a program silently — exit 0 means success
java -cp out Main examples/mandala1.mnd

# Print the AST
java -cp out Main examples/mandala1.mnd --dump-ast

# Print both token stream and AST
java -cp out Main examples/mandala1.mnd --print-tokens --dump-ast

# See a lexer error (malformed color literal)
java -cp out Main examples/bad_color.mnd

# See a parser error (missing semicolon)
java -cp out Main examples/bad_syntax.mnd
```

---

## Error Format

All errors are printed to `stderr` and exit with code `1`.
[Lexer error]  Line 4: invalid color literal '#XYZ': expected exactly 6 hexadecimal digits after '#'
[Parser error] Line 7: expected ';' after variable declaration