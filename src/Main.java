
// Main.java
import ast.ProgramNode;
import lexer.Lexer;
import lexer.Token;
import parser.Parser;

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

        // ── Stage 4: Interpreter placeholder ────────────────────
        // Will be replaced in Part 2 with:
        // TypeChecker checker = new TypeChecker();
        // checker.check(ast);
        // Interpreter interpreter = new Interpreter();
        // interpreter.execute(ast);
        System.out.println("[OK] '" + cli.filePath + "' parsed successfully.");
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
            return null; // unreachable — keeps compiler happy
        }
    }

    private static ProgramNode runParser(List<Token> tokens, CLIArgs cli) {
        try {
            Parser parser = new Parser(tokens);
            return parser.parse();

        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return null; // unreachable — keeps compiler happy
        }
    }

    // ════════════════════════════════════════════════════════════
    // FILE READING
    // ════════════════════════════════════════════════════════════

    private static String readFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            System.err.println("[Error] Cannot read file '" + path + "': "
                    + e.getMessage());
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
                case "--help":
                    printUsageAndExit();
                    break;
                default:
                    if (arg.startsWith("--")) {
                        System.err.println("[Error] Unknown flag '" + arg + "'.");
                        printUsageAndExit();
                    }
                    if (cli.filePath != null) {
                        System.err.println("[Error] Only one source file may be specified.");
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

        return cli;
    }

    private static void printUsageAndExit() {
        System.out.println("MandalaCode Interpreter — Part 1 (Lexer + Parser)");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java Main <source.mnd> [flags]");
        System.out.println();
        System.out.println("Flags:");
        System.out.println("  --dump-ast       Print the full AST after parsing");
        System.out.println("  --print-tokens   Print the token stream after lexing");
        System.out.println("  --help           Show this message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java Main examples/mandala1.mnd");
        System.out.println("  java Main examples/mandala1.mnd --dump-ast");
        System.out.println("  java Main examples/mandala1.mnd --print-tokens --dump-ast");
        System.exit(0);
    }

    // ════════════════════════════════════════════════════════════
    // CLI ARGS DATA CARRIER
    // ════════════════════════════════════════════════════════════

    private static class CLIArgs {
        String filePath = null;
        boolean dumpAst = false;
        boolean printTokens = false;
    }
}