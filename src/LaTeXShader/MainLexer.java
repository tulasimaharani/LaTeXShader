package LaTeXShader;

import java.io.*;

import LaTeXShader.lexer.*;
import LaTeXShader.node.*;

import java.io.* ;

public class MainLexer
{
	public static void main(String[] args)
	{
		try
		{
			String arquivo = "src/LaTeXShader/codeExamples/eq_CookTorranceModel.tex";
			//src/LaTeXShader/codeExamples/eq_AshikhminShirleyModel.tex
			//src/LaTeXShader/codeExamples/eq_BlinnPhongModel.tex
			//src/LaTeXShader/codeExamples/eq_CookTorranceModel.tex
			//src/LaTeXShader/codeExamples/eq_WardModel.tex
			Lexer lexer =
					new Lexer(
							new PushbackReader(  
									new FileReader(arquivo), 1024)); 
			Token token;
			while(!((token = lexer.next()) instanceof EOF)) {
				if (token.getClass().getName() != "latexToGLSL.node.TVazio") {
					System.out.print(token.getClass());
					System.out.println(" ( "+token.toString()+")");
				}
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
}