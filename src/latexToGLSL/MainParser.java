package latexToGLSL;

import java.io.*;

import latexToGLSL.lexer.*;
import latexToGLSL.node.*;
import latexToGLSL.parser.*;
import latexToGLSL.semantic.SemanticAnalyser;

public class MainParser {
	public static void main(String[] args) {
		try {
			String arquivo = "src/exemplos/vetor3.tex";
			//src/exemplos/eq_AshikhminShirleyModel.tex
			//src/exemplos/eq_BlinnPhongModel.tex
			//src/exemplos/eq_CookTorranceModel.tex
			//src/exemplos/eq_WardModel.tex
			Lexer lex = new Lexer(new PushbackReader(new FileReader(arquivo), 1024));

			Parser p = new Parser(lex);

			Start tree = p.parse();
			// Imprime árvore na saída padrão
			//tree.apply(new ASTPrinter());
			// Imprime árvore em interface gráfica
			tree.apply(new ASTDisplay());
			tree.apply(new SemanticAnalyser());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}