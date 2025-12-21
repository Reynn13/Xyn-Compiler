package IRs;

import builtins.BuiltinType;
import errors.ErrorEngine;
import parser.Ast.AstNode;
import parser.Ast.exprs.BinaryExpr;
import parser.Ast.exprs.Expr;
import parser.Ast.exprs.UnaryExpr;
import parser.Ast.statements.decls.VarDecl;
import symboltables.Symbol;
import symboltables.SymbolType;

import java.util.HashMap;



// optimization class for IR
final class IRPass {
    private boolean afterOperation = false;
    private StringBuilder sb = new StringBuilder(500);
    private final StringBuilder s = new StringBuilder(500);
    private int idx = 0;
    private char[] chars;


    private final HashMap<Integer, String> reg = new HashMap<>();
    private final HashMap<String, String> var = new HashMap<>();

    public IRPass(char[] c) {
        chars = c;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void handleTempVar() {
        // skipping "t"
        ++idx;
        while (isDigit(chars[idx])) {
            s.append(chars[idx++]);
        }
        final int i = Integer.parseInt(s.toString());
        s.setLength(0);
        idx += 3; // skipping " = "
        switch (chars[idx]) {
            // const
            case 'c':
                // skipping "const "
                idx += 6;
                if (isDigit(chars[idx])) {
                    while (isDigit(chars[idx])) {
                        s.append(chars[idx++]);
                    }
                    reg.put(i, s.toString());
                }
                break;

            // mul
            case 'm': {
                final int v0 = getV0(i);
                // second argument
                if (chars[idx] == 't') {
                    ++idx;
                    while (isDigit(chars[idx])) {
                        s.append(chars[idx++]);
                    }


                    // the first argument is temp var
                    if (v0 >= 0) {
                        final String l = reg.get(v0);
                        final String r = reg.get(Integer.parseInt(s.toString()));
                        final String v = String.valueOf(
                                Integer.parseInt(l) * Integer.parseInt(r)
                        );

                        reg.put(i, v);
                        sb.append(v).append('\n');
                    }
                }
                break;
            }
            // sub
            case 's': {
                final int v0 = getV0(i);
                // second argument
                if (chars[idx] == 't') {
                    ++idx;
                    while (isDigit(chars[idx])) {
                        s.append(chars[idx++]);
                    }


                    // the first argument is temp var
                    if (v0 >= 0) {
                        final String l = reg.get(v0);
                        final String r = reg.get(Integer.parseInt(s.toString()));
                        final String v = String.valueOf(
                                Integer.parseInt(l) - Integer.parseInt(r)
                        );

                        reg.put(i, v);
                        sb.append(v).append('\n');
                    }
                }
                break;
            }
            // add
            case 'a': {
                final int v0 = getV0(i);
                // second argument
                if (chars[idx] == 't') {
                    ++idx;
                    while (isDigit(chars[idx])) {
                        s.append(chars[idx++]);
                    }


                    // the first argument is temp var
                    if (v0 >= 0) {
                        final String l = reg.get(v0);
                        final String r = reg.get(Integer.parseInt(s.toString()));
                        final String v = String.valueOf(
                                Integer.parseInt(l) + Integer.parseInt(r)
                        );

                        reg.put(i, v);
                        sb.append(v).append('\n');
                    }
                }
                break;
            }
        }
        ++idx; // skip the newline
        s.setLength(0);
    }

    private int getV0(int i) {
        afterOperation = true;
        // skip the "add "
        idx += 4;
        sb.append('t').append(i).append(" = const ");
        int v0 = -1;
        // first argument
        if (chars[idx] == 't') {
            ++idx;
            while (isDigit(chars[idx])) {
                s.append(chars[idx++]);
            }
            ++idx; // skip the space
            v0 = Integer.parseInt(s.toString());
        }
        s.setLength(0);
        return v0;
    }

    private void handleVar() {
        // skipping the "store "
        idx += 6;
        s.append("store ");
        // skipping the varname
        while (chars[idx] != ' ') {
            s.append(chars[idx++]);
        }
        final String st = s.toString();
        s.setLength(0);
        // skipping the " "
        ++idx;
        if (chars[idx] == 't') {
            ++idx; // skipping the t
            while (isDigit(chars[idx])) {
                s.append(chars[idx++]);
            }
            ++idx; // skipping the new line
            final int r = Integer.parseInt(s.toString());

            if (afterOperation) {
                sb.append(st).append(' ').append('t').append(r).append('\n');
            } else {
                sb.append(st).append(' ').append(reg.get(r)).append('\n');
            }
            // skip the "store "
            var.put(st.substring(5), reg.get(r));
        } else {
            while (chars[idx] != '\n') {
                s.append(chars[idx++]);
            }

            final String r = s.toString();
            sb.append(st).append(' ').append(var.get(r)).append('\n');
            // skip the "store "
            var.put(st.substring(5), var.get(r));
        }
        ++idx; // skipping the new line
        s.setLength(0);
    }

    public String optimize() {
        onePass();
        IO.println(sb.toString());
        chars = sb.toString().toCharArray();
        sb = new StringBuilder();

        onePass();

        return sb.toString();
    }

    private void onePass() {
        boolean hasImprovement = true;
        idx = 0;
        reg.clear();
        var.clear();

        while (hasImprovement) {
            hasImprovement = false;

            if (idx >= chars.length) break;

            if (chars[idx] == '\n') {
                hasImprovement = true;
                ++idx;
                continue;
            }
            // temp var
            if (chars[idx] == 't') {
                handleTempVar();
                hasImprovement = true;
            }
            // permanent var
            if (chars[idx] == 's') {
                handleVar();
                hasImprovement = true;
            }

            afterOperation = false;
        }
    }

}

public final class IRGen {
    private final StringBuilder sb = new StringBuilder(1000);

    // borrowing for a sec, sorry ErrorEngine
    private final String source = ErrorEngine.source;
    private final HashMap<SymbolType, HashMap<String, Symbol>> symbolTable;
    private final AstNode[] statements;
    private final int length;
    private int counter = 0;
    public IRGen(AstNode[] s, HashMap<SymbolType, HashMap<String, Symbol>> sy, int l) {
        statements = s;
        symbolTable = sy;
        length = l;
    }

    private final HashMap<Integer, BuiltinType> reg = new HashMap<>(3);

    private void makeBinary(BinaryExpr bin) {
        evalExpr(bin.left);
        int left = counter-1;
        evalExpr(bin.right);
        int right = counter-1;
        if ((reg.get(left) == BuiltinType.Integer && reg.get(right) == BuiltinType.Float) || (reg.get(right) == BuiltinType.Integer && reg.get(left) == BuiltinType.Float)) {
            sb.append('t').append(counter++).append(" = itof t").append((reg.get(left) == BuiltinType.Integer) ? left : right).append('\n');
            if (reg.get(left) == BuiltinType.Integer) {
                left = counter-1;
            } else {
                right = counter-1;
            }
            reg.put(counter, BuiltinType.Float);
        } else {
            reg.put(counter, BuiltinType.Integer);
        }
        
        sb.append('t').append(counter++).append(" = ").append(
                switch (bin.op) {
                    case Plus -> "add";
                    case Minus -> "sub";
                    case Mul -> "mul";
                    case Div -> "div";
                    default -> throw new Error("wth");
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
            sb.append('t').append(counter++).append(" = const ").append(source, value.startIdx, value.endIdx).append('\n');
            return;
        }
        throw new Error("What? How");
    }

    private void generateDecl(VarDecl var) {
        AstNode val = var.value;
        evalExpr(val);
        int v = counter-1;
        if (var.varType != BuiltinType.Inferred && var.varType != reg.get(v)) {
            sb.append('t').append(counter++).append(" = ").append(
                    switch (reg.get(v)) {
                        case Integer -> "ito";
                        case Float -> "fto";
                        default ->  throw new Error("WTH");
                    }
            ).append(
                    switch (var.varType) {
                        case Integer -> "i";
                        case Float -> "f";
                        default ->  throw new Error("WTH");
                    }
            ).append(" t").append(v).append('\n');
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
        IO.println(sb.toString());
        return new IRPass(sb.toString().toCharArray()).optimize();
    }
}
