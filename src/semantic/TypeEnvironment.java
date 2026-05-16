// TypeEnvironment.java
package semantic;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class TypeEnvironment {

    // Each frame is a map from name → declared type string.
    // The deque is used as a stack: front = innermost scope.
    private final Deque<Map<String, String>> scopeStack = new ArrayDeque<>();

    // ── Scope management ─────────────────────────────────────────

    // Call when entering a new block (pattern body, radial_repeat, if/else).
    public void enterScope() {
        scopeStack.push(new HashMap<>());
    }

    // Call when leaving a block.
    public void exitScope() {
        if (scopeStack.isEmpty()) {
            throw new IllegalStateException(
                    "[TypeEnvironment] exitScope() called on empty scope stack.");
        }
        scopeStack.pop();
    }

    // ── Variable declaration ──────────────────────────────────────

    // Binds 'name' to 'type' in the current (innermost) scope.
    // Throws if the name is already declared in this exact scope
    // (shadowing an outer scope is allowed — static scoping).
    public void declare(String name, String type, int line) {
        if (scopeStack.isEmpty()) {
            throw new IllegalStateException(
                    "[TypeEnvironment] declare() called with no active scope.");
        }
        Map<String, String> current = scopeStack.peek();
        if (current.containsKey(name)) {
            throw new TypeException(
                    "variable '" + name + "' is already declared in this scope", line);
        }
        current.put(name, type);
    }

    // ── Variable lookup ───────────────────────────────────────────

    // Walks the scope stack from innermost to outermost.
    // Returns the declared type string if found.
    // Throws if the name is not declared in any enclosing scope.
    public String lookup(String name, int line) {
        for (Map<String, String> frame : scopeStack) {
            if (frame.containsKey(name)) {
                return frame.get(name);
            }
        }
        throw new TypeException(
                "undeclared variable '" + name + "'", line);
    }

    // ── Convenience predicate ─────────────────────────────────────

    public boolean isDeclared(String name) {
        for (Map<String, String> frame : scopeStack) {
            if (frame.containsKey(name))
                return true;
        }
        return false;
    }
}