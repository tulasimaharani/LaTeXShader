package LaTeXShader;

import java.io.FileReader;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.StringReader;

import LaTeXShader.analysis.*;
import LaTeXShader.lexer.*;
import LaTeXShader.node.*;
import LaTeXShader.parser.*;

public class TreeDumper extends DepthFirstAdapter {
    private int depth = 0;
    private PrintWriter out;

    public TreeDumper(PrintWriter out) {
        this.out = out;
    }

    public void defaultCase(Node node) {
        indent();
        out.println(((Token)node).getText());
    }

    public void defaultIn(Node node) {
        indent();
        printNodeName(node);
        out.println();

        depth = depth+1;
    }

    public void defaultOut(Node node) {
        depth = depth-1;
        out.flush();
    }

    private void printNodeName(Node node) {
        String fullName = node.getClass().getName();
        String name = fullName.substring(fullName.lastIndexOf('.')+1);

        out.print(name);
    }

    private void indent() {
        for (int i = 0; i < depth; i++) out.write("   ");
    }

    public static void main(String[] args) {
        try {
        	String arquivo = "src/exemplos/eq_CookTorranceModel.tex";
	        Parser parser = new Parser(new Lexer(new PushbackReader(new FileReader(arquivo), 1024)));
	      	
            Start start = parser.parse();
            start.getPABlocoEquacoes().apply(new TreeDumper(new PrintWriter(System.out)));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
