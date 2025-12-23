package LIRs;

import java.util.HashMap;

public final class LIRGen {
    private final char[] source;
    private final StringBuilder sb = new StringBuilder();
    private final StringBuilder other = new StringBuilder();

    private final HashMap<String, VarDesc> variable = new HashMap<>();

    private int idx = 0;
    public LIRGen(String s) {
        source = s.toCharArray();
    }

    private void skip(int i) {
        idx += i;
    }

    private void reset(StringBuilder s) {
        s.setLength(0);
    }

    private void skipUntil(char c) {
        while (source[idx] != c) {
            other.append(source[idx++]);
        }
    }

    private String skipUntilThenGet(char c) {
        skipUntil(c);
        final String f = other.toString();
        reset(other);
        return f;
    }

    private boolean isDigit(char c) {
    }

    private VarDesc getDigitOrFloat() {

    }

    private VarDesc getValue() {
        if (isDigit(source[idx])) {
            return getDigitOrFloat();
        }
        throw new Error("Unknown value");
    }




    public void generate() {
        while (idx < source.length) {
            switch (source[idx]) {
                // for store
                case 's':
                    // skip "store "
                    skip(6);
                    String varName = skipUntilThenGet(' ');
                    skip(1); // skip the space between varName and value
                    VarDesc value = getValue();
                    skip(1); // skip the newline;

                    // variable.put(varName, value);

            }
        }
    }
}
