package parser;

import builtins.BuiltinType;
import errors.EOFError;
import errors.ErrorEngine;
import lexer.tokens.Token;
import lexer.tokens.TokenType;
import parser.Ast.AstNode;
import parser.Ast.exprs.BinaryExpr;
import parser.Ast.exprs.UnaryExpr;
import parser.Ast.exprs.literals.FloatLiteral;
import parser.Ast.exprs.literals.IntLiteral;
import parser.Ast.exprs.literals.StringLiteral;
import parser.Ast.exprs.literals.VarRef;
import parser.Ast.statements.Block;
import parser.Ast.statements.decls.VarDecl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Parser {
    private final Token[] tokens;
    private final int TokLen;
    public int idx = 0;

    private static final Map<TokenType, TokenType> possibleStartOfStatement = Map.of(
            TokenType.Let, TokenType.Let
    );

    private static final HashMap<TokenType, List<TokenType>> CheckPointByContext = new HashMap<>();

    static {
        CheckPointByContext.put(TokenType.Let, List.of(TokenType.Eq, TokenType.Semicolon));
        CheckPointByContext.put(TokenType.Dyn, List.of(TokenType.Eq, TokenType.Semicolon));
        CheckPointByContext.put(TokenType.Operation, List.of(TokenType.Semicolon));
    }

    /**
     * A constructor that will construct a parser who process the tokens into list of {@link AstNode}.
     *
     * @param t is a list of token produced by the lexer
     * @param l is a length of the {@code t}
     *
     */
    public Parser(Token[] t, int l) {
        tokens = t;
        TokLen = l;
    }

    private int getPrecedence(TokenType type) {
        return switch (type) {
            case Minus, Plus -> 1;
            case Div, Mul -> 2;
            default -> throw new Error("Unknown operator precedence: " + type);
        };
    }

    private AstNode Prefix() {
        while (true) {
            checkEOF(", expected an expression.");
            Token t = tokens[idx++];
            switch (t.type) {
                case Digit:
                    return new IntLiteral(t.startIdx, t.endIdx, t.startCol, t.endCol, t.line);
                case Float:
                    return new FloatLiteral(t.startIdx, t.endIdx, t.startCol, t.endCol, t.line);
                case String:
                    return new StringLiteral(t.startIdx, t.endIdx, t.startCol, t.endCol, t.line);
                case Minus:
                    AstNode e = Expr(3);
                    return new UnaryExpr(e, t.startIdx, e.endIdx, t.startCol, e.endCol, t.line);

                case Ident:
                    return new VarRef(t.startIdx, t.endIdx, t.startCol, t.endCol, t.line);

                case Lparen:
                    AstNode expr = Expr(0);
                    checkAndGet(", expected a right-parentheses.", TokenType.Operation, TokenType.Rparen);
                    return expr;

                // it will loop all over again until meet a correct value
                default:
                    ErrorEngine.addWithValue("Value Error: Unexpected value: `%s` at column %d:%d and line %d.", t.line, t.startIdx, t.endIdx, ErrorEngine.source.substring(t.startIdx, t.endIdx), t.startCol, t.endCol, t.line);
                    break;
            }
        }
    }

    private AstNode Expr(int minPrecedence) {
        AstNode left = Prefix();

        while (true) {
            checkEOF(", expected a semicolon or expression.");
            Token t = tokens[idx];
            if (t.type == TokenType.Semicolon) break;
            int precedence = getPrecedence(t.type);
            if (precedence < minPrecedence) break;

            ++idx; // consume operator

            // left-associative
            AstNode right = Expr(precedence + 1);

            left = switch (t.type) {
                case Div, Mul, Minus, Plus -> new BinaryExpr(left, t.type, right, left.startIdx, right.endIdx, left.startCol, right.endCol, left.line);
                default -> throw new Error("Unexpected operator");
            };
        }
        return left;
    }


    private void checkEOF(String msg) {
        if (idx >= TokLen) {
            Token t = tokens[idx-1];
            ErrorEngine.add(new EOFError("EOF: Unexpected end of file%s", t.line, t.startIdx, t.endIdx, msg));
            ErrorEngine.reportAll();
        }
    }

    private Token recover(TokenType ctx, TokenType expectedType) {
        int d = idx;
        int possibleStatementIdx = idx;
        Token t;
        List<TokenType> checkpoints = CheckPointByContext.get(ctx);
        boolean possibleStatement = false;

        // check for nearest safe point
        for (int i = 0; i < 5 && d < TokLen && (t = tokens[d]).type != expectedType; ++i) {
            if (checkpoints.contains(t.type)) {
                idx = d;
                t.isCheckpoint = true;
                return t;
            }
            if (!possibleStatement && possibleStartOfStatement.containsKey(t.type)) {
                possibleStatement = true;
                possibleStatementIdx = d;
            }
            ++d;
        }
        Token e;
        // If no checkpoints found
        if (d - idx == 5) {
            t = tokens[idx];

            if (possibleStatement) {

                e = tokens[possibleStatementIdx-1];

                ErrorEngine.addWithValue("Syntax Error: Unexpected token value: `%s` at column %d-%d and line %d. Please delete this token.", t.line, t.startIdx, e.endIdx, ErrorEngine.source.substring(t.startIdx, e.endIdx), t.startCol, e.endCol, t.line);

                // possibleStatement is true, then assign it to index
                idx = possibleStatementIdx;

                return null;
            }
            e = tokens[(idx = d)];

            ErrorEngine.addWithValue("Syntax Error: Unexpected token value: `%s` at column %d-%d and line %d. Please delete this token.", t.line, t.startIdx, e.endIdx, ErrorEngine.source.substring(t.startIdx, e.endIdx), t.startCol, e.endCol, t.line);

            return null;
        }
        else {
            t = tokens[idx];
            e = tokens[d-1];
            idx = d;
            ErrorEngine.addWithValue("Syntax Error: Unexpected token value: `%s` at column %d-%d and line %d. Please delete this token.", t.line, t.startIdx, e.endIdx, ErrorEngine.source.substring(t.startIdx, e.endIdx), t.startCol, e.endCol, t.line);
            return tokens[idx++];
        }
    }

    /**
     * Return a token with type that is the same as the {@code expectedType}
     *
     * @param ctx are the context of what is the parser parse right now
     * @param expectedType is the expected {@link TokenType}
     * @return either <b>Token</b> or <b>null</b>. If the return value is null, then the parser met an/some errors.
     */
    private Token checkAndGet(String msg, TokenType ctx, TokenType expectedType) {
        // Check EOF
        checkEOF(msg);

        return (tokens[idx].type != expectedType) ? recover(ctx, expectedType) : tokens[idx++];

    }

    // TODO: Make this method can handle lists type
    private BuiltinType getType(Token t) {
        checkEOF(", expected a type.");
        final String type = ErrorEngine.source.substring(t.startIdx, t.endIdx);
        return switch (type) {
            case "float" -> BuiltinType.Float;
            case "int" -> BuiltinType.Integer;
            case "str" -> BuiltinType.String;
            default -> throw new Error("Unexpected type");
        };
    }

    private VarDecl Decl(TokenType declType, int line, int startCol) {
        Token ident = checkAndGet(", expected an Ident.", declType, TokenType.Ident);
        if (ident == null) return null;


        BuiltinType whatType = BuiltinType.Inferred;

        // static typing by user
        if (tokens[idx].type == TokenType.Colon) {
            //                          skipping the colon
            whatType = getType(tokens[++idx]);
            ++idx;
        }

        if (checkAndGet(", expected an equal sign.", declType, TokenType.Eq) == null) return null;

        AstNode expr = Expr(0);

        Token semicolon = checkAndGet(", expected a semicolon", declType, TokenType.Semicolon);
        if (semicolon == null) return null;

        return new VarDecl(ident.startIdx, ident.endIdx, expr, startCol, semicolon.endCol, line, whatType, declType == TokenType.Dyn);
    }

    /**
     * THE MAIN LOOP
     */
    public Block parse() {
        Block global = new Block(Math.max(8, TokLen / 2));
        int elm = 0;

        while (idx < TokLen) {

            Token t = tokens[idx];
            // checks for start of a statement
            switch (t.type) {
                case Dyn: // dynamic var
                case Let: // static var
                    ++idx;
                    global.statements[elm++] = Decl(t.type, t.line, t.startCol);
                    break;

                default:
                    ErrorEngine.addWithValue("Syntax Error: Unexpected token value: `%s` at column %d:%d and line %d. Please delete this token.", t.line, t.startIdx, t.endIdx,  ErrorEngine.source.substring(t.startIdx, t.endIdx), t.startCol, t.endCol, t.line);
                    ++idx;
                    break;
            }
        }
        idx = elm;
        return global;
    }
}
