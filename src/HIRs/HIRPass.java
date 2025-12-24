package HIRs;

import LIRs.VarDesc;
import builtins.BuiltinType;

import java.util.HashMap;

/// optimization class for IR
public final class HIRPass {
    /// a flag that indicates if the previous expr are operation
    private boolean afterOperation = false;

    /// for main stringBuilder
    private StringBuilder sb = new StringBuilder(500);

    /// for other purposes
    private final StringBuilder other = new StringBuilder(500);

    private int idx = 0;
    private char[] chars;


    private final HashMap<Integer, VarDesc> reg = new HashMap<>();
    public final HashMap<String, VarDesc> var = new HashMap<>();

    public HIRPass(char[] c) {
        chars = c;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }


    private void skipIdx(int i) {
        idx += i;
    }

    private void gatherIndex() {
        while (isDigit(chars[idx])) {
            other.append(chars[idx++]);
        }
    }

    private void gatherUntil(char c) {
        while (chars[idx] != c) {
            other.append(chars[idx++]);
        }
    }

    private char byType(BuiltinType t) {
        return switch (t) {
            case Integer -> 'i';
            case Float -> 'f';
            case String -> 's';
            default -> throw new Error("Unknown type");
        };
    }

    private int getFirstArg(int i) {
        afterOperation = true;
        // skip the "add "
        skipIdx(4);
        // example: "t0 = const "
        sb.append('t').append(i).append(" = const ");

        int v0 = -1;
        // first argument
        if (chars[idx] == 't') {
            skipIdx(1);
            gatherIndex();

            v0 = Integer.parseInt(other.toString());
        }
        skipIdx(1); // skip the space between first arg and second arg
        reset(other);
        sb.append(byType(reg.get(v0).type)).append(' ');
        return v0;
    }

    private String parseAdd(String left, String right) {
        if (!left.contains(".")) {
            if (!right.contains(".")) {
                return String.valueOf(Integer.parseInt(left) + Integer.parseInt(right));
            }
            return String.valueOf(Integer.parseInt(left) + Float.parseFloat(right));
        }
        return String.valueOf(Float.parseFloat(left) + Float.parseFloat(right));
    }
    private String parseSub(String left, String right) {
        if (!left.contains(".")) {
            if (!right.contains(".")) {
                return String.valueOf(Integer.parseInt(left) - Integer.parseInt(right));
            }
            return String.valueOf(Integer.parseInt(left) - Float.parseFloat(right));
        }
        return String.valueOf(Float.parseFloat(left) - Float.parseFloat(right));
    }
    private String parseMul(String left, String right) {
        if (!left.contains(".")) {
            if (!right.contains(".")) {
                return String.valueOf(Integer.parseInt(left) * Integer.parseInt(right));
            }
            return String.valueOf(Integer.parseInt(left) * Float.parseFloat(right));
        }
        return String.valueOf(Float.parseFloat(left) * Float.parseFloat(right));
    }
    private String parseDiv(String left, String right) {
        if (!left.contains(".")) {
            if (!right.contains(".")) {
                return String.valueOf(Integer.parseInt(left) / Integer.parseInt(right));
            }
            return String.valueOf(Integer.parseInt(left) / Float.parseFloat(right));
        }
        return String.valueOf(Float.parseFloat(left) / Float.parseFloat(right));
    }

    /// 0 for addition, 1 for subtraction, 2 for multiplication, and 3 for division
    private void handleOperation(int op, int index) {

        final int v0 = getFirstArg(index);

        skipIdx(1);
        gatherIndex();

        // the first argument is temp var
        if (v0 >= 0) {
            final VarDesc leftArg = reg.get(v0);
            final VarDesc rightArg = reg.get(Integer.parseInt(other.toString()));
            String value = switch (op) {
                case 0 -> parseAdd(leftArg.value, rightArg.value);
                case 1 -> parseSub(leftArg.value, rightArg.value);
                case 2 -> parseMul(leftArg.value, rightArg.value);
                case 3 -> parseDiv(leftArg.value, rightArg.value);
                default -> throw new Error("Unknown operator");
            };


            if (leftArg.type != rightArg.type && rightArg.type == BuiltinType.Float) {
                value = value.substring(0, value.indexOf('.'));
                sb.append(value).append('\n');

            } else {
                sb.append(value).append('\n');
            }
            reg.put(index, new VarDesc(leftArg.type, value));
        }
    }

    private BuiltinType getType() {
        return switch (chars[idx]) {
            case 'i' -> BuiltinType.Integer;
            case 'f' -> BuiltinType.Float;
            case 's' -> BuiltinType.String;
            default -> throw new Error("Unknown Type");
        };
    }

    private void handleTempVarValue(int index) {
        switch (chars[idx]) {
            // const
            case 'c':
                // skipping "const "
                skipIdx(6);
                BuiltinType type = getType();
                skipIdx(2);
                if (chars[idx] == '-') {
                    other.append(chars[idx++]);
                }
                if (isDigit(chars[idx])) {
                    gatherIndex();
                    reg.put(index, new VarDesc(type, other.toString()));
                } else { // var
                    gatherUntil('\n');
                    reg.put(index, new VarDesc(type, var.get(other.toString()).value));
                }
                break;

            // add
            case 'a': {
                handleOperation(0, index);
                break;
            }
            // sub
            case 's': {
                handleOperation(1, index);
                break;
            }
            // mul
            case 'm': {
                handleOperation(2, index);
                break;
            }
            // div
            case 'd': {
                handleOperation(3, index);
                break;
            }

            // integer
            case 'i':
                skipIdx(2);
                if (chars[idx] == '-') {
                    other.append(chars[idx++]);
                }
                if (isDigit(chars[idx])) {
                    gatherIndex();
                    reg.put(index, new VarDesc(BuiltinType.Integer, other.toString()));
                    // float
                    if (chars[idx] == '.') {
                        gatherUntil('\n');
                    }
                } else {
                    gatherUntil('\n');
                    reg.put(index, new VarDesc(BuiltinType.Integer, var.get(other.toString()).value));
                }

                break;

            // float
            case 'f':
                skipIdx(2);
                if (chars[idx] == '-') {
                    other.append(chars[idx++]);
                }
                if (chars[idx] == 't') {
                    skipIdx(1);
                    gatherUntil('\n');
                    final int i = Integer.parseInt(other.toString());
                    if (reg.get(i).type != BuiltinType.Float) {
                        reg.put(index, new VarDesc(BuiltinType.Float, reg.get(i).value + ".0"));
                    } else {
                        reg.put(index, new VarDesc(BuiltinType.Float, reg.get(i).value));
                    }
                }
                else if (isDigit(chars[idx])) {
                    gatherIndex();
                    // float
                    if (chars[idx] == '\n') {
                        reg.put(index, new VarDesc(BuiltinType.Float, other + ".0"));
                    } else {
                        gatherUntil('\n');
                        reg.put(index, new VarDesc(BuiltinType.Float, other.toString()));
                    }
                } else {
                    gatherUntil('\n');
                    final String val = var.get(other.toString()).value;

                    reg.put(index, new VarDesc(BuiltinType.Float, (!val.contains(".") ? val + ".0" : val)));
                }

                break;
        }
        skipIdx(1); // skip the newline
        reset(other);
    }

    private void handleTempVar() {
        // skipping "t"
        skipIdx(1);
        gatherIndex();


        final int index = Integer.parseInt(other.toString());

        reset(other);
        skipIdx(3); // skipping " = "

        handleTempVarValue(index);

    }

    private void reset(StringBuilder s) {
        s.setLength(0);
    }

    private void handleVar() {
        // skipping the "store "
        skipIdx(6);

        other.append("store ");
        // skipping the varName
        gatherUntil(' ');
        other.append(chars[idx++]);
        BuiltinType type = getType();
        other.append(chars[idx++]);


        final String st = other.toString();

        reset(other);
        skipIdx(1);
        // skip
        if (isDigit(chars[idx])) {
            gatherUntil('\n');
            reset(other);
            skipIdx(1);
            return;
        }
        final String key = st.substring(6, st.length() - 2);
        if (chars[idx] == 't') {
            skipIdx(1); // skipping the t

            gatherIndex();
            final int right = Integer.parseInt(other.toString());

            if (afterOperation) {
                sb.append(st).append(' ').append('t').append(right).append('\n');
            } else {
                sb.append(st).append(' ').append(reg.get(right).value).append('\n');

            }
            // skip the "store "
            final VarDesc val = reg.get(right);
            if (type != val.type) {
                var.put(
                        key,
                        new VarDesc(
                                type, (type == BuiltinType.Integer) ?
                                val.value.substring(0, val.value.indexOf('.'))
                                :
                                val.value + ".0"
                        )
                );
            } else {
                var.put(key, new VarDesc(val.type, val.value));
            }

        } else {
            gatherUntil('\n');

            final String right = other.toString();
            sb.append(st).append(' ').append(var.get(right).value).append('\n');
            // skip the "store "
            var.put(key, var.get(right));
        }
        skipIdx(1); // skipping the new line
        reset(other);
    }

    public String optimize() {
        Pass();


        return sb.toString();
    }

    private void Pass() {
        boolean hasImprovement = true;
        int i = 1;
        while (true) {
            sb = new StringBuilder();

            while (hasImprovement) {

                hasImprovement = false;
                IO.println("- Pass " + i++ + ":");
                IO.println(sb.toString());

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
            if (sb.toString().isEmpty()) {
                IO.println("None left.");
                break;
            } else {
                chars = sb.toString().toCharArray();

                hasImprovement = true;
                idx = 0;
                reg.clear();
            }
        }
    }

}
