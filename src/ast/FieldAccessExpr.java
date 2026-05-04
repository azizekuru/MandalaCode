// FieldAccessExpr.java
package ast;

public class FieldAccessExpr extends Expression {
    private final String objectName;
    private final String fieldName;

    public FieldAccessExpr(String objectName, String fieldName, int line) {
        super(line);
        this.objectName = objectName;
        this.fieldName = fieldName;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String dump(String indent) {
        return indent + "FieldAccess(" + objectName + "." + fieldName + ") [line " + getLine() + "]";
    }
}