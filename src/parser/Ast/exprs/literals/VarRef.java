package parser.Ast.exprs.literals;

import builtins.BuiltinType;
import parser.Ast.exprs.Expr;

public final class VarRef extends Expr {
    public VarRef(int si, int ei, int sc, int ec, int ln) {
        super(si, ei, sc, ec, ln);
        type = BuiltinType.VarRef;
    }
}
