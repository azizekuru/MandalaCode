
// Main.java
import ast.ProgramNode;
import interpreter.Interpreter;
import interpreter.RuntimeError;
import lexer.Lexer;
import lexer.Token;
import parser.Parser;
import semantic.TypeChecker;
import semantic.TypeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    // ════════════════════════════════════════════════════════════
    // ENTRY POINT
    // ════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        CLIArgs cli = parseCLIArgs(args);

        String source = readFile(cli.filePath);

        // ── Stage 1: Lex ─────────────────────────────────────────
        List<Token> tokens = runLexer(source, cli);

        // ── Stage 2: Parse ───────────────────────────────────────
        ProgramNode ast = runParser(tokens, cli);

        // ── Stage 3: Dump AST (if requested) ────────────────────
        if (cli.dumpAst) {
            System.out.println("──────────── AST DUMP ────────────");
            System.out.println(ast.dump(""));
            System.out.println("──────────────────────────────────");
        }

        // ── Stage 4: Type check ──────────────────────────────────
        if (!cli.skipTypeCheck) {
            runTypeChecker(ast, cli);
        }

        // ── Stage 5: Interpret ───────────────────────────────────
        if (!cli.parseOnly && !cli.typeCheckOnly) {
            runInterpreter(ast, cli);
        }

        if (cli.typeCheckOnly) {
            System.out.println("[OK] '" + cli.filePath
                    + "' passed type checking.");
        }
    }

    // ════════════════════════════════════════════════════════════
    // PIPELINE STAGES
    // ════════════════════════════════════════════════════════════

    private static List<Token> runLexer(String source, CLIArgs cli) {
        try {
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenize();

            if (cli.printTokens) {
                System.out.println("──────────── TOKEN STREAM ────────────");
                for (Token t : tokens) {
                    System.out.println("  " + t);
                }
                System.out.println("──────────────────────────────────────");
            }

            return tokens;

        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return null; // unreachable
        }
    }

    private static ProgramNode runParser(List<Token> tokens, CLIArgs cli) {
        try {
            Parser parser = new Parser(tokens);
            ProgramNode ast = parser.parse();

            if (cli.parseOnly) {
                System.out.println("[OK] '" + cli.filePath
                        + "' parsed successfully.");
            }

            return ast;

        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return null; // unreachable
        }
    }

    private static void runTypeChecker(ProgramNode ast, CLIArgs cli) {
        try {
            TypeChecker checker = new TypeChecker();
            checker.check(ast);

        } catch (TypeException e) {
            // TypeException already formats the line number in its message.
            System.err.println(e.getMessage());
            System.exit(2); // exit code 2 = type error (distinct from parse error)
        } catch (RuntimeException e) {
            System.err.println("[Type error] " + e.getMessage());
            System.exit(2);
        }
    }

    private static void runInterpreter(ProgramNode ast, CLIArgs cli) {
        try {
            Interpreter interpreter = new Interpreter();
            interpreter.execute(ast);

            System.out.println("[OK] '" + cli.filePath
                    + "' executed successfully.");

        } catch (RuntimeError e) {
            // RuntimeError already formats the line number in its message.
            System.err.println(e.getMessage());
            System.exit(3); // exit code 3 = runtime error
        } catch (RuntimeException e) {
            System.err.println("[Runtime error] " + e.getMessage());
            System.exit(3);
        }
    }

    // ════════════════════════════════════════════════════════════
    // FILE READING
    // ════════════════════════════════════════════════════════════

    private static String readFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            System.err.println("[Error] Cannot read file '" + path
                    + "': " + e.getMessage());
            System.exit(1);
            return null; // unreachable
        }
    }

    // ════════════════════════════════════════════════════════════
    // CLI ARGUMENT PARSING
    // ════════════════════════════════════════════════════════════

    private static CLIArgs parseCLIArgs(String[] args) {
        CLIArgs cli = new CLIArgs();

        if (args.length == 0) {
            printUsageAndExit();
        }

        for (String arg : args) {
            switch (arg) {
                case "--dump-ast":
                    cli.dumpAst = true;
                    break;
                case "--print-tokens":
                    cli.printTokens = true;
                    break;
                case "--parse-only":
                    cli.parseOnly = true;
                    break;
                case "--type-check-only":
                    cli.typeCheckOnly = true;
                    break;
                case "--skip-type-check":
                    cli.skipTypeCheck = true;
                    break;
                case "--help":
                    printUsageAndExit();
                    break;
                default:
                    if (arg.startsWith("--")) {
                        System.err.println("[Error] Unknown flag '" + arg + "'.");
                        printUsageAndExit();
                    }
                    if (cli.filePath != null) {
                        System.err.println(
                                "[Error] Only one source file may be specified.");
                        printUsageAndExit();
                    }
                    cli.filePath = arg;
                    break;
            }
        }

        if (cli.filePath == null) {
            System.err.println("[Error] No source file specified.");
            printUsageAndExit();
        }

        if (!cli.filePath.endsWith(".mnd")) {
            System.err.println("[Warning] File '" + cli.filePath
                    + "' does not have the expected .mnd extension.");
        }

        if (cli.parseOnly && cli.typeCheckOnly) {
            System.err.println(
                    "[Error] --parse-only and --type-check-only are mutually exclusive.");
            System.exit(1);
        }

        if (cli.skipTypeCheck && cli.typeCheckOnly) {
            System.err.println(
                    "[Error] --skip-type-check and --type-check-only are mutually exclusive.");
            System.exit(1);
        }

        return cli;
    }

    private static void printUsageAndExit() {
        System.out.println("MandalaCode Interpreter — Part 2");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java Main <source.mnd> [flags]");
        System.out.println();
        System.out.println("Flags:");
        System.out.println("  --dump-ast          Print the full AST after parsing");
        System.out.println("  --print-tokens      Print the token stream after lexing");
        System.out.println("  --parse-only        Stop after parsing (no type check or execution)");
        System.out.println("  --type-check-only   Stop after type checking (no execution)");
        System.out.println("  --skip-type-check   Skip type checking and go straight to execution");
        System.out.println("  --help              Show this message");
        System.out.println();
        System.out.println("Exit codes:");
        System.out.println("  0  Success");
        System.out.println("  1  Lex or parse error");
        System.out.println("  2  Type error");
        System.out.println("  3  Runtime error");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java Main examples/mandala1.mnd");
        System.out.println("  java Main examples/mandala1.mnd --dump-ast");
        System.out.println("  java Main examples/mandala1.mnd --type-check-only");
        System.out.println("  java Main examples/mandala1.mnd --print-tokens --dump-ast");
        System.out.println("  java Main examples/bad_type.mnd --type-check-only");
        System.exit(0);
    }

    // ════════════════════════════════════════════════════════════
    // CLI ARGS DATA CARRIER
    // ════════════════════════════════════════════════════════════

    private static class CLIArgs {
        String filePath = null;
        boolean dumpAst = false;
        boolean printTokens = false;
        boolean parseOnly = false;
        boolean typeCheckOnly = false;
        boolean skipTypeCheck = false;
    }
}