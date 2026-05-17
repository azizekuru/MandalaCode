// Interpreter.java
package interpreter;

import ast.*;

import java.util.List;

public class Interpreter {

    private final RuntimeEnvironment env = new RuntimeEnvironment();

    // ════════════════════════════════════════════════════════════
    // PUBLIC ENTRY POINT
    // ════════════════════════════════════════════════════════════

    public void execute(ProgramNode program) {
        env.enterScope();
        for (Statement stmt : program.getTopLevel()) {
            executeStatement(stmt);
        }
        env.exitScope();
    }

    // ════════════════════════════════════════════════════════════
    // STATEMENT DISPATCH
    // ════════════════════════════════════════════════════════════

    private void executeStatement(Statement stmt) {
        if (stmt instanceof VarDeclStmt) {
            executeVarDecl((VarDeclStmt) stmt);
            return;
        }
        if (stmt instanceof AssignStmt) {
            executeAssign((AssignStmt) stmt);
            return;
        }
        if (stmt instanceof BrushDeclStmt) {
            executeBrushDecl((BrushDeclStmt) stmt);
            return;
        }
        if (stmt instanceof DrawStmt) {
            executeDraw((DrawStmt) stmt);
            return;
        }
        if (stmt instanceof RadialRepeatStmt) {
            executeRadialRepeat((RadialRepeatStmt) stmt);
            return;
        }
        if (stmt instanceof IfStmt) {
            executeIf((IfStmt) stmt);
            return;
        }
        if (stmt instanceof PatternDeclStmt) {
            executePatternDecl((PatternDeclStmt) stmt);
            return;
        }
        if (stmt instanceof PatternCallStmt) {
            executePatternCall((PatternCallStmt) stmt);
            return;
        }

        throw new IllegalStateException(
                "[Interpreter] Unhandled statement type: "
                        + stmt.getClass().getSimpleName());
    }

    // ════════════════════════════════════════════════════════════
    // STATEMENT EXECUTION
    // ════════════════════════════════════════════════════════════

    // <var_decl> ::= "let" IDENT ":" <type> "=" <expr> ";"
    private void executeVarDecl(VarDeclStmt stmt) {
        Value value = evaluate(stmt.getInitializer());
        // Coerce FloatValue into the declared domain if needed.
        // The type checker already verified the assignment is legal.
        value = coerceToDeclaration(stmt.getType(), value, stmt.getLine());
        env.bind(stmt.getName(), value, stmt.getLine());
    }

    // <assign_stmt> ::= IDENT "=" <expr> ";"
    private void executeAssign(AssignStmt stmt) {
        Value value = evaluate(stmt.getValue());
        env.assign(stmt.getName(), value, stmt.getLine());
    }

    // ── Remaining statement executors (stubs — expanded next) ────

    private void executeBrushDecl(BrushDeclStmt stmt) {
        double radius = toDouble(evaluate(stmt.getRadius()), stmt.getLine());
        double angle = toDouble(evaluate(stmt.getAngle()), stmt.getLine());
        ColorValue color = toColor(evaluate(stmt.getColor()), stmt.getLine());
        env.bind(stmt.getName(), new BrushValue(radius, angle, color), stmt.getLine());
    }

    private void executeDraw(DrawStmt stmt) {
        // Canvas rendering will be implemented in the next step.
        // For now, print a description so example programs produce visible output.
        if (stmt.isNamed()) {
            Value v = env.lookup(stmt.getBrushName(), stmt.getLine());
            System.out.println("[draw] " + v.display());
        } else {
            double radius = toDouble(evaluate(stmt.getRadius()), stmt.getLine());
            double angle = toDouble(evaluate(stmt.getAngle()), stmt.getLine());
            ColorValue color = toColor(evaluate(stmt.getColor()), stmt.getLine());
            System.out.println("[draw] brush { radius: " + radius
                    + ", angle: " + angle
                    + ", color: " + color.display() + " }");
        }
    }

    private void executeRadialRepeat(RadialRepeatStmt stmt) {
        Value countVal = evaluate(stmt.getCount());
        int count = toInt(countVal, stmt.getLine());
        if (count < 0) {
            throw new RuntimeError(
                    "radial_repeat count must be non-negative, got " + count,
                    stmt.getLine());
        }
        for (int i = 0; i < count; i++) {
            env.enterScope();
            for (Statement s : stmt.getBody())
                executeStatement(s);
            env.exitScope();
        }
    }

    private void executeIf(IfStmt stmt) {
        Value condVal = evaluate(stmt.getCondition());
        boolean cond = toBool(condVal, stmt.getLine());
        if (cond) {
            env.enterScope();
            for (Statement s : stmt.getThenBlock())
                executeStatement(s);
            env.exitScope();
        } else if (stmt.hasElse()) {
            env.enterScope();
            for (Statement s : stmt.getElseBlock())
                executeStatement(s);
            env.exitScope();
        }
    }

    private void executePatternDecl(PatternDeclStmt stmt) {
        // Store the AST node itself as a PatternValue so call sites
        // can retrieve the parameter list and body at call time.
        env.bind(stmt.getName(), new PatternValue(stmt), stmt.getLine());
    }

    private void executePatternCall(PatternCallStmt stmt) {
        Value callee = env.lookup(stmt.getCallee(), stmt.getLine());
        if (!(callee instanceof PatternValue)) {
            throw new RuntimeError(
                    "'" + stmt.getCallee() + "' is not a pattern",
                    stmt.getLine());
        }
        PatternDeclStmt decl = ((PatternValue) callee).getDecl();
        List<PatternParam> params = decl.getParams();
        List<Expression> args = stmt.getArgs();

        if (params.size() != args.size()) {
            throw new RuntimeError(
                    "pattern '" + stmt.getCallee()
                            + "' expects " + params.size()
                            + " argument(s) but got " + args.size(),
                    stmt.getLine());
        }

        // Evaluate all arguments in the caller's scope before
        // opening the callee's scope — avoids parameter-name capture.
        Value[] evaluated = new Value[args.size()];
        for (int i = 0; i < args.size(); i++) {
            evaluated[i] = evaluate(args.get(i));
            evaluated[i] = coerceToDeclaration(
                    params.get(i).getType(), evaluated[i], stmt.getLine());
        }

        env.enterScope();
        for (int i = 0; i < params.size(); i++) {
            env.bind(params.get(i).getName(), evaluated[i], stmt.getLine());
        }
        for (Statement s : decl.getBody())
            executeStatement(s);
        env.exitScope();
    }

    // ════════════════════════════════════════════════════════════
    // EXPRESSION EVALUATION
    // ════════════════════════════════════════════════════════════

    public Value evaluate(Expression expr) {
        if (expr instanceof IntLitExpr)
            return evaluateIntLit((IntLitExpr) expr);
        if (expr instanceof FloatLitExpr)
            return evaluateFloatLit((FloatLitExpr) expr);
        if (expr instanceof ColorLitExpr)
            return evaluateColorLit((ColorLitExpr) expr);
        if (expr instanceof BoolExpr)
            return evaluateBoolExpr((BoolExpr) expr);
        if (expr instanceof IdentExpr)
            return evaluateIdent((IdentExpr) expr);
        if (expr instanceof FieldAccessExpr)
            return evaluateFieldAccess((FieldAccessExpr) expr);
        if (expr instanceof BinOpExpr)
            return evaluateBinOp((BinOpExpr) expr);
        if (expr instanceof UnaryExpr)
            return evaluateUnary((UnaryExpr) expr);

        throw new IllegalStateException(
                "[Interpreter] Unhandled expression type: "
                        + expr.getClass().getSimpleName());
    }

    // ── Literals ──────────────────────────────────────────────────

    private Value evaluateIntLit(IntLitExpr expr) {
        return new IntValue(expr.getValue());
    }

    private Value evaluateFloatLit(FloatLitExpr expr) {
        return new FloatValue(expr.getValue());
    }

    private Value evaluateColorLit(ColorLitExpr expr) {
        return new ColorValue(expr.getHexValue());
    }

    // ── Identifier ────────────────────────────────────────────────

    private Value evaluateIdent(IdentExpr expr) {
        return env.lookup(expr.getName(), expr.getLine());
    }

    // ── Field access ──────────────────────────────────────────────

    private Value evaluateFieldAccess(FieldAccessExpr expr) {
        Value obj = env.lookup(expr.getObjectName(), expr.getLine());
        if (!(obj instanceof BrushValue)) {
            throw new RuntimeError(
                    "field access '." + expr.getFieldName()
                            + "' requires a brush value, but '"
                            + expr.getObjectName() + "' is " + obj.display(),
                    expr.getLine());
        }
        BrushValue brush = (BrushValue) obj;
        switch (expr.getFieldName()) {
            case "radius":
                return new FloatValue(brush.getRadius());
            case "angle":
                return new FloatValue(brush.getAngle());
            case "color":
                return brush.getColor();
            default:
                throw new RuntimeError(
                        "brush has no field '" + expr.getFieldName() + "'",
                        expr.getLine());
        }
    }

    // ── BinOpExpr — arithmetic ────────────────────────────────────

    private Value evaluateBinOp(BinOpExpr expr) {
        Value left = evaluate(expr.getLeft());
        Value right = evaluate(expr.getRight());

        // Unify both sides to double for arithmetic.
        // The type checker already guarantees both sides are numeric.
        double l = toDouble(left, expr.getLine());
        double r = toDouble(right, expr.getLine());

        double result;
        switch (expr.getOp()) {
            case ADD:
                result = l + r;
                break;
            case SUB:
                result = l - r;
                break;
            case MUL:
                result = l * r;
                break;
            case DIV:
                if (r == 0.0) {
                    throw new RuntimeError(
                            "division by zero", expr.getLine());
                }
                result = l / r;
                break;
            default:
                throw new IllegalStateException(
                        "Unhandled BinOp: " + expr.getOp());
        }

        // Preserve int type if both operands were integers and
        // the result has no fractional part.
        if (left instanceof IntValue && right instanceof IntValue
                && result == Math.floor(result)
                && !Double.isInfinite(result)) {
            return new IntValue((int) result);
        }
        return new FloatValue(result);
    }

    // ── UnaryExpr ─────────────────────────────────────────────────

    private Value evaluateUnary(UnaryExpr expr) {
        Value operand = evaluate(expr.getOperand());
        switch (expr.getOp()) {
            case NEG:
                if (operand instanceof IntValue) {
                    return new IntValue(-((IntValue) operand).getValue());
                }
                if (operand instanceof FloatValue) {
                    return new FloatValue(-((FloatValue) operand).getValue());
                }
                throw new RuntimeError(
                        "unary '-' requires a numeric value, got "
                                + operand.display(),
                        expr.getLine());
            case NOT:
                if (operand instanceof BoolValue) {
                    return BoolValue.of(!((BoolValue) operand).getValue());
                }
                throw new RuntimeError(
                        "unary '!' requires a bool value, got "
                                + operand.display(),
                        expr.getLine());
            default:
                throw new IllegalStateException(
                        "Unhandled UnaryOp: " + expr.getOp());
        }
    }

    // ── BoolExpr ──────────────────────────────────────────────────

    private Value evaluateBoolExpr(BoolExpr expr) {
        // Short-circuit evaluation for && and ||.
        if (expr.getOp() == BoolExpr.Op.AND) {
            Value left = evaluate(expr.getLeft());
            if (!toBool(left, expr.getLine()))
                return BoolValue.FALSE;
            return BoolValue.of(toBool(evaluate(expr.getRight()), expr.getLine()));
        }
        if (expr.getOp() == BoolExpr.Op.OR) {
            Value left = evaluate(expr.getLeft());
            if (toBool(left, expr.getLine()))
                return BoolValue.TRUE;
            return BoolValue.of(toBool(evaluate(expr.getRight()), expr.getLine()));
        }

        // All other operators evaluate both sides eagerly.
        Value left = evaluate(expr.getLeft());
        Value right = evaluate(expr.getRight());

        switch (expr.getOp()) {
            case EQ:
                return BoolValue.of(valuesEqual(left, right, expr.getLine()));
            case NEQ:
                return BoolValue.of(!valuesEqual(left, right, expr.getLine()));
            case LT:
                return BoolValue.of(toDouble(left, expr.getLine()) < toDouble(right, expr.getLine()));
            case LTE:
                return BoolValue.of(toDouble(left, expr.getLine()) <= toDouble(right, expr.getLine()));
            case GT:
                return BoolValue.of(toDouble(left, expr.getLine()) > toDouble(right, expr.getLine()));
            case GTE:
                return BoolValue.of(toDouble(left, expr.getLine()) >= toDouble(right, expr.getLine()));
            default:
                throw new IllegalStateException(
                        "Unhandled BoolOp: " + expr.getOp());
        }
    }

    // ════════════════════════════════════════════════════════════
    // COERCION AND EXTRACTION HELPERS
    // ════════════════════════════════════════════════════════════

    // Adjusts a FloatValue to the exact runtime type its declaration
    // demands. This is the runtime counterpart of isAssignable() in
    // the type checker — both must agree on what is permitted.
    private Value coerceToDeclaration(String declaredType, Value value, int line) {
        if (value instanceof FloatValue) {
            switch (declaredType) {
                case "radius":
                case "angle":
                    return value; // FloatValue is already the correct runtime rep
                case "int":
                    throw new RuntimeError(
                            "cannot assign a float literal to an 'int' variable",
                            line);
            }
        }
        return value; // no coercion needed
    }

    // Extracts a double from IntValue or FloatValue.
    // Used to unify operands before arithmetic.
    private double toDouble(Value v, int line) {
        if (v instanceof IntValue)
            return ((IntValue) v).getValue();
        if (v instanceof FloatValue)
            return ((FloatValue) v).getValue();
        throw new RuntimeError(
                "expected a numeric value but got " + v.display(), line);
    }

    // Extracts an int from IntValue only — float is not silently truncated.
    private int toInt(Value v, int line) {
        if (v instanceof IntValue)
            return ((IntValue) v).getValue();
        throw new RuntimeError(
                "expected an int value but got " + v.display(), line);
    }

    // Extracts a boolean from BoolValue.
    private boolean toBool(Value v, int line) {
        if (v instanceof BoolValue)
            return ((BoolValue) v).getValue();
        throw new RuntimeError(
                "expected a bool value but got " + v.display(), line);
    }

    // Extracts a ColorValue — used for brush field validation.
    private ColorValue toColor(Value v, int line) {
        if (v instanceof ColorValue)
            return (ColorValue) v;
        throw new RuntimeError(
                "expected a color value but got " + v.display(), line);
    }

    // Structural equality for == and != operators.
    private boolean valuesEqual(Value a, Value b, int line) {
        if (a instanceof IntValue && b instanceof IntValue)
            return ((IntValue) a).getValue() == ((IntValue) b).getValue();
        if (a instanceof FloatValue && b instanceof FloatValue)
            return ((FloatValue) a).getValue() == ((FloatValue) b).getValue();
        if (a instanceof IntValue && b instanceof FloatValue)
            return ((IntValue) a).getValue() == ((FloatValue) b).getValue();
        if (a instanceof FloatValue && b instanceof IntValue)
            return ((FloatValue) a).getValue() == ((IntValue) b).getValue();
        if (a instanceof BoolValue && b instanceof BoolValue)
            return ((BoolValue) a).getValue() == ((BoolValue) b).getValue();
        if (a instanceof ColorValue && b instanceof ColorValue)
            return ((ColorValue) a).getHex().equals(((ColorValue) b).getHex());
        throw new RuntimeError(
                "cannot compare " + a.display()
                        + " with " + b.display(),
                line);
    }
}