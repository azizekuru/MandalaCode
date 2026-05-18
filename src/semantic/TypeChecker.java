package semantic;

import ast.*;

public class TypeChecker {

    // ── Constants for primitive types ─────────────────────────────
    private static final String T_INT = "int";
    private static final String T_RADIUS = "radius";
    private static final String T_ANGLE = "angle";
    private static final String T_COLOR = "color";
    private static final String T_BOOL = "bool";
    private static final String T_BRUSH = "brush";
    private static final String T_FLOAT = "float"; // Internal only

    private final TypeEnvironment env = new TypeEnvironment();

    // ════════════════════════════════════════════════════════════
    // PUBLIC ENTRY POINT
    // ════════════════════════════════════════════════════════════
    public void check(ProgramNode program) {
        env.enterScope();
        for (Statement stmt : program.getTopLevel()) {
            checkStatement(stmt);
        }
        env.exitScope();
    }

    // ════════════════════════════════════════════════════════════
    // STATEMENT DISPATCH
    // ════════════════════════════════════════════════════════════
    private void checkStatement(Statement stmt) {
        if (stmt instanceof VarDeclStmt) {
            checkVarDecl((VarDeclStmt) stmt);
            return;
        }
        if (stmt instanceof AssignStmt) {
            checkAssign((AssignStmt) stmt);
            return;
        }
        if (stmt instanceof BrushDeclStmt) {
            checkBrushDecl((BrushDeclStmt) stmt);
            return;
        }
        if (stmt instanceof DrawStmt) {
            checkDraw((DrawStmt) stmt);
            return;
        }
        if (stmt instanceof RadialRepeatStmt) {
            checkRadialRepeat((RadialRepeatStmt) stmt);
            return;
        }
        if (stmt instanceof IfStmt) {
            checkIf((IfStmt) stmt);
            return;
        }
        if (stmt instanceof PatternDeclStmt) {
            checkPatternDecl((PatternDeclStmt) stmt);
            return;
        }
        if (stmt instanceof PatternCallStmt) {
            checkPatternCall((PatternCallStmt) stmt);
            return;
        }

        throw new IllegalStateException(
                "[TypeChecker] Unhandled statement type: "
                        + stmt.getClass().getSimpleName());
    }

    // ════════════════════════════════════════════════════════════
    // STATEMENT TYPE-CHECKING
    // ════════════════════════════════════════════════════════════
    private void checkVarDecl(VarDeclStmt stmt) {
        String declared = stmt.getType();
        String actual = inferExpr(stmt.getInitializer());

        if (!isAssignable(declared, actual)) {
            throw new TypeException(
                    "type mismatch in declaration of '" + stmt.getName()
                            + "': declared as '" + declared
                            + "' but initializer has type '" + displayType(actual) + "'",
                    stmt.getLine());
        }
        env.declare(stmt.getName(), declared, stmt.getLine());
    }

    private void checkAssign(AssignStmt stmt) {
        String declared = env.lookup(stmt.getName(), stmt.getLine());
        String actual = inferExpr(stmt.getValue());

        if (!isAssignable(declared, actual)) {
            throw new TypeException(
                    "type mismatch in assignment to '" + stmt.getName()
                            + "': variable is '" + declared
                            + "' but expression has type '" + displayType(actual) + "'",
                    stmt.getLine());
        }
    }

    private void checkBrushDecl(BrushDeclStmt stmt) {
        String radiusType = inferExpr(stmt.getRadius());
        if (!isNumeric(radiusType)) {
            throw new TypeException(
                    "brush '" + stmt.getName()
                            + "': 'radius' field must be a numeric type "
                            + "but got '" + displayType(radiusType) + "'",
                    stmt.getLine());
        }

        String angleType = inferExpr(stmt.getAngle());
        if (!isNumeric(angleType)) {
            throw new TypeException(
                    "brush '" + stmt.getName()
                            + "': 'angle' field must be a numeric type "
                            + "but got '" + displayType(angleType) + "'",
                    stmt.getLine());
        }

        String colorType = inferExpr(stmt.getColor());
        if (!colorType.equals(T_COLOR)) {
            throw new TypeException(
                    "brush '" + stmt.getName()
                            + "': 'color' field must be of type 'color' "
                            + "but got '" + displayType(colorType) + "'",
                    stmt.getLine());
        }

        env.declare(stmt.getName(), T_BRUSH, stmt.getLine());
    }

    private void checkDraw(DrawStmt stmt) {
        if (stmt.isNamed()) {
            String type = env.lookup(stmt.getBrushName(), stmt.getLine());
            if (!type.equals(T_BRUSH)) {
                throw new TypeException(
                        "draw: '" + stmt.getBrushName()
                                + "' is not a brush (found type '" + type + "')",
                        stmt.getLine());
            }
        } else {
            String radiusType = inferExpr(stmt.getRadius());
            if (!isNumeric(radiusType)) {
                throw new TypeException("inline draw: 'radius' must be numeric, got '" + displayType(radiusType) + "'",
                        stmt.getLine());
            }
            String angleType = inferExpr(stmt.getAngle());
            if (!isNumeric(angleType)) {
                throw new TypeException("inline draw: 'angle' must be numeric, got '" + displayType(angleType) + "'",
                        stmt.getLine());
            }
            String colorType = inferExpr(stmt.getColor());
            if (!colorType.equals(T_COLOR)) {
                throw new TypeException(
                        "inline draw: 'color' must be of type 'color', got '" + displayType(colorType) + "'",
                        stmt.getLine());
            }
        }
    }

    private void checkRadialRepeat(RadialRepeatStmt stmt) {
        String countType = inferExpr(stmt.getCount());
        if (!countType.equals(T_INT)) {
            throw new TypeException(
                    "radial_repeat: count must be of type 'int' but got '"
                            + displayType(countType) + "'",
                    stmt.getLine());
        }

        env.enterScope();
        for (Statement s : stmt.getBody())
            checkStatement(s);
        env.exitScope();
    }

    private void checkIf(IfStmt stmt) {
        String condType = inferExpr(stmt.getCondition());
        if (!condType.equals(T_BOOL)) {
            throw new TypeException(
                    "if condition must be of type 'bool' but got '"
                            + displayType(condType) + "'",
                    stmt.getLine());
        }

        env.enterScope();
        for (Statement s : stmt.getThenBlock())
            checkStatement(s);
        env.exitScope();

        if (stmt.hasElse()) {
            env.enterScope();
            for (Statement s : stmt.getElseBlock())
                checkStatement(s);
            env.exitScope();
        }
    }

    private void checkPatternDecl(PatternDeclStmt stmt) {
        env.declare(stmt.getName(), "pattern", stmt.getLine());
        env.enterScope();
        for (PatternParam p : stmt.getParams()) {
            env.declare(p.getName(), p.getType(), p.getLine());
        }
        for (Statement s : stmt.getBody())
            checkStatement(s);
        env.exitScope();
    }

    private void checkPatternCall(PatternCallStmt stmt) {
        String type = env.lookup(stmt.getCallee(), stmt.getLine());
        if (!type.equals("pattern")) {
            throw new TypeException(
                    "'" + stmt.getCallee()
                            + "' is not a pattern (found type '" + type + "')",
                    stmt.getLine());
        }
        for (Expression arg : stmt.getArgs())
            inferExpr(arg);
    }

    // ════════════════════════════════════════════════════════════
    // EXPRESSION TYPE INFERENCE
    // ════════════════════════════════════════════════════════════
    private String inferExpr(Expression expr) {
        if (expr instanceof IntLitExpr)
            return inferIntLit((IntLitExpr) expr);
        if (expr instanceof BoolLitExpr)
            return T_BOOL;
        if (expr instanceof FloatLitExpr)
            return inferFloatLit((FloatLitExpr) expr);
        if (expr instanceof ColorLitExpr)
            return T_COLOR;
        if (expr instanceof IdentExpr)
            return inferIdent((IdentExpr) expr);
        if (expr instanceof FieldAccessExpr)
            return inferFieldAccess((FieldAccessExpr) expr);
        if (expr instanceof BinOpExpr)
            return inferBinOp((BinOpExpr) expr);
        if (expr instanceof UnaryExpr)
            return inferUnary((UnaryExpr) expr);
        if (expr instanceof BoolExpr)
            return inferBoolExpr((BoolExpr) expr);

        throw new IllegalStateException(
                "[TypeChecker] Unhandled expression type: "
                        + expr.getClass().getSimpleName());
    }

    private String inferIntLit(IntLitExpr expr) {
        return T_INT;
    }

    private String inferFloatLit(FloatLitExpr expr) {
        return T_FLOAT;
    }

    private String inferIdent(IdentExpr expr) {
        return env.lookup(expr.getName(), expr.getLine());
    }

    private String inferFieldAccess(FieldAccessExpr expr) {
        String objectType = env.lookup(expr.getObjectName(), expr.getLine());
        if (!objectType.equals(T_BRUSH)) {
            throw new TypeException(
                    "field access '." + expr.getFieldName()
                            + "' is only valid on type 'brush', but '"
                            + expr.getObjectName() + "' has type '" + objectType + "'",
                    expr.getLine());
        }
        switch (expr.getFieldName()) {
            case "radius":
                return T_RADIUS;
            case "angle":
                return T_ANGLE;
            case "color":
                return T_COLOR;
            default:
                throw new TypeException(
                        "brush has no field '" + expr.getFieldName()
                                + "' — valid fields are: radius, angle, color",
                        expr.getLine());
        }
    }

    private String inferBinOp(BinOpExpr expr) {
        String left = inferExpr(expr.getLeft());
        String right = inferExpr(expr.getRight());

        if (!isNumeric(left)) {
            throw new TypeException("operator " + expr.getOp() + ": left operand has type '" + displayType(left)
                    + "' which is not allowed in arithmetic", expr.getLine());
        }
        if (!isNumeric(right)) {
            throw new TypeException("operator " + expr.getOp() + ": right operand has type '" + displayType(right)
                    + "' which is not allowed in arithmetic", expr.getLine());
        }

        if (left.equals(right))
            return left;

        // YENİ KURAL ÜSTTE OLMALI: float literal ve int işleme girerse sonuç float
        // literal kalır.
        if (left.equals(T_FLOAT) && right.equals(T_INT))
            return T_FLOAT;
        if (right.equals(T_FLOAT) && left.equals(T_INT))
            return T_FLOAT;

        // ESKİ KURAL ALTTA OLMALI: float literal ile angle veya radius girerse onların
        // tipine dönüşür.
        if (left.equals(T_FLOAT) && isSpecificNumeric(right))
            return right;
        if (right.equals(T_FLOAT) && isSpecificNumeric(left))
            return left;

        throw new TypeException(
                "operator " + expr.getOp()
                        + ": operand type mismatch — left is '" + displayType(left)
                        + "', right is '" + displayType(right)
                        + "'. MandalaCode does not perform implicit type coercion.",
                expr.getLine());
    }

    private String inferUnary(UnaryExpr expr) {
        String operandType = inferExpr(expr.getOperand());
        switch (expr.getOp()) {
            case NEG:
                if (!isNumeric(operandType)) {
                    throw new TypeException(
                            "unary '-' requires a numeric operand but got '" + displayType(operandType) + "'",
                            expr.getLine());
                }
                return operandType;
            case NOT:
                if (!operandType.equals(T_BOOL)) {
                    throw new TypeException(
                            "unary '!' requires a bool operand but got '" + displayType(operandType) + "'",
                            expr.getLine());
                }
                return T_BOOL;
            default:
                throw new IllegalStateException("Unhandled unary op: " + expr.getOp());
        }
    }

    private String inferBoolExpr(BoolExpr expr) {
        String left = inferExpr(expr.getLeft());
        String right = inferExpr(expr.getRight());

        switch (expr.getOp()) {
            case LT:
            case LTE:
            case GT:
            case GTE:
                if (!isNumeric(left) || !isNumeric(right)) {
                    throw new TypeException("relational comparison requires numeric operands", expr.getLine());
                }
                if (!left.equals(right) && !left.equals(T_FLOAT) && !right.equals(T_FLOAT)) {
                    throw new TypeException(
                            "cannot compare '" + displayType(left) + "' with '" + displayType(right) + "'",
                            expr.getLine());
                }
                return T_BOOL;
            case EQ:
            case NEQ:
                if (!left.equals(right) && !left.equals(T_FLOAT) && !right.equals(T_FLOAT)) {
                    throw new TypeException(
                            "cannot compare '" + displayType(left) + "' with '" + displayType(right) + "'",
                            expr.getLine());
                }
                return T_BOOL;
            case AND:
            case OR:
                if (!left.equals(T_BOOL) || !right.equals(T_BOOL)) {
                    throw new TypeException("logical operators require bool operands", expr.getLine());
                }
                return T_BOOL;
            default:
                throw new IllegalStateException("Unhandled bool op: " + expr.getOp());
        }
    }

    // ════════════════════════════════════════════════════════════
    // UTILITIES
    // ════════════════════════════════════════════════════════════
    private boolean isNumeric(String type) {
        return type.equals(T_INT) || type.equals(T_RADIUS) || type.equals(T_ANGLE) || type.equals(T_FLOAT);
    }

    private boolean isSpecificNumeric(String type) {
        return type.equals(T_INT) || type.equals(T_RADIUS) || type.equals(T_ANGLE);
    }

    private boolean isAssignable(String declared, String actual) {
        if (declared.equals(actual))
            return true;
        if (actual.equals(T_FLOAT) && (declared.equals(T_RADIUS) || declared.equals(T_ANGLE)))
            return true;
        return false;
    }

    private String displayType(String type) {
        return type.equals(T_FLOAT) ? "float literal" : type;
    }
}