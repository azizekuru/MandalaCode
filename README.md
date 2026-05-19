# MandalaCode 🌀

A domain-specific language for drawing ASCII mandalas using polar coordinate geometry.
Designed and implemented as a course project for **CSE 341 — Concepts of Programming Languages**, Gebze Technical University, Spring 2026.

---

## Table of Contents
- [Overview](#overview)
- [Language Features](#language-features)
- [Project Structure](#project-structure)
- [Build Instructions](#build-instructions)
- [Run Instructions](#run-instructions)
- [Language Reference](#language-reference)
  - [Primitive Types](#primitive-types)
  - [The brush Type](#the-brush-type)
  - [Variable Declaration](#variable-declaration)
  - [Assignment](#assignment)
  - [Expressions](#expressions)
  - [Control Flow](#control-flow)
  - [Patterns](#patterns)
  - [Draw Statement](#draw-statement)
- [Example Programs](#example-programs)
- [Canvas & Rendering](#canvas--rendering)
- [Error Reference](#error-reference)
- [Compiler Pipeline](#compiler-pipeline)
- [Design Decisions](#design-decisions)

---

## Overview
MandalaCode is a statically-scoped, strongly-typed DSL whose sole purpose is to describe and render symmetric geometric compositions — mandalas — on a 100×50 ASCII canvas. Programs describe brushes, patterns, and repetition rules; the interpreter evaluates them and prints the result directly to the terminal.

The language was designed with two Sebesta evaluation criteria as primary goals:
- **Reliability** — strong typing with no implicit coercion, static scoping, and informative error messages at every stage of the pipeline.
- **Writability** — a small, focused vocabulary of domain constructs (`brush`, `radial_repeat`, `draw`, `pattern`) that map directly onto the concepts a mandala artist thinks in.

Raw numeric performance is knowingly sacrificed; MandalaCode is an interpreted teaching language, not a production renderer.

---

## Language Features
- **Three numeric primitive types** — `int`, `radius`, `float`-literal compatible with both `radius` and `angle`
- **Two domain primitive types** — `angle` and `color` (hex literal `#RRGGBB`)
- **One structured type** — `brush`, a named record with three fields: `radius`, `angle`, and `color`
- **Static (lexical) scoping** with nested block scopes
- **Strong typing** — no implicit coercion between declared types; raw float literals are the only context-sensitive case
- **Name equivalence** for the `brush` type
- **User-defined parameterised patterns** (functions/procedures)
- **Control flow** — `if`/`else` and `radial_repeat`
- **Arithmetic operators** — `+`, `-`, `*`, `/` with standard precedence
- **Relational and logical operators** — `==`, `!=`, `<`, `<=`, `>`, `>=`, `&&`, `||`, `!` with short-circuit evaluation
- **ASCII canvas renderer** — 100×50 grid with polar-to-Cartesian conversion and aspect-ratio correction

---

## Project Structure
```
MandalaCode/
├── src/
│   ├── Main.java                      Entry point and CLI
│   ├── ast/
│   │   ├── ASTNode.java               Abstract base — carries line number, dump()
│   │   ├── Expression.java            Abstract expression base
│   │   ├── Statement.java             Abstract statement base
│   │   ├── PatternParam.java          Formal parameter data carrier
│   │   ├── ProgramNode.java           Root of the AST
│   │   ├── BinOpExpr.java             Binary arithmetic: + - * /
│   │   ├── BoolExpr.java              Boolean/relational: == != < <= > >= && ||
│   │   ├── BoolLitExpr.java           Boolean literals: true / false
│   │   ├── ColorLitExpr.java          Color literal: #RRGGBB
│   │   ├── FieldAccessExpr.java       Field access: brush.radius etc.
│   │   ├── FloatLitExpr.java          Float literal: 3.14
│   │   ├── IdentExpr.java             Variable reference
│   │   ├── IntLitExpr.java            Integer literal: 42
│   │   ├── UnaryExpr.java             Unary: - and !
│   │   ├── AssignStmt.java            Assignment: x = expr;
│   │   ├── BrushDeclStmt.java         Brush declaration
│   │   ├── DrawStmt.java              Draw statement (named or inline)
│   │   ├── IfStmt.java                If / else
│   │   ├── PatternCallStmt.java       Pattern call: name(args);
│   │   ├── PatternDeclStmt.java       Pattern declaration
│   │   ├── RadialRepeatStmt.java      radial_repeat(n) { ... }
│   │   └── VarDeclStmt.java           Variable declaration: let x : T = expr;
│   ├── lexer/
│   │   ├── Lexer.java                 Hand-written lexer
│   │   ├── Token.java                 Token with type, value, line number
│   │   └── TokenType.java             Enum of all token categories
│   ├── parser/
│   │   └── Parser.java                Recursive descent parser
│   ├── semantic/
│   │   ├── TypeChecker.java           AST-walking type checker
│   │   ├── TypeEnvironment.java       Scope stack: name → type string
│   │   └── TypeException.java         Type error with line number
│   └── interpreter/
│       ├── Interpreter.java           Tree-walking interpreter
│       ├── RuntimeEnvironment.java    Scope stack: name → Value
│       ├── RuntimeError.java          Runtime error with line number
│       ├── Canvas.java                100×50 ASCII canvas renderer
│       ├── Value.java                 Abstract runtime value base
│       ├── IntValue.java              Runtime integer
│       ├── FloatValue.java            Runtime real (radius and angle)
│       ├── BoolValue.java             Runtime boolean (singleton TRUE/FALSE)
│       ├── ColorValue.java            Runtime color with parsed RGB components
│       ├── BrushValue.java            Runtime brush record
│       └── PatternValue.java          Runtime pattern (wraps PatternDeclStmt)
└── examples/
    ├── mandala1.mnd                   8-petal symmetric ring
    ├── mandala2.mnd                   Layered mandala with if/else and patterns
    └── mandala3.mnd                   Multi-ring mandala with computed geometry
```

---

## Build Instructions

No build tool is required. Compile all source files from the `src/` directory using the standard `javac` compiler (Java 8 or later).

```bash
cd src
javac -d ../out $(find . -name "*.java")
```

On Windows (PowerShell):

```powershell
cd src
Get-ChildItem -Recurse -Filter "*.java" | ForEach-Object { $_.FullName } | ForEach-Object { javac -d ..\out $_ }
```

Or compile all at once on Windows:

```powershell
javac -d ..\out (Get-ChildItem -Recurse -Filter "*.java").FullName
```

All `.class` files are placed into the `out/` directory.

---

## Run Instructions

From the project root, after compiling:

```bash
java -cp out Main <source.mnd> [flags]
```

### CLI Flags

| Flag | Effect |
|------|--------|
| `--dump-ast` | Print the full AST after parsing |
| `--print-tokens` | Print the token stream after lexing |
| `--parse-only` | Stop after parsing — no type check or execution |
| `--type-check-only` | Stop after type checking — no execution |
| `--skip-type-check` | Skip type checking and execute directly |
| `--help` | Show usage information |

### Exit Codes

| Code | Meaning |
|------|---------|
| 0 | Success |
| 1 | Lexer or parser error |
| 2 | Type error |
| 3 | Runtime error |

### Example Commands

```bash
# Run a program end-to-end
java -cp out Main examples/mandala1.mnd

# Print the AST then execute
java -cp out Main examples/mandala1.mnd --dump-ast

# Type-check only — useful for testing error programs
java -cp out Main examples/bad_type.mnd --type-check-only

# Print token stream and AST without executing
java -cp out Main examples/mandala1.mnd --print-tokens --dump-ast
```

---

## Language Reference

### Primitive Types

| Type | Description | Example literal |
|------|-------------|-----------------|
| `int` | Whole number | `8` |
| `radius` | Real-valued distance | `20.0` |
| `angle` | Real-valued degrees | `45.0` |
| `color` | Hex RGB color | `#FF4500` |
| `bool` | Boolean | `true`, `false` |

Float literals (`3.14`, `360.0`) are context-sensitive: they are assignable to `radius` or `angle` variables but not to `int`. This is the only flexibility in an otherwise strict type system with no implicit coercion.

### The brush Type

A `brush` is the only structured type in MandalaCode. It is a named record with exactly three fields. MandalaCode uses name equivalence for brushes — two brush declarations with identical fields are still distinct types if they have different names.

```
brush myBrush {
    radius: 20.0,
    angle:  0.0,
    color:  #FF4500
}
```

Fields are accessed with dot notation: `myBrush.radius`, `myBrush.angle`, `myBrush.color`.

### Variable Declaration

```
let <name> : <type> = <expr> ;
```

Variables are stack-dynamic: they are created when their declaration is executed and destroyed when their enclosing block exits. All variables must be explicitly typed.

```
let segments : int    = 8;
let r        : radius = 20.0;
let a        : angle  = 0.0;
let visible  : bool   = true;
```

### Assignment

```
<name> = <expr> ;
```

Assignment updates the binding in the scope where the variable was originally declared — not necessarily the innermost scope. This is a consequence of static scoping: a variable declared outside a `radial_repeat` block can be mutated inside it, and the updated value persists after the block exits.

```
a = a + 45.0;
```

### Expressions

**Arithmetic — `+`, `-`, `*`, `/`**

Operands must be the same numeric type (`int`, `radius`, or `angle`). A raw float literal is compatible with `radius` or `angle` operands. Division by zero throws a runtime error.

**Relational — `==`, `!=`, `<`, `<=`, `>`, `>=`**

Both operands must be the same type. Relational comparisons on numeric types produce `bool`.

**Logical — `&&`, `||`, `!`**

Both operands must be `bool`. `&&` and `||` use short-circuit evaluation: the right operand is not evaluated if the result is determined by the left.

**Precedence (high to low)**

| Level | Operators |
|-------|-----------|
| 1 (highest) | Unary `-`, `!` |
| 2 | `*`, `/` |
| 3 | `+`, `-` |
| 4 | `==`, `!=`, `<`, `<=`, `>`, `>=` |
| 5 | `&&` |
| 6 (lowest) | `\|\|` |

All binary operators are left-associative.

### Control Flow

**if / else**

```
if (<bool_expr>) {
    <stmts>
} else {
    <stmts>
}
```

The `else` branch is optional. Dangling-else ambiguity is resolved by eager-else: an `else` always binds to the nearest unmatched `if`.

**radial_repeat**

```
radial_repeat(<int_expr>) {
    <stmts>
}
```

Executes the block body exactly `n` times, where `n` is the value of the count expression evaluated once before the loop begins. The count must be of type `int`. A new scope is opened for each iteration and closed when it exits.

The canonical pattern for drawing a symmetric ring is to declare a mutable angle variable before the loop and advance it each iteration:

```
let a    : angle = 0.0;
let step : angle = 360.0 / segments;

radial_repeat(segments) {
    draw brush { radius: r, angle: a, color: #FF4500 };
    a = a + step;
}
```

### Patterns

Patterns are named, parameterised subroutines — the MandalaCode equivalent of functions. They are declared at the top level and called as statements.

```
pattern <name>(<param> : <type>, ...) {
    <stmts>
}
```

Call:

```
<name>(<expr>, ...) ;
```

Parameters are passed by value. Arguments are evaluated in the caller's scope before the callee's scope is opened, preventing parameter-name capture. Patterns cannot return a value — they produce side effects on the canvas.

```
pattern buildRing(size : radius, count : int, col : color) {
    let a    : angle = 0.0;
    let step : angle = 360.0 / count;
    
    radial_repeat(count) {
        draw brush { radius: size, angle: a, color: col };
        a = a + step;
    }
}

buildRing(15.0, 12, #E63946);
```

### Draw Statement

Named brush:

```
draw <brushName> ;
```

Inline brush:

```
draw brush { radius: <expr>, angle: <expr>, color: <color_lit> } ;
```

`draw` plots a single `*` character on the canvas at the polar coordinates given by the brush's `radius` and `angle` fields. The `color` field is type-checked but not used by the ASCII renderer.

---

## Example Programs

### mandala1.mnd — 8-petal ring

```
let r        : radius = 20.0;
let a        : angle  = 0.0;
let segments : int    = 8;

radial_repeat(segments) {
    draw brush { radius: r, angle: a, color: #FF4500 };
    a = a + 45.0;
}
```

### mandala2.mnd — layered mandala with pattern and if/else

```
brush innerRing { radius: 15.0, angle: 0.0, color: #00BFFF }
brush outerRing { radius: 20.0, angle: 0.0, color: #FFD700 }

pattern drawLayer(layerIndex : int, spacing : radius) {
    if (layerIndex == 1) {
        draw innerRing;
    } else {
        draw outerRing;
    }
    
    let adjustedRadius : radius = spacing * 2.0;
    let a    : angle = 0.0;
    let step : angle = 60.0;
    
    radial_repeat(6) {
        draw brush { radius: adjustedRadius, angle: a, color: #9400D3 };
        a = a + step;
    }
}

drawLayer(1, 5.0);
drawLayer(2, 8.0);
```

### mandala3.mnd — multi-ring mandala with computed geometry

```
let baseSize  : radius = 15.0;
let increment : radius = 5.0;
let petals    : int    = 12;

pattern buildRing(size : radius, count : int, col : color) {
    let a    : angle = 0.0;
    let step : angle = 360.0 / count;
    
    radial_repeat(count) {
        draw brush { radius: size, angle: a, color: col };
        a = a + step;
    }
}

buildRing(baseSize, petals, #E63946);
buildRing(baseSize + increment, petals / 2, #457B9D);
buildRing(baseSize + increment * 2.0, petals / 3, #2A9D8F);

let showCenter : bool = true;
if (showCenter == true) {
    draw brush { radius: 5.0, angle: 0.0, color: #F1FAEE };
}
```

---

## Canvas & Rendering

The canvas is a 100×50 grid of characters. The center cell (50, 25) maps to the Cartesian origin (0, 0).

Polar to grid conversion:

```
cartX = radius × cos(angle°)
cartY = radius × sin(angle°)

col   = 50 + round(cartX × 2.0)    ← aspect-ratio correction
row   = 25 − round(cartY)          ← row axis is inverted
```

The `× 2.0` factor compensates for the 2:1 height-to-width aspect ratio of monospace terminal characters, ensuring circles render as circles rather than vertical ellipses.

Points that fall outside the grid boundary are silently clipped — this is a rendering limitation, not a semantic error. Keep `radius` values at or below `20.0` for reliable on-canvas output with the default grid size.

The origin is marked with `+` after all drawing is complete. All other cells are initialized to `.`.

---

## Error Reference

All errors are printed to `stderr`. Each error includes its stage and the source line number.

| Stage | Format | Exit code |
|-------|--------|-----------|
| Lexer | `[Lexer error] Line N: message` | 1 |
| Parser | `[Parser error] Line N: message` | 1 |
| Type checker | `[Type error] Line N: message` | 2 |
| Interpreter | `[Runtime error] Line N: message` | 3 |

Common errors:

```
[Lexer error]  Line 7: invalid color literal '#XYZ': expected exactly
6 hexadecimal digits after '#' (e.g. #FF8800)

[Lexer error]  Line 4: unrecognized character '@'

[Parser error] Line 4: expected ';' after variable declaration

[Parser error] Line 12: expected ')' after argument list

[Type error]   Line 6: type mismatch in declaration of 'a':
declared as 'angle' but initializer has type 'int'

[Type error]   Line 9: operator ADD: operand type mismatch —
left is 'radius', right is 'angle'.
MandalaCode does not perform implicit type coercion.

[Runtime error] Line 14: division by zero

[Runtime error] Line 8: pattern 'buildRing' expects 3 argument(s)
but got 2
```

---

## Compiler Pipeline

```
Source file (.mnd)
       │
       ▼
[ Lexer ]  →  List<Token>          (exit 1 on lex error)
       │
       ▼
[ Parser ]  →  ProgramNode (AST)   (exit 1 on parse error)
       │
       ▼
[ TypeChecker ]  →  (void)         (exit 2 on type error)
       │
       ▼
[ Interpreter ]  →  Canvas output  (exit 3 on runtime error)
       │
       ▼
ASCII canvas printed to stdout     (exit 0)
```

Each stage runs to completion before the next begins. A type error is always caught before execution starts — the interpreter never runs on a type-incorrect program unless `--skip-type-check` is explicitly passed.

---

## Design Decisions

**Static scoping.** Variable lookup always walks the lexical scope chain established at definition time, not at call time. This makes every variable reference in a program traceable by reading the source alone.

**Strong typing, no implicit coercion.** The only flexibility is that raw float literals (`3.14`, `360.0`) are assignable to both `radius` and `angle` variables. No other cross-type assignment is permitted. This catches unit errors — accidentally using an angle where a radius is expected — at compile time.

**Name equivalence for brush.** Two brush declarations with identical fields are still distinct types. A `lens { radius, angle, color }` cannot be passed to `draw` even though its structure matches `brush`. This prevents accidental structural aliasing and preserves programmer intent at the type level.

**Assignment is a statement, not an expression.** Writing `a = b = 0.0` is a parse error. This eliminates the class of bugs caused by assignment in conditional expressions.

**`radial_repeat` count is evaluated once.** The count expression is evaluated before the loop body executes and is not re-evaluated each iteration. This gives the loop a fixed, predictable iteration count and avoids infinite-loop risks from a mutable count variable.

**Short-circuit boolean evaluation.** `&&` and `||` do not evaluate their right operand if the result is determined by the left. This makes guards like `count > 0 && 360.0 / count > 10.0` safe — division by zero on the right is never reached when `count` is zero.

---

*MandalaCode — CSE 341 Course Project, Gebze Technical University, Spring 2026.*
