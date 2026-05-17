// RuntimeEnvironment.java
package interpreter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class RuntimeEnvironment {

    // Each frame maps variable name → runtime Value.
    // Front of the deque is the innermost (current) scope.
    private final Deque<Map<String, Value>> scopeStack = new ArrayDeque<>();

    // ── Scope management ──────────────────────────────────────────

    public void enterScope() {
        scopeStack.push(new HashMap<>());
    }

    public void exitScope() {
        if (scopeStack.isEmpty()) {
            throw new IllegalStateException(
                    "[RuntimeEnvironment] exitScope() called on empty scope stack.");
        }
        scopeStack.pop();
    }

    // ── Binding ───────────────────────────────────────────────────

    // Binds a new name in the current (innermost) scope.
    // Called for variable declarations and pattern parameters.
    public void bind(String name, Value value, int line) {
        if (scopeStack.isEmpty()) {
            throw new RuntimeError(
                    "internal error: no active scope when binding '" + name + "'", line);
        }
        scopeStack.peek().put(name, value);
    }

    // ── Assignment ────────────────────────────────────────────────

    // Walks the stack to find an existing binding and updates it.
    // Static scoping: assignment always updates the frame where
    // the variable was originally declared, not the innermost frame.
    public void assign(String name, Value value, int line) {
        for (Map<String, Value> frame : scopeStack) {
            if (frame.containsKey(name)) {
                frame.put(name, value);
                return;
            }
        }
        throw new RuntimeError(
                "assignment to undeclared variable '" + name + "'", line);
    }

    // ── Lookup ────────────────────────────────────────────────────

    public Value lookup(String name, int line) {
        for (Map<String, Value> frame : scopeStack) {
            if (frame.containsKey(name)) {
                return frame.get(name);
            }
        }
        throw new RuntimeError(
                "undeclared variable '" + name + "'", line);
    }

    public boolean isDeclared(String name) {
        for (Map<String, Value> frame : scopeStack) {
            if (frame.containsKey(name))
                return true;
        }
        return false;
    }
}