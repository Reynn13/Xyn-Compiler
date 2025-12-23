package errors;

import java.util.ArrayList;
import java.util.List;

public final class ErrorEngine {
    /// MUST ONLY ASSIGN BY LEXER
    public static String source;
    private static final Err[] errors = new Err[1000];
    private static int idx = 0;

    public static void add(Err e) {
        if (idx == 1000) {
            IO.println("Error: Too many errors. emitted now");
            reportAll();
        }
        errors[idx++] = e;
    }

    public static boolean hasError() {
        return idx != 0;
    }

    /**
     *
     * @param prompt the format
     * @param f the args, f[0] is the value form
     */
    public static void addWithValue(String prompt, int l, int s, int e, Object... f) {

        add(new DummyError(prompt, l, s, e, f));
    }
    private static ArrayList<ArrayList<Err>> gatherErrorByLine(int errorCount) {
        int minLine = 1;
        ArrayList<ArrayList<Err>> errs = new ArrayList<>(100);
        errs.add(new ArrayList<>(100));
        if (errorCount == 0) {
            // sort the error by line
            for (Err e : errors) {
                if (e.line > minLine) {
                    errs.add(new ArrayList<>(100));
                    errs.get(e.line-1).add(e);
                    minLine = e.line;
                } else {
                    errs.get(minLine-1).add(e);
                }
            }
            return errs;
        }
        for (int i = 0; i < errorCount; ++i) {
            Err e = errors[i];
            if (e.line > minLine) {
                errs.add(new ArrayList<>(100));
                errs.get(e.line-1).add(e);
                minLine = e.line;
            } else {
                errs.get(minLine-1).add(e);
            }
        }
        return errs;
    }

    private static int lengthOfIdx(int id) {
        int len = 1;
        int r = 10;
        while (true) {
            if (id % r == id) {
                return len;
            } else {
                ++len;
                r *= 10;
            }
        }
    }

    private static String highlight(String m, int si, int len) {
        final String substring = m.substring(si, si + len);
        return  (m.length() > (50 + len)) ?
                "..." +
                "\u001B[1;92m" +
                m.substring(Math.max(si - 10, si), si) +
                "\u001B[0m" +
                "\u001B[31m" +
                substring +
                "\u001B[0m" +
                "\u001B[1;92m" +
                m.substring(si + len, si + len + 9) +
                "\u001B[0m" +
                "..."
                :
                "\u001B[1;92m" +
                m.substring(0, si) +
                "\u001B[0m" +
                "\u001B[31m" +
                substring +
                "\u001B[0m" +
                "\u001B[1;92m" +
                m.substring(si + len) +
                "\u001B[0m";
    }
    /// automatically exit the system immediately
    /// TODO: <b>fix this method with more prettier output</b>
    public static void reportAll() {
        if (idx == 0) {
            System.exit(0);
        }
        List<String> lines = source.lines().toList();

        int id = 1;
        int line = 0;
        int offset = 0;
        System.out.printf("\n@ -> Program terminated: Found %d error(s) while compiling.\n\n", idx);
        for (ArrayList<Err> e : gatherErrorByLine(Math.min(idx, 10))) {
            IO.println("\u001B[97m| > line " + e.getFirst().line + ":\n|\u001B[0m");
            for (Err er : e) {
                String l = lines.get(line);
                IO.println("|   " + id + ". " + highlight(l, er.startIdx % l.length() - offset, er.endIdx - er.startIdx));
                IO.println("|" + " ".repeat(5 + lengthOfIdx(id++) + (er.startIdx % l.length()) - offset) + "\u001B[1;93m" + "^".repeat(er.endIdx - er.startIdx) + "\u001B[0m");
                IO.println("| >> " + er.msg);
                IO.println("|");
            }
            ++line;
            ++offset;
            IO.println("|");
            id = 1;
        }
        if (idx > 10) {
            System.out.printf("| \u001B[1;93mNote:\u001B[0m \u001B[97m%d errors left from line %d-%d\u001B[0m.", idx - 10, errors[10].line, errors[idx-1].line);
        }
        System.exit(0);
    }
}
