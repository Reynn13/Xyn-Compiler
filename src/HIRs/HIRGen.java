package HIRs;

import builtins.BuiltinType;
import errors.ErrorEngine;
import parser.Ast.AstNode;
import parser.Ast.exprs.BinaryExpr;
import parser.Ast.exprs.Expr;
import parser.Ast.exprs.UnaryExpr;
import parser.Ast.statements.decls.VarDecl;


import java.util.HashMap;


public final class HIRGen {
    private final StringBuilder sb = new StringBuilder(1000);

    // borrowing for a sec, sorry ErrorEngine
    private final String source = ErrorEngine.source;

    private final AstNode[] statements;
    private final int length;
    private int counter = 0;
    public HIRGen(AstNode[] s, int l) {
        statements = s;

        length = l;
    }

    private final HashMap<Integer, BuiltinType> reg = new HashMap<>(3);

    private boolean isLeftOrRightInteger(BuiltinType leftType, BuiltinType rightType) {
        return (leftType == BuiltinType.Integer && rightType == BuiltinType.Float)
                ||
               (rightType == BuiltinType.Integer && leftType == BuiltinType.Float);
    }

    private void makeBinary(BinaryExpr bin) {
        evalExpr(bin.left);
        int left = counter-1;
        evalExpr(bin.right);
        int right = counter-1;

        if (isLeftOrRightInteger(reg.get(left), reg.get(right))) {
            // example: t1 = itof t0
            sb.append('t').append(counter++).append(" = itof t").append((reg.get(left) == BuiltinType.Integer) ? left : right).append('\n');
            // if example == "t1 = itof t0" where left is Integer. left = counter - 1 (example: 1)
            if (reg.get(left) == BuiltinType.Integer) {
                left = counter-1;
            } else {
                right = counter-1;
            }
            reg.put(counter, BuiltinType.Float);
        } else {
            reg.put(counter, BuiltinType.Integer);
        }

        // example: t3 = add t2 t1
        sb.append('t').append(counter++).append(" = ").append(
                switch (bin.op) {
                    case Plus -> "add";
                    case Minus -> "sub";
                    case Mul -> "mul";
                    case Div -> "div";
                    default -> throw new Error("unknown operator");
                }
                ).append(' ')
                .append('t').append(left)
                .append(" t").append(right)
                .append('\n');
    }

    private void evalExpr(AstNode value) {
        if (value instanceof BinaryExpr b) {
            makeBinary(b);
            return;
        }
        if (value instanceof UnaryExpr u) {
            evalExpr(u.expr);
            return;
        }
         if (value instanceof Expr e) {
            reg.put(counter, e.type);
            // example: t0 = const 12
            sb.append('t').append(counter++).append(" = const ").append(source, value.startIdx, value.endIdx).append('\n');
            return;
        }
        throw new Error("Unknown value type");
    }

    private boolean isNotInferredAndVarRefAnd(BuiltinType type, BuiltinType notType) {
        return type != BuiltinType.Inferred && notType != BuiltinType.VarRef && type != notType;
    }

    private void handleVarTypeNotTyped(BuiltinType type, int regIdx) {
        sb.append('t').append(counter++).append(" = ").append(
                switch (reg.get(regIdx)) {
                    case Integer -> "ito";
                    case Float -> "fto";
                    default ->  throw new Error("WTH");
                }
        ).append(
                switch (type) {
                    case Integer -> "i";
                    case Float -> "f";
                    default ->  throw new Error("WTH");
                }
        ).append(" t").append(regIdx).append('\n');
    }

    private void generateDecl(VarDecl var) {
        evalExpr(var.value);
        int v = counter-1;

        if (isNotInferredAndVarRefAnd(var.varType, reg.get(v))) {
            handleVarTypeNotTyped(var.varType, v);
        }
        sb.append("store ").append(source, var.startIdx, var.endIdx).append(' ').append('t').append(counter-1).append('\n');
    }

    public String generate() {
        int elm = 0;
        while (elm < length) {
            switch (statements[elm++]) {
                case VarDecl v:
                    generateDecl(v);
                    break;
                default:
                    break;
            }
        }
        IO.println("\n- zero pass:\n" + sb);
        return new HIRPass(sb.toString().toCharArray()).optimize();
    }
}
