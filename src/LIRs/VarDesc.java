package LIRs;

import builtins.BuiltinType;

public class VarDesc {
    public final BuiltinType type;
    public String value;
    public VarDesc(BuiltinType t, String v) {
        type = t;
        value = v;
    }
}
