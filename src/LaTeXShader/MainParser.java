package LaTeXShader;

import java.io.*;

import LaTeXShader.lexer.*;
import LaTeXShader.node.*;
import LaTeXShader.parser.*;
import LaTeXShader.semantic.SemanticAnalyser;

public class MainParser {
	public static void main(String[] args) {
		try {
			String arquivo = "src/LaTeXShader/codeExamples/eq_WardModel.tex";
			//src/LaTeXShader/codeExamples/eq_AshikhminShirleyModel.tex
			//src/LaTeXShader/codeExamples/eq_BlinnPhongModel.tex
			//src/LaTeXShader/codeExamples/eq_CookTorranceModel.tex
			//src/LaTeXShader/codeExamples/eq_WardModel.tex
			
			Lexer lex = new Lexer(new PushbackReader(new FileReader(arquivo), 1024));
			Parser p = new Parser(lex);
			Start tree = p.parse();
			
			// Imprime árvore na saída padrão
			//tree.apply(new ASTPrinter());
			
			// Imprime árvore em interface gráfica
			tree.apply(new ASTDisplay());
			
			// Inicia a análise semantica
			tree.apply(new SemanticAnalyser());
		
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}