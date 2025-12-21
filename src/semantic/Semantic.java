package semantic;

import builtins.BuiltinType;
import errors.Err;
import errors.ErrorEngine;
import lexer.tokens.TokenType;
import parser.Ast.AstNode;
import parser.Ast.exprs.BinaryExpr;
import parser.Ast.exprs.Expr;
import parser.Ast.exprs.UnaryExpr;
import parser.Ast.exprs.literals.VarRef;
import parser.Ast.statements.Block;
import parser.Ast.statements.Statement;
import parser.Ast.statements.decls.VarDecl;
import symboltables.Symbol;
import symboltables.SymbolType;
import symboltables.Variable;

import java.util.Arrays;
import java.util.HashMap;

public final class Semantic {
    private final Block global;
    private final int statementLength;

    public Semantic(Block g, int statementLen) {
        global = g;
        statementLength = statementLen;
    }

    private BuiltinType getType(AstNode v) {
        return (v instanceof BinaryExpr b) ? getType(b.left) : (v instanceof VarRef var) ?
                (
                        (Variable)global.symbolTable.get(SymbolType.var).get("a")
                ).type
                :
                ((Expr)v).type;
    }
    private void checkAddition(AstNode l, AstNode r) {
        BuiltinType lt = getType(l);
        BuiltinType rt = getType(r);
        if (lt == rt) return;
        if (lt == BuiltinType.String || rt == BuiltinType.String) {
            ErrorEngine.addWithValue("Addition Error: Cannot add String value `%s` with %s value `%s`.",
                    l.line, l.startIdx, r.endIdx,
                    (lt == BuiltinType.String) ?
                            ErrorEngine.source.substring(l.startIdx+1, l.endIdx-1)
                            :
                            ErrorEngine.source.substring(r.startIdx+1, r.endIdx-1),
                    (lt == BuiltinType.String) ? rt : lt,
                    (lt == BuiltinType.String) ?
                            ErrorEngine.source.substring(r.startIdx, r.endIdx)
                            :
                            ErrorEngine.source.substring(l.startIdx, l.endIdx)
            );
            return;
        }
        if ((lt == BuiltinType.Integer && rt == BuiltinType.Float) || (rt == BuiltinType.Integer && lt == BuiltinType.Float)) {
            return;
        }
        ErrorEngine.addWithValue("Value Error: Unknown value `%s` and `%s`.",
                l.line, l.startIdx, r.endIdx,
                ErrorEngine.source.substring(l.startIdx, l.endIdx),
                ErrorEngine.source.substring(r.startIdx, r.endIdx)
        );
    }
    private void checkSubtraction(AstNode l, AstNode r) {
        BuiltinType lt = getType(l);
        BuiltinType rt = getType(r);
        if (lt == BuiltinType.String && rt == BuiltinType.String) {
            ErrorEngine.addWithValue("Substraction Error: Cannot subtract two String values `%s` and `%s`.",
                    l.line, l.startIdx, r.endIdx,
                    ErrorEngine.source.substring(l.startIdx+1, l.endIdx-1),
                    ErrorEngine.source.substring(r.startIdx+1, r.endIdx-1)
            );
        }
        if (lt == BuiltinType.String) {
            ErrorEngine.addWithValue("Substraction Error: Cannot subtract a String value `%s` with right operand %s value `%s`.",
                    l.line, l.startIdx, r.endIdx,
                    ErrorEngine.source.substring(l.startIdx+1, l.endIdx-1),
                    rt,
                    ErrorEngine.source.substring(r.startIdx, r.endIdx)
            );
            return;
        }
        if (rt == BuiltinType.String) {
            ErrorEngine.addWithValue("Substraction Error: Cannot do subtraction with right operand String value `%s` with left operand %s value `%s`.",
                    l.line, l.startIdx, r.endIdx,
                    ErrorEngine.source.substring(r.startIdx+1, r.endIdx-1),
                    lt,
                    ErrorEngine.source.substring(l.startIdx, l.endIdx)
            );
            return;
        }
        if (lt == rt) return;

        if ((lt == BuiltinType.Integer && rt == BuiltinType.Float) || (rt == BuiltinType.Integer && lt == BuiltinType.Float)) {
            return;
        }
        ErrorEngine.addWithValue("Value Error: Unknown value `%s` and `%s`.",
                l.line, l.startIdx, r.endIdx,
                ErrorEngine.source.substring(l.startIdx, l.endIdx),
                ErrorEngine.source.substring(r.startIdx, r.endIdx)
        );
    }
    private void checkMultiplication(AstNode l, AstNode r) {
        BuiltinType lt = getType(l);
        BuiltinType rt = getType(r);
        if (lt == BuiltinType.String && rt == BuiltinType.String) {
            ErrorEngine.addWithValue("Substraction Error: Cannot multiply two String values `%s` and `%s`.",
                    l.line, l.startIdx, r.endIdx,
                    ErrorEngine.source.substring(l.startIdx+1, l.endIdx-1),
                    ErrorEngine.source.substring(r.startIdx+1, r.endIdx-1)
            );
        }
        if (lt == BuiltinType.String) {
            ErrorEngine.addWithValue("Substraction Error: Cannot multiply a String value `%s` with right operand %s value `%s`.",
                    l.line, l.startIdx, r.endIdx,
                    ErrorEngine.source.substring(l.startIdx+1, l.endIdx-1),
                    rt,
                    ErrorEngine.source.substring(r.startIdx, r.endIdx)
            );
            return;
        }
        if (rt == BuiltinType.String) {
            ErrorEngine.addWithValue("Substraction Error: Cannot do multiplication with right operand String value `%s` with left operand %s value `%s`.",
                    l.line, l.startIdx, r.endIdx,
                    ErrorEngine.source.substring(r.startIdx+1, r.endIdx-1),
                    lt,
                    ErrorEngine.source.substring(l.startIdx, l.endIdx)
            );
            return;
        }
        if (lt == rt) return;

        if ((lt == BuiltinType.Integer && rt == BuiltinType.Float) || (rt == BuiltinType.Integer && lt == BuiltinType.Float)) {
            return;
        }
        ErrorEngine.addWithValue("Value Error: Unknown value `%s` and `%s`.",
                l.line, l.startIdx, r.endIdx,
                ErrorEngine.source.substring(l.startIdx, l.endIdx),
                ErrorEngine.source.substring(r.startIdx, r.endIdx)
        );
    }
    private void checkDivision(AstNode l, AstNode r) {
        BuiltinType lt = getType(l);
        BuiltinType rt = getType(r);
        if (lt == BuiltinType.String && rt == BuiltinType.String) {
            ErrorEngine.addWithValue("Substraction Error: Cannot divide two String values `%s` and `%s`.",
                    l.line, l.startIdx, r.endIdx,
                    ErrorEngine.source.substring(l.startIdx+1, l.endIdx-1),
                    ErrorEngine.source.substring(r.startIdx+1, r.endIdx-1)
            );
        }
        if (lt == BuiltinType.String) {
            ErrorEngine.addWithValue("Substraction Error: Cannot divide a String value `%s` with right operand %s value `%s`.",
                    l.line, l.startIdx, r.endIdx,
                    ErrorEngine.source.substring(l.startIdx+1, l.endIdx-1),
                    rt,
                    ErrorEngine.source.substring(r.startIdx, r.endIdx)
            );
            return;
        }
        if (rt == BuiltinType.String) {
            ErrorEngine.addWithValue("Substraction Error: Cannot do division with right operand String value `%s` with left operand %s value `%s`.",
                    l.line, l.startIdx, r.endIdx,
                    ErrorEngine.source.substring(r.startIdx+1, r.endIdx-1),
                    lt,
                    ErrorEngine.source.substring(l.startIdx, l.endIdx)
            );
            return;
        }
        if (lt == rt) return;

        if ((lt == BuiltinType.Integer && rt == BuiltinType.Float) || (rt == BuiltinType.Integer && lt == BuiltinType.Float)) {
            return;
        }
        ErrorEngine.addWithValue("Value Error: Unknown value `%s` and `%s`.",
                l.line, l.startIdx, r.endIdx,
                ErrorEngine.source.substring(l.startIdx, l.endIdx),
                ErrorEngine.source.substring(r.startIdx, r.endIdx)
        );
    }

    private void evaluateBinary(BinaryExpr b) {
        checkExpr(b.left);
        checkExpr(b.right);

        switch (b.op) {
            case TokenType.Plus -> checkAddition(b.left, b.right);
            case TokenType.Minus -> checkSubtraction(b.left, b.right);
            case TokenType.Mul -> checkMultiplication(b.left, b.right);
            case TokenType.Div -> checkDivision(b.left, b.right);
            default -> throw new Error("Error: Unsupported operator: " + b.op);
        };
    }
    private void checkExpr(AstNode expr) {
        if (expr instanceof BinaryExpr b) {
            evaluateBinary(b);
        }
        if (expr instanceof UnaryExpr u) {
            checkExpr(u.expr);
        }
        // if var instanceof VarRef, you don't need to check the value again
        if (expr instanceof Expr) {
            return;
        }
        throw new Error("Unexpected expr");
    }

    private BuiltinType checkAndGetType(AstNode val) {
        BuiltinType type = getType(val);
        checkExpr(val);
        return type;
    }

    private void checkDecl(VarDecl var) {
        BuiltinType type = checkAndGetType(var.value);
        if ((var.varType == BuiltinType.Inferred) || (var.varType == type) || (var.varType == BuiltinType.Integer && type == BuiltinType.Float) || (var.varType == BuiltinType.Float && type == BuiltinType.Integer)) {
            global.symbolTable.get(SymbolType.var).put(ErrorEngine.source.substring(var.startIdx, var.endIdx), new Variable(var.isDynamic, type, var.value));
            return;
        }
        // if not inferred, not an integer nor float
        if (var.varType != type) {
            ErrorEngine.addWithValue("Declaration Error: Cannot assign variable name `%s` with expected value type are %s, but got value type %s instead.",
                    var.line, var.value.startIdx, var.value.endIdx,
                    ErrorEngine.source.substring(var.startIdx, var.endIdx),
                    var.varType,
                    type
                );
        }
    }

    public void check() {
        // fast exit
        if (ErrorEngine.hasError()) {
            ErrorEngine.reportAll();
        }
        int idx = 0;
        while (idx < statementLength) {
            switch (global.statements[idx++]) {
                case VarDecl v:
                    checkDecl(v);

                default:
                    break;
            }
        }

        // from the checking
        if (ErrorEngine.hasError()) {
            ErrorEngine.reportAll();
        }
    }
}
