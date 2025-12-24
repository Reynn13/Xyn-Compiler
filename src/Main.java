import HIRs.HIRGen;
import HIRs.HIRPass;
import LIRs.LIRGen;
import lexer.Lexer;
import parser.Ast.statements.Block;
import parser.Parser;
import semantic.Semantic;


void main() {
//    StringBuilder sb = new StringBuilder(100_000);
//    // ~35 million chars
//    for (int i = 0; i < 1_000_000; ++i) {
//        sb.append("let a").append(i).append(" = ").append(i).append(';');
//    }
    Lexer lexer = new Lexer("let a = 10 + 2;");

    long t0 = System.nanoTime();

    Parser parser = new Parser(lexer.lex(), lexer.idx);
    Block g = parser.parse();

    new Semantic(g, parser.idx).check();

    HIRGen hirGenerator = new HIRGen(g.statements, parser.idx);

    HIRPass hirPass = new HIRPass(hirGenerator.generate().toCharArray());
    String s = hirPass.optimize();
    IO.println(s);

    LIRGen lirGenerator = new LIRGen(hirPass.var);
    IO.println("- LIR:");
    IO.println(lirGenerator.generate());
    long t1 = System.nanoTime();

    System.out.println((t1 - t0) / 1_000_000 + " ms");
}
